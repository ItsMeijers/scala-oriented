package oriented.syntax

import cats.data.NonEmptyList
import oriented._
import oriented.free.dsl._

/**
  * Constructs Queries resulting in either a Vertex or Edge.
  */
sealed trait Query[A] {

  type E <: Element[A]

  def S: Sqls[OrientProgram]

  /**
    * The extended SQL string that forms the query.
    */
  def query: String

  /**
    * orientFormat instance for A needed to format OrientElement to A.
    */
  def orientFormat: OrientFormat[A]

  /**
    * List filled with Elements.
    */
  def list: OrientIO[List[E]]

  /**
    * Non Empty List filled with Elements.
    */
  def nel: OrientIO[NonEmptyList[E]]

  /**
    * Optional Element
    */
  def option: OrientIO[Option[E]]

  /**
    * One and exactly one element.
    */
  def unique: OrientIO[E]

  /**
    * Insert query resulting in a single E
    */
  def insert: OrientIO[E]

}

/**
  * Query Resulting in a Vertex.
  */
class VertexQuery[A](val query: String, val orientFormat: OrientFormat[A])(implicit val S: Sqls[OrientProgram]) extends Query[A] {

  import S._

  type E = Vertex[A]

  def list: OrientIO[List[E]] =
    vertexList[A](query, orientFormat)

  def nel: OrientIO[NonEmptyList[E]] =
    vertexNel[A](query, orientFormat)

  def option: OrientIO[Option[E]] =
    optionalVertex[A](query, orientFormat)

  def unique: OrientIO[E] =
    uniqueVertex[A](query, orientFormat)

  def insert: OrientIO[E] =
    insertVertex[A](query, orientFormat)

}

/**
  * Query resulting in an Edge.
  */
class EdgeQuery[A](val query: String, val orientFormat: OrientFormat[A])(implicit val S: Sqls[OrientProgram]) extends Query[A] {

  import S._

  type E = Edge[A]

  def list: OrientIO[List[E]] =
    edgeList[A](query, orientFormat)

  def nel: OrientIO[NonEmptyList[E]] =
    edgeNel[A](query, orientFormat)

  def option: OrientIO[Option[E]] =
    optionalEdge[A](query, orientFormat)

  def unique: OrientIO[E] =
    uniqueEdge[A](query, orientFormat)

  def insert: OrientIO[E] =
    insertEdge[A](query, orientFormat)

}
