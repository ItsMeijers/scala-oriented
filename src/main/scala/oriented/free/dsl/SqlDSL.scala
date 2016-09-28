package oriented.free.dsl

import cats.Id
import cats.data.NonEmptyList
import com.tinkerpop.blueprints.impls.orient.OrientElement
import oriented._

/**
  * Algebra for actions that are performed by using the extended SQL language of OrientDB.
  */
sealed trait SqlDSL[A]

/**
  * Constructor for an SQL action resulting into a Unique Vertex of type A.
  */
case class UniqueVertex[A](query: String, f: OrientElement => A) extends SqlDSL[Id[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Unique Edge of type A.
  */
case class UniqueEdge[A](query: String, f: OrientElement => A) extends SqlDSL[Id[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an Optional Vertex of type A.
  */
case class OptionalVertex[A](query: String, f: OrientElement => A) extends SqlDSL[Option[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Optional Edge of type A.
  */
case class OptionalEdge[A](query: String, f: OrientElement => A) extends SqlDSL[Option[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an List of vertices of type A.
  */
case class VertexList[A](query: String, f: OrientElement => A) extends SqlDSL[List[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an List of edges of type A.
  */
case class EdgeList[A](query: String, f: OrientElement => A) extends SqlDSL[List[Edge[A]]]

/**
  * Constructor for an SQL action resulting in an Non Empty List of vertices of type A.
  */
case class VertexNel[A](query: String, f: OrientElement => A) extends SqlDSL[NonEmptyList[Vertex[A]]]

/**
  * Constructor for an SQL action resulting in an Non Empty list of edges of type A.
  */
case class EdgeNel[A](query: String, f: OrientElement => A) extends SqlDSL[NonEmptyList[Edge[A]]]

/**
  * Constructor for an SQL action resulting in Unit.
  */
case class UnitDSL(query: String) extends SqlDSL[Unit]
