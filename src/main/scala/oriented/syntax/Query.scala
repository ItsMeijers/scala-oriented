package oriented.syntax

import cats.Id
import cats.data.NonEmptyList
import oriented._
import oriented.free.dsl._
import freek._

/**
  * Constructs Queries resulting in either a Vertex or Edge.
  */
sealed trait Query[A] {

  type E <: Element[A]

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

}

/**
  * Query Resulting in a Vertex.
  */
class VertexQuery[A](val query: String, val orientFormat: OrientFormat[A]) extends Query[A] {

  type E = Vertex[A]

  def list: OrientIO[List[E]] =
    VertexList[A](query, orientFormat.format)
      .upcast[SqlDSL[List[E]]]
      .freek[OrientProgram]

  def nel: OrientIO[NonEmptyList[E]] =
    VertexNel[A](query, orientFormat.format)
      .upcast[SqlDSL[NonEmptyList[E]]]
      .freek[OrientProgram]

  def option: OrientIO[Option[E]] =
    OptionalVertex[A](query, orientFormat.format)
      .upcast[SqlDSL[Option[E]]]
      .freek[OrientProgram]

  def unique: OrientIO[E] =
    UniqueVertex[A](query, orientFormat.format)
      .upcast[SqlDSL[Id[E]]]
      .freek[OrientProgram]

}

/**
  * Query resulting in an Edge.
  */
class EdgeQuery[A](val query: String, val orientFormat: OrientFormat[A]) extends Query[A] {

  type E = Edge[A]

  def list: OrientIO[List[E]] =
    EdgeList[A](query, orientFormat.format)
      .upcast[SqlDSL[List[E]]]
      .freek[OrientProgram]

  def nel: OrientIO[NonEmptyList[E]] =
    EdgeNel[A](query, orientFormat.format)
      .upcast[SqlDSL[NonEmptyList[E]]]
      .freek[OrientProgram]

  def option: OrientIO[Option[E]] =
    OptionalEdge[A](query, orientFormat.format)
      .upcast[SqlDSL[Option[E]]]
      .freek[OrientProgram]

  def unique: OrientIO[E] =
    UniqueEdge[A](query, orientFormat.format)
      .upcast[SqlDSL[Id[E]]]
      .freek[OrientProgram]

}
