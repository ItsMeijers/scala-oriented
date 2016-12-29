package oriented.free.interpreters

import cats.data.EitherT
import cats.{Id, ~>}
import com.tinkerpop.blueprints.Direction
import oriented._
import oriented.free.dsl.{EdgeDSL, GetInVertex, GetOutVertex, UpdateEdge}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * An EdgeInterpreter forms a natural transformation from EdgeDSL to a higher kinded type G.
  */
sealed trait EdgeInterpreter[G[_]] extends (EdgeDSL ~> G) {

  /**
    * Helper function to construct a Vertex.
    */
  def getVertex[A, B](edge: Edge[A], direction: Direction, orientFormat: OrientFormat[B]): Vertex[B] = {
    val vertexElement = edge.orientElement.getVertex(direction)
    Vertex(orientFormat.reader.run(vertexElement), vertexElement)
  }

  /**
    * Interprets an constructor of EdgeDSL[A] resulting in the A.
    */
  def interpretDSL[A](fa: EdgeDSL[A]): A = fa match {
    case GetInVertex(edge, orientFormat)  => getVertex(edge, Direction.IN, orientFormat)
    case GetOutVertex(edge, orientFormat) => getVertex(edge, Direction.OUT, orientFormat)
    case UpdateEdge(newModel, orientEdge, orientFormat) =>

      orientEdge.setProperties(orientFormat.properties(newModel).asJava)

      Edge(newModel, orientEdge)
  }

}

/**
  * The UnsafeEdgeInterpreter interpretes the EdgeDSL without any containment of side effect.
  */
object UnsafeEdgeInterpreter extends EdgeInterpreter[Id] {

  /**
    * Transformation of EdgeDSL to Id.
    */
  override def apply[A](fa: EdgeDSL[A]): Id[A] = interpretDSL(fa)

}

/**
  * The TryEdgeInterpreter interprets the ClientDSL by containing the side effect in the Try Monad.
  */
object TryEdgeInterpreter extends EdgeInterpreter[Try] {

  /**
    * Transformation of EdgeDSL to Try.
    */
  override def apply[A](fa: EdgeDSL[A]): Try[A] = Try(interpretDSL(fa))

}

/**
  * The AsyncEdgeInterpreter interprets each of the DSL components of one OrientAction in a Future, but does not
  * contain any side effect in the case of errors.
  */
case class AsyncEdgeInterpreter(implicit val executionContext: ExecutionContext) extends EdgeInterpreter[Future] {

  /**
    * Transformation of EdgeDSL to Future.
    */
  override def apply[A](fa: EdgeDSL[A]): Future[A] = Future(interpretDSL(fa))

}

/**
  * The SafeAsyncEdgeInterpreter interprets each of the DSL Constructors in a Future but resulting into an Either
  * containing the errors of side effect in the Left branch of the Either instance.
  */
case class SafeAsyncEdgeInterpreter(implicit executionContext: ExecutionContext) extends EdgeInterpreter[EitherT[Future, Throwable, ?]] {

  /**
    * Transformation of EdgeDSL to EitherT.
    */
  override def apply[A](fa: EdgeDSL[A]): EitherT[Future, Throwable, A] =
    EitherT[Future, Throwable, A](
      Future(Try(interpretDSL(fa)) match {
        case Failure(exc) => Left(exc)
        case Success(v)   => Right(v)
      })
    )

}
