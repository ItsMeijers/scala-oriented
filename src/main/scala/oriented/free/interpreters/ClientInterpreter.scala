package oriented.free.interpreters

import java.util
import cats.data.EitherT
import cats.{Id, ~>}
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientEdge, OrientVertex}
import oriented._
import oriented.free.dsl._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * A ClientInterpreter forms a natural transformation from ClientDSL to a higher kinded G.
  */
trait ClientInterpreter[G[_]] extends (ClientDSL ~> G) {

  def graph: OrientBaseGraph

  private def createVertexType[A](orientFormat: OrientFormat[A]): VertexType[A] =
    VertexType(graph.createVertexType(orientFormat.name))

  private def createEdgeType[A](orientFormat: OrientFormat[A]): EdgeType[A] =
    EdgeType(graph.createEdgeType(orientFormat.name))

  private def addVertex[A](vertexModel: A, orientFormat: OrientFormat[A]): Vertex[A] = {
    val vertex: OrientVertex = graph.addVertex(s"class:${orientFormat.name}", new util.ArrayList[Any]())

    orientFormat.properties(vertexModel).foreach { case (key, value) =>
      vertex.setProperty(key, value)
    }

    Vertex(vertexModel, vertex)
  }

  private def addEdge[A, B, C](edgeModel: A,
                               inVertex: Vertex[B],
                               outVertex: Vertex[C],
                               orientFormat: OrientFormat[A]): Edge[A] = {

    val edge: OrientEdge = graph.addEdge(
      s"class:${orientFormat.name}",
      outVertex.orientElement,
      inVertex.orientElement,
      null)

    orientFormat.properties(edgeModel).foreach { case (key, value) =>
      edge.setProperty(key, value)
    }

    Edge(edgeModel, edge)
  }

  /**
    * Matches each subtype of ClientDSL resulting into the expected result type A of each DSL constructor.
    */
  protected def evaluateDSL[A](fa: ClientDSL[A]): A = fa match {
    case CreateVertexType(orientFormat)           => createVertexType(orientFormat)
    case CreateEdgeType(orientFormat)             => createEdgeType(orientFormat)
    case AddVertex(vertexModel, orientFormat)     => addVertex(vertexModel, orientFormat)
    case AddEdge(edgeModel, iv, ov, orientFormat) => addEdge(edgeModel, iv, ov, orientFormat)
  }

}

/**
  * The UnsafeClientInterpreter interprets the ClientDSL without any containment of side effect.
  */
case class UnsafeClientInterpreter(implicit val graph: OrientBaseGraph) extends ClientInterpreter[Id] {

  /**
    * Transformation of ClientDSL to Id.
    */
  override def apply[A](fa: ClientDSL[A]): Id[A] = evaluateDSL(fa)

}

/**
  * The TryClientInterpreter interprets the ClientDSL by containing the side effect in the Try Monad.
  */
case class TryClientInterpreter(implicit val graph: OrientBaseGraph) extends ClientInterpreter[Try] {

  /**
    * Transformation of ClientDSL to Try.
    */
  override def apply[A](fa: ClientDSL[A]): Try[A] = Try(evaluateDSL(fa))

}

/**
  * The AsyncClientInterpreter interprets each of the DSL components of one OrientAction in a Future, but does not
  * contain any side effect in the case of errors.
  */
case class AsyncClientInterpreter(implicit val executionContext: ExecutionContext, val graph: OrientBaseGraph) extends ClientInterpreter[Future] {

  /**
    * Transformation of ClientDSL to Future.
    */
  override def apply[A](fa: ClientDSL[A]): Future[A] = Future(evaluateDSL(fa))

}

/**
  * The SafeAsyncClientInterpreter interprets each of the DSL Constructors in a Future but resulting into an Either
  * containing the errors of side effect in the Left branch of the Either instance.
  */
case class SafeAsyncClientInterpreter(implicit val executionContext: ExecutionContext, val graph: OrientBaseGraph) extends ClientInterpreter[EitherT[Future, Throwable, ?]] {

  /**
    * Transformation of ClientDSL to EitherT.
    */
  override def apply[A](fa: ClientDSL[A]): EitherT[Future, Throwable, A] =
  EitherT[Future, Throwable, A](
    Future(Try(evaluateDSL(fa)) match {
      case Failure(exc) => Left(exc)
      case Success(a)   => Right(a)
    })
  )

}