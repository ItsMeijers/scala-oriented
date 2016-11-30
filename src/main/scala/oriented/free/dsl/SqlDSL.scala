package oriented.free.dsl

import cats.Id
import cats.data.{NonEmptyList}
import cats.free.{Free, Inject}
import com.tinkerpop.blueprints.impls.orient.OrientElement
import oriented._

/**
  * Algebra for actions that are performed by using the extended SQL language of OrientDB.
  */
sealed trait SqlDSL[A]

/**
  * Constructor for an SQL action resulting into a Unique Vertex of type A.
  */
case class UniqueVertex[A](query: String, format: OrientFormat[A]) extends SqlDSL[Id[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Unique Edge of type A.
  */
case class UniqueEdge[A](query: String, format: OrientFormat[A]) extends SqlDSL[Id[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an Optional Vertex of type A.
  */
case class OptionalVertex[A](query: String, format: OrientFormat[A]) extends SqlDSL[Option[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Optional Edge of type A.
  */
case class OptionalEdge[A](query: String, format: OrientFormat[A]) extends SqlDSL[Option[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an List of vertices of type A.
  */
case class VertexList[A](query: String, format: OrientFormat[A]) extends SqlDSL[List[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an List of edges of type A.
  */
case class EdgeList[A](query: String, format: OrientFormat[A]) extends SqlDSL[List[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an Non Empty List of vertices of type A.
  */
case class VertexNel[A](query: String, format: OrientFormat[A]) extends SqlDSL[NonEmptyList[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Non Empty list of edges of type A.
  */
case class EdgeNel[A](query: String, format: OrientFormat[A]) extends SqlDSL[NonEmptyList[Edge[A]]]

/**
  * Constructor for an SQL action resulting in Unit.
  */
case class UnitDSL(query: String) extends SqlDSL[Unit]

/**
  * Constructor for SQL action resulting in Simple type such as Long, Int, String etc.
  */
case class As[A](query: String, field: String, format: OrientFormat[A]) extends SqlDSL[A]

/**
  * Constructor for SQL insert resulting in an Edge
  */
case class InsertEdge[A](query: String, format: OrientFormat[A]) extends SqlDSL[Id[Edge[A]]]

/**
  * Constructor for SQL insert resulting in an Vertex
  */
case class InsertVertex[A](query: String, format: OrientFormat[A]) extends SqlDSL[Id[Vertex[A]]]

class Sqls[F[_]](implicit inject: Inject[SqlDSL, F]) {

  def uniqueVertex[A](query: String, format: OrientFormat[A]): Free[F, Id[Vertex[A]]] =
    Free.inject[SqlDSL, F](UniqueVertex[A](query, format))

  def uniqueEdge[A](query: String, format: OrientFormat[A]): Free[F, Id[Edge[A]]] =
    Free.inject[SqlDSL, F](UniqueEdge[A](query, format))

  def optionalVertex[A](query: String, format: OrientFormat[A]): Free[F, Option[Vertex[A]]] =
    Free.inject[SqlDSL, F](OptionalVertex[A](query, format))

  def optionalEdge[A](query: String, format: OrientFormat[A]): Free[F, Option[Edge[A]]] =
    Free.inject[SqlDSL, F](OptionalEdge[A](query, format))

  def vertexList[A](query: String, format: OrientFormat[A]): Free[F, List[Vertex[A]]] =
    Free.inject[SqlDSL, F](VertexList[A](query, format))

  def edgeList[A](query: String, format: OrientFormat[A]): Free[F, List[Edge[A]]] =
    Free.inject[SqlDSL, F](EdgeList[A](query, format))

  def vertexNel[A](query: String, format: OrientFormat[A]): Free[F, NonEmptyList[Vertex[A]]] =
    Free.inject[SqlDSL, F](VertexNel[A](query, format))

  def edgeNel[A](query: String, format: OrientFormat[A]): Free[F, NonEmptyList[Edge[A]]] =
    Free.inject[SqlDSL, F](EdgeNel[A](query, format))

  def unit(query: String): Free[F, Unit] =
    Free.inject[SqlDSL, F](UnitDSL(query))

  def as[A](query: String, field: String, format: OrientFormat[A]): Free[F, A] =
    Free.inject[SqlDSL, F](As[A](query, field, format))

  def insertEdge[A](query: String, format: OrientFormat[A]): Free[F, Id[Edge[A]]] =
    Free.inject[SqlDSL, F](InsertEdge[A](query, format))

  def insertVertex[A](query: String, format: OrientFormat[A]): Free[F, Id[Vertex[A]]] =
    Free.inject[SqlDSL, F](InsertVertex[A](query, format))

}

object Sqls {
  def sqls[F[_]](implicit inject: Inject[SqlDSL, F]): Sqls[F] = new Sqls[F]
}
