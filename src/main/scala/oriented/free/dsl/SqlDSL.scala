package oriented.free.dsl

import cats.Id
import cats.data.{NonEmptyList, Reader}
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
case class UniqueVertex[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[Id[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Unique Edge of type A.
  */
case class UniqueEdge[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[Id[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an Optional Vertex of type A.
  */
case class OptionalVertex[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[Option[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Optional Edge of type A.
  */
case class OptionalEdge[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[Option[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an List of vertices of type A.
  */
case class VertexList[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[List[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an List of edges of type A.
  */
case class EdgeList[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[List[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an Non Empty List of vertices of type A.
  */
case class VertexNel[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[NonEmptyList[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Non Empty list of edges of type A.
  */
case class EdgeNel[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[NonEmptyList[Edge[A]]]

/**
  * Constructor for an SQL action resulting in Unit.
  */
case class UnitDSL(query: String) extends SqlDSL[Unit]

/**
  * Constructor for SQL action resulting in Simple type such as Long, Int, String etc.
  */
case class As[A](query: String, field: String, reader: Reader[OrientElement, A]) extends SqlDSL[A]

/**
  * Constructor for SQL insert resulting in an Edge
  */
case class InsertEdge[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[Id[Edge[A]]]

/**
  * Constructor for SQL insert resulting in an Vertex
  */
case class InsertVertex[A](query: String, reader: Reader[OrientElement, A]) extends SqlDSL[Id[Vertex[A]]]

class Sqls[F[_]](implicit inject: Inject[SqlDSL, F]) {

  def uniqueVertex[A](query: String, reader: Reader[OrientElement, A]): Free[F, Id[Vertex[A]]] =
  Free.inject[SqlDSL, F](UniqueVertex[A](query, reader))

  def uniqueEdge[A](query: String, reader: Reader[OrientElement, A]): Free[F, Id[Edge[A]]] =
    Free.inject[SqlDSL, F](UniqueEdge[A](query, reader))

  def optionalVertex[A](query: String, reader: Reader[OrientElement, A]): Free[F, Option[Vertex[A]]] =
    Free.inject[SqlDSL, F](OptionalVertex[A](query, reader))

  def optionalEdge[A](query: String, reader: Reader[OrientElement, A]): Free[F, Option[Edge[A]]] =
    Free.inject[SqlDSL, F](OptionalEdge[A](query, reader))

  def vertexList[A](query: String, reader: Reader[OrientElement, A]): Free[F, List[Vertex[A]]] =
    Free.inject[SqlDSL, F](VertexList[A](query, reader))

  def edgeList[A](query: String, reader: Reader[OrientElement, A]): Free[F, List[Edge[A]]] =
    Free.inject[SqlDSL, F](EdgeList[A](query, reader))

  def vertexNel[A](query: String, reader: Reader[OrientElement, A]): Free[F, NonEmptyList[Vertex[A]]] =
    Free.inject[SqlDSL, F](VertexNel[A](query, reader))

  def edgeNel[A](query: String, reader: Reader[OrientElement, A]): Free[F, NonEmptyList[Edge[A]]] =
    Free.inject[SqlDSL, F](EdgeNel[A](query, reader))

  def unit(query: String): Free[F, Unit] =
    Free.inject[SqlDSL, F](UnitDSL(query))

  def as[A](query: String, field: String, reader: Reader[OrientElement, A]): Free[F, A] =
    Free.inject[SqlDSL, F](As[A](query, field, reader))

  def insertEdge[A](query: String, reader: Reader[OrientElement, A]): Free[F, Id[Edge[A]]] =
    Free.inject[SqlDSL, F](InsertEdge(query, reader))

  def insertVertex[A](query: String, reader: Reader[OrientElement, A]): Free[F, Id[Vertex[A]]] =
    Free.inject[SqlDSL, F](InsertVertex(query, reader))
}

object Sqls {
  def sqls[F[_]](implicit inject: Inject[SqlDSL, F]): Sqls[F] = new Sqls[F]
}
