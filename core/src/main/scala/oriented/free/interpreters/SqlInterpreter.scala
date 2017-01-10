package oriented.free.interpreters

import scala.collection.JavaConverters._
import cats.data.{EitherT, Reader}
import cats.{Id, ~>}
import cats.syntax.list._
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.impls.orient._
import oriented.{Edge, Element, Vertex}
import oriented.free.dsl._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * The SQL Interpreter forms a natural transformation from SqlDSL to a higher kinded G
  */
sealed trait SqlInterpreter[G[_]] extends (SqlDSL ~> G) {

  def graph: OrientBaseGraph

  private def executeIterable[E <: Element[_]](query: String)(toElement: Any => E) =
    graph
    .command(new OCommandSQL(query))
    .execute[OrientDynaElementIterable]()
    .asScala
    .map(toElement)
    .toList

  private def executeCommandVertex[A](query: String, f: Reader[OrientElement, A]): List[Vertex[A]] =
    executeIterable[Vertex[A]](query) { r =>
      val orientVertex = r.asInstanceOf[OrientVertex]
      Vertex(f(orientVertex), orientVertex)
    }

  private def executeCommandEdge[A](query: String, f: Reader[OrientElement,A]): List[Edge[A]] =
    executeIterable[Edge[A]](query) { r =>
      val orientEdge = r.asInstanceOf[OrientEdge]
      Edge(f(orientEdge), orientEdge)
    }

  private def executeCommand[A](query: String, f: Reader[OrientElement, A]): A =
    f(graph
      .command(new OCommandSQL(query))
      .execute[OrientDynaElementIterable]()
      .asScala
      .head.asInstanceOf[OrientElement])

  private def executeInsertVertex[A](query: String, f: Reader[OrientElement, A]): Vertex[A] = {
    val orientVertex = graph.command(new OCommandSQL(query)).execute[OrientVertex]()
    Vertex(f(orientVertex), orientVertex)
  }

  private def executeInsertEdge[A](query: String, f: Reader[OrientElement, A]): Edge[A] = {
    val orientEdge = graph.command(new OCommandSQL(query)).execute[OrientEdge]()
    Edge(f(orientEdge), orientEdge)
  }


  /**
    * Evaluates each SqlDSL A constructor to A
    */
  protected def evaluateDSL[A](fa: SqlDSL[A]): A = fa match {
    case UniqueVertex(query, f)   => executeCommandVertex(query, f).head
    case UniqueEdge(query, f)     => executeCommandEdge(query, f).head
    case OptionalVertex(query, f) => executeCommandVertex(query, f).headOption
    case OptionalEdge(query, f)   => executeCommandEdge(query, f).headOption
    case VertexList(query, f)     => executeCommandVertex(query, f)
    case EdgeList(query,f)        => executeCommandEdge(query, f)
    case VertexNel(query, f)      => executeCommandVertex(query, f).toNel.get
    case EdgeNel(query, f)        => executeCommandEdge(query, f).toNel.get
    case InsertVertex(query, f)   => executeInsertVertex(query, f)
    case InsertEdge(query, f)     => executeInsertEdge(query, f)
    case As(query, field, f)      => executeCommand(query, f)
    case UnitDSL(query)           =>
      graph.command(new OCommandSQL(query)).execute()
      ()
  }
}

/**
  * The UnsafeElementInterpreter interprets the ElementDSL without any containment of side effect.
  */
case class UnsafeSqlInterpreter(implicit val graph: OrientBaseGraph) extends SqlInterpreter[Id] {

  /**
    * * Transformation from SqlDSL to Id
    */
  def apply[A](fa: SqlDSL[A]): Id[A] = evaluateDSL(fa)

}

/**
  * The TrySqlInterpreter interprets the SqlDSL and contains all side effect in the Try Monad.
  */
case class TrySqlInterpreter(implicit val graph: OrientBaseGraph) extends SqlInterpreter[Try] {

  /**
    * * Transformation from SqlDSL to Try
    */
  override def apply[A](fa: SqlDSL[A]): Try[A] = Try(evaluateDSL(fa))

}

/**
  * The AsyncSqlInterpreter interprets the SqlDSL components in a Future, but does not
  * contain any side effect in the case of errors.
  */
case class AsyncSqlInterpreter(implicit val executionContext: ExecutionContext, val graph: OrientBaseGraph) extends SqlInterpreter[Future] {

  /**
    * * Transformation from SqlDSL to Future
    */
  override def apply[A](fa: SqlDSL[A]): Future[A] = Future(evaluateDSL(fa))

}

/**
  * The SafeAsyncSqlInterpreter interprets the SqlDSL components in a Future but resulting into an Either
  * containing the errors of side effect in the Left branch of the Either instance.
  */
case class SafeAsyncSqlInterpreter(implicit val executionContext: ExecutionContext, val graph: OrientBaseGraph) extends SqlInterpreter[EitherT[Future, Throwable, ?]] {

  /**
    * Transformation from SqlDSL to EitherT
    */
  override def apply[A](fa: SqlDSL[A]): EitherT[Future, Throwable, A] =
    EitherT[Future, Throwable, A](
      Future(Try(evaluateDSL(fa)) match {
        case Failure(ex) => Left(ex)
        case Success(a)  => Right(a)
      })
    )

}