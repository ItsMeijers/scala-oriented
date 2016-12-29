package oriented.free.interpreters

import collection.JavaConverters._
import cats.{Id, ~>}
import cats.data.EitherT
import com.tinkerpop.blueprints.impls.orient.{OrientEdge, OrientVertex}
import com.tinkerpop.blueprints.Direction
import oriented._
import oriented.free.dsl._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * TODO
  */
sealed trait VertexInterpreter[M[_]] extends (VertexDSL ~> M) {

  /**
    * TODO
    */
  def getDirection(dir: oriented.Direction): Direction = dir match {
    case In => Direction.IN
    case Out => Direction.OUT
    case Both => Direction.BOTH
  }

  /**
    * TODO
    */
  def interpretDSL[A](fa: VertexDSL[A]): A = fa match {
    case AddEdgeToVertex(vertex, edgeModel, inVertex, clusterName, of) =>
      val elements = of.properties(edgeModel).asJava

      val orientEdge = clusterName.map { cn =>
        vertex.orientElement.addEdge(
          of.name,
          inVertex.orientElement,
          null,
          cn,
          elements)
      }.getOrElse {
        vertex.orientElement.addEdge(of.name, inVertex.orientElement, null, null, elements)
      }

      Edge(edgeModel, orientEdge)
    case CountEdges(vertex, dir, of) => vertex.orientElement.countEdges(getDirection(dir), of.name)
    case GetEdgesDestination(vertex, destination, direction, orientFormat) =>
      vertex
        .orientElement
        .getEdges(destination.orientElement, getDirection(direction), orientFormat.name)
        .asScala.map { te =>
          val orientEdge = te.asInstanceOf[OrientEdge]
          Edge(orientFormat.reader.run(orientEdge), orientEdge)
        }.toList

    case GetEdges(vertex, direction, orientFormat) =>
      vertex
        .orientElement
        .getEdges(getDirection(direction), orientFormat.name)
        .asScala
        .map { te =>
          val orientEdge = te.asInstanceOf[OrientEdge]
          Edge(orientFormat.reader.run(orientEdge), orientEdge)
        }.toList

    case GetType(vertex) => VertexType(vertex.orientElement.getType)
    case GetVertices(vertex, direction, orientFormatEdge, orientFormatVertex) =>
      vertex.orientElement.getVertices(getDirection(direction), orientFormatEdge.name)
        .asScala
        .map(_.asInstanceOf[OrientVertex])
        .filter(_.getLabel == orientFormatVertex.name)
        .map { orientVertex =>
          Vertex(orientFormatVertex.reader.run(orientVertex), orientVertex)
        }.toList
    case UpdateVertex(newModel, orientVertex, orientFormat) =>

      orientVertex.setProperties(orientFormat.properties(newModel).asJava)

      Vertex(newModel, orientVertex)
  }
}

/**
  * TODO
  */
object UnsafeVertexInterpreter extends VertexInterpreter[Id] {
  def apply[A](fa: VertexDSL[A]): Id[A] = interpretDSL(fa)
}

/**
  * TODO
  */
object TryVertexInterpreter extends VertexInterpreter[Try] {
  def apply[A](fa: VertexDSL[A]): Try[A] = Try(interpretDSL(fa))
}

/**
  * TODO
  */
case class AsyncVertexInterpreter(implicit val executionContext: ExecutionContext) extends VertexInterpreter[Future] {
  def apply[A](fa: VertexDSL[A]): Future[A] = Future(interpretDSL(fa))
}

/**
  * TODO
  */
case class SafeAsyncVertexInterpreter(implicit val executionContext: ExecutionContext) extends VertexInterpreter[EitherT[Future, Throwable, ?]] {
  def apply[A](fa: VertexDSL[A]): EitherT[Future, Throwable, A] = EitherT[Future, Throwable, A](
    Future(Try(interpretDSL(fa)) match {
      case Failure(exc) => Left(exc)
      case Success(v)   => Right(v)
    })
  )
}


