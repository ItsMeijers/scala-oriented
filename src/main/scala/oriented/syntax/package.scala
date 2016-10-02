package oriented

import cats.data.{Coproduct, EitherT}
import cats.free.Free
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph
import oriented.free.dsl._
import oriented.free.interpreters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import cats.instances.try_.catsStdInstancesForTry
import cats.instances.future.catsStdInstancesForFuture
import cats.{Id, ~>}

/**
  * Syntax package for importing types and implicits in scope.
  */
package object syntax {


  /**
    * Type of an OrientProgram where each DSL is combined into one program.
    */
  type OrientProgram[A] =
    Coproduct[EdgeDSL, Coproduct[VertexDSL, Coproduct[ElementDSL, Coproduct[ClientDSL, SqlDSL, ?], ?], ?], A]

  /**
    * An OrientIO is a Free from the OrientProgram co product resulting in a type A.
    */
  type OrientIO[A] = Free[OrientProgram, A]

  type OrientRead[A] = Free[ReadDSL, A]

  implicit class OrientIOInterpreter[A](orientIO: OrientIO[A]) {

    /**
      * Overloaded function of runUnsafe.
      */
    def runUnsafe(orientIO: OrientIO[A])(implicit orientClient: OrientClient): A =
      runUnsafe(enableTransactions = true)

    /**
      * Runs the OrientIO unsafely.
      * Can throw errors and does not control side effects!
      */
    def runUnsafe(enableTransactions: Boolean)(implicit orientClient: OrientClient): A = {
      implicit val graph: OrientBaseGraph =
        if(enableTransactions) orientClient.graph
        else orientClient.graphNoTransaction

      val unsafeInterpreter: OrientProgram ~> Id =
        UnsafeEdgeInterpreter     or
       (UnsafeVertexInterpreter   or
       (UnsafeElementInterpreter  or
       (UnsafeClientInterpreter() or
        UnsafeSqlInterpreter())))

      val result = orientIO.foldMap(unsafeInterpreter)

      graph.shutdown()

      result
    }

    /**
      * Overloaded function of tryRun.
      */
    def tryRun(implicit orientClient: OrientClient): Try[A] = tryRun(enableTransactions = true)

    /**
      * Runs the orientIO safely resulting in a Try[A].
      */
    def tryRun(enableTransactions: Boolean)(implicit orientClient: OrientClient): Try[A] = {
      implicit val graph: OrientBaseGraph = if(enableTransactions) orientClient.graph
      else orientClient.graphNoTransaction

      val tryInterpreter: OrientProgram ~> Try =
        TryEdgeInterpreter     or
       (TryVertexInterpreter   or
       (TryElementInterpreter  or
       (TryClientInterpreter() or
        TrySqlInterpreter())))

      val result = orientIO.foldMap(tryInterpreter)

      if(result.isFailure && enableTransactions) graph.rollback()

      graph.shutdown()

      result
    }

    /**
      * Overloaded function of runSafe.
      */
    def run(implicit orientClient: OrientClient): Either[Throwable, A] =
      run(enableTransactions = true)

    /**
      * Runs the orientIO safely resulting in either a Throwable or A, where A is the result of Free.
      */
    def run(enableTransactions: Boolean)(implicit orientClient: OrientClient): Either[Throwable, A] =
      tryRun(enableTransactions) match {
        case Failure(exception) => Left(exception)
        case Success(a)         => Right(a)
      }

    /**
      * Overloaded function of runAsyncUnsafe
      */
    def runAsyncUnsafe(implicit executionContext: ExecutionContext, orientClient: OrientClient): Future[A] =
      runAsyncUnsafe(enableTransactions = true)

    /**
      * Runs the orientIO resulting in Futures, note that this is expirimental since the OrientElements are not thread
      * save.
      */
    def runAsyncUnsafe(enableTransactions: Boolean)
                      (implicit executionContext: ExecutionContext,
                       orientClient: OrientClient): Future[A] = {
      implicit val graph: OrientBaseGraph = if(enableTransactions) orientClient.graph
        else orientClient.graphNoTransaction

      val asyncInterpreter: OrientProgram ~> Future =
        AsyncEdgeInterpreter()    or
       (AsyncVertexInterpreter()  or
       (AsyncElementInterpreter() or
       (AsyncClientInterpreter()  or
        AsyncSqlInterpreter())))

      val result = orientIO.foldMap(asyncInterpreter)

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
      * Overloaded function of runAsyncSafe
      */
    def runAsync(implicit executionContext: ExecutionContext,
                 orientClient: OrientClient): EitherT[Future, Throwable, A] =
      runAsync(enableTransactions = true)

    /**
      * Runs the orientIO resulting in a Either Transformer of Future Either[Throwable, A]
      */
    def runAsync(enableTransactions: Boolean)
                (implicit executionContext: ExecutionContext,
                     orientClient: OrientClient): EitherT[Future, Throwable, A] = {
      implicit val graph: OrientBaseGraph = if(enableTransactions) orientClient.graph
      else orientClient.graphNoTransaction

      val interpreter: OrientProgram ~> EitherT[Future, Throwable, ?] =
        SafeAsyncEdgeInterpreter() or
       (SafeAsyncVertexInterpreter() or
       (SafeAsyncElementInterpreter() or
       (SafeAsyncClientInterpreter() or
       SafeAsyncSqlInterpreter())))


      val result = orientIO.foldMap(interpreter)

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

  }

  /**
    * Implicit conversion for sql interpolation
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