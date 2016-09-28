package oriented

import cats.data.EitherT
import cats.free.Free
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph
import oriented.free.dsl._
import oriented.free.interpreters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import cats.instances.try_.catsStdInstancesForTry
import cats.instances.future.catsStdInstancesForFuture
import freek._

/**
  * Syntax package for importing types and implicits in scope.
  */
package object syntax {

  /**
    * Type of an OrientProgram where each DSL is combined into one program.
    */
  type OrientProgram = SqlDSL :|: ClientDSL :|: ElementDSL :|: VertexDSL :|: EdgeDSL :|: NilDSL

  /**
    * Creates the OrientProgram DSL
    */
  val OP = DSL.Make[OrientProgram]

  /**
    * An OrientIO is a Free from the OrientProgram co product resulting in a type R.
    */
  type OrientIO[R] = Free[OP.Cop, R]

  /**
    * Overloaded function of runUnsafe.
    */
  def runUnsafe[A](orientIO: OrientIO[A])(implicit orientClient: OrientClient): A =
    runUnsafe(orientIO, enableTransactions = true)

  /**
    * Runs the OrientIO unsafely.
    * Can throw errors and does not control side effects!
    */
  def runUnsafe[A](orientIO: OrientIO[A], enableTransactions: Boolean)(implicit orientClient: OrientClient): A = {
    implicit val graph: OrientBaseGraph =
      if(enableTransactions) orientClient.graph
      else orientClient.graphNoTransaction

    val result = orientIO.interpret(
      UnsafeSqlInterpreter()    :&:
      UnsafeClientInterpreter() :&:
      UnsafeElementInterpreter  :&:
      UnsafeVertexInterpreter   :&:
      UnsafeEdgeInterpreter)

    graph.shutdown()

    result
  }


  /**
    * Overloaded function of runSafe.
    */
  def runSafe[A](orientIO: OrientIO[A])(implicit orientClient: OrientClient): Either[Throwable, A] = runSafe(orientIO, enableTransactions = true)

  /**
    * Runs the orientIO safely resulting in either a Throwable or A, where A is the result of Free.
    */
  def runSafe[A](orientIO: OrientIO[A], enableTransactions: Boolean)(implicit orientClient: OrientClient): Either[Throwable, A] =
  tryRun(orientIO, enableTransactions) match {
    case Failure(exception) => Left(exception)
    case Success(a)         => Right(a)
  }

  /**
    * Overloaded function of tryRun.
    */
  def tryRun[A](orientIO: OrientIO[A])(implicit orientClient: OrientClient): Try[A] = tryRun(orientIO, enableTransactions = true)

  /**
    * Runs the orientIO safely resulting in a Try[A].
    */
  def tryRun[A](orientIO: OrientIO[A], enableTransactions: Boolean)(implicit orientClient: OrientClient): Try[A] = {
    implicit val graph: OrientBaseGraph = if(enableTransactions) orientClient.graph
    else orientClient.graphNoTransaction

    val result = orientIO.interpret(
      TrySqlInterpreter()    :&:
        TryClientInterpreter() :&:
        TryElementInterpreter  :&:
        TryVertexInterpreter  :&:
        TryEdgeInterpreter)

    if(result.isFailure && enableTransactions) graph.rollback()

    graph.shutdown()

    result
  }

  /**
    * TODO
    */
  def runAsyncUnsafe[A](orientIO: OrientIO[A])(implicit executionContext: ExecutionContext, orientClient: OrientClient): Future[A] =
    runAsyncUnsafe(orientIO, enableTransactions = true)

  /**
    * TODO
    */
  def runAsyncUnsafe[A](orientIO: OrientIO[A], enableTransactions: Boolean)(implicit executionContext: ExecutionContext, orientClient: OrientClient): Future[A] = {
    implicit val graph: OrientBaseGraph = if(enableTransactions) orientClient.graph
      else orientClient.graphNoTransaction

    val result = orientIO.interpret(
      AsyncSqlInterpreter()     :&:
        AsyncClientInterpreter()  :&:
        AsyncElementInterpreter() :&:
        AsyncVertexInterpreter()  :&:
        AsyncEdgeInterpreter())

    result.onFailure(PartialFunction { _ =>
      if(enableTransactions) graph.rollback()
      graph.shutdown()
    })

    result.onSuccess(PartialFunction { _ =>
      graph.shutdown()
    })

    result
  }


  /**
    * TODO
    */
  def runAsyncSafe[A](orientIO: OrientIO[A])(implicit executionContext: ExecutionContext, orientClient: OrientClient): EitherT[Future, Throwable, A] =
  runAsyncSafe(orientIO, enableTransactions = true)

  /**
    * TODO
    */
  def runAsyncSafe[A](orientIO: OrientIO[A], enableTransactions: Boolean)(implicit executionContext: ExecutionContext, orientClient: OrientClient): EitherT[Future, Throwable, A] = {
    implicit val graph: OrientBaseGraph = if(enableTransactions) orientClient.graph
    else orientClient.graphNoTransaction

    val result = orientIO.interpret(
      SafeAsyncSqlInterpreter()     :&:
        SafeAsyncClientInterpreter()  :&:
        SafeAsyncElementInterpreter() :&:
        SafeAsyncVertexInterpreter()  :&:
        SafeAsyncEdgeInterpreter())

    val isLeft = result.isLeft

    isLeft.onFailure(PartialFunction { _ =>
      if(enableTransactions) graph.rollback()
      graph.shutdown()
    })

    isLeft.onSuccess(PartialFunction { failed =>
      if(failed && enableTransactions) graph.rollback()
      graph.shutdown()
    })

    result
  }

  /**
    * TODO
    */
  implicit class OrientSqlWrapper(val sql: StringContext) {

    /**
      * String interpolation method, currently using standardInterpolater passing to SqlStatement Class
      */
    def sql(args: Any*): SQLStatement = {
      SQLStatement(sql.standardInterpolator(identity, args))
    }

  }

  /**
    * TODO
    */
  implicit class VertexToEdgeModels[V](val vertexModel: V)(implicit val orientFormat: OrientFormat[V]) {

    /**
      * TODO
      */
    def --[E](edgeModel: E)(implicit orientFormatEdge: OrientFormat[E]): VertexToEdgeToVertexModels[V, E] =
    new VertexToEdgeToVertexModels(vertexModel, edgeModel)(orientFormat, orientFormatEdge)

  }

  /**
    * TODO
    */
  class VertexToEdgeToVertexModels[V, E](val vertexModel: V,
                                         val edgeModel: E)
                                        (implicit val orientFormatV: OrientFormat[V],
                                         val orientFormatE: OrientFormat[E]) {

    /**
      * TODO
      */
    def -->[VT](vertexTwo: VT)
               (implicit orientFormatVT: OrientFormat[VT],
                client: OrientClient): OrientIO[(Vertex[V], Edge[E], Vertex[VT])] =
    for {
      vertex    <- client.addVertex(vertexModel)
      vertexTwo <- client.addVertex(vertexTwo)
      edge      <- vertex.addEdge(edgeModel, vertexTwo)
    } yield (vertex, edge, vertexTwo)

  }

  /**
    * TODO
    */
  implicit class VertexToEdge[V](val vertex: Vertex[V]) {

    def --[E](edge: E)(implicit orientFormat: OrientFormat[E]): VertexToEdgeToVertex[V, E] =
      new VertexToEdgeToVertex(vertex, edge)

  }

  /**
    * TODO
    */
  class VertexToEdgeToVertex[V, E](val vertex: Vertex[V], val edge: E)(implicit val orientFormat: OrientFormat[E]) {
    def -->[VT](vertexTwo: Vertex[VT]): OrientIO[Edge[E]] = vertex.addEdge(edge, vertexTwo)
  }

}