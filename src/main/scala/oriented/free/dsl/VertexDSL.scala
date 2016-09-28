package oriented.free.dsl

import oriented._

/**
  * Algebra for actions performed on the Vertex typeclass.
  */
sealed trait VertexDSL[A]

/**
  * Constructor for the action that adds an Edge to two vertices (in, out), results in an Edge of type B.
  */
case class AddEdgeToVertex[A, B, C](vertex: Vertex[A],
                                    edgeModel: B,
                                    inVertex: Vertex[C],
                                    clusterName: Option[String],
                                    orientFormat: OrientFormat[B]) extends VertexDSL[Edge[B]]

/**
  * Constructor for the action of counting all the edges of a certain Vertex, results in a Long.
  */
case class CountEdges[A, B](vertex: Vertex[A], direction: Direction, orientFormat: OrientFormat[B]) extends VertexDSL[Long]

/**
  * Constructor for the action of retrieving all the edges from a specific vertex, resulting in a List of edges of type C.
  * @param destination only vertices that point to this specific vertex will be retrieved.
  * @param direction only vertices in this direction will be retrieved.
  */
case class GetEdgesDestination[A, B, C](vertex: Vertex[A],
                                        destination: Vertex[B],
                                        direction: Direction,
                                        orientFormat: OrientFormat[C]) extends VertexDSL[List[Edge[C]]]

/**
  * Constructor for the action of retrieving all the edges from a specific vertex, resulting in a List of edges of type B.
  * @param direction only vertices in this direction will be retrieved.
  */
case class GetEdges[A, B](vertex: Vertex[A], direction: Direction, orientFormat: OrientFormat[B]) extends VertexDSL[List[Edge[B]]]

/**
  * Constructor for the action that gets the type of an Vertex, always resulting in a VertexType of type A.
  */
case class GetType[A](vertex: Vertex[A]) extends VertexDSL[VertexType[A]]

/**
  * Constructor for the action of getting al the vertices from another vertex. Results in a List of vertices of type C.
  * @param direction only vertices in this direction will be retrieved.
  * @param formatEdge for retrieving the name/tags of the edge.
  * @param formatVertex for formatting the vertices that result from this action.
  */
case class GetVertices[A, B, C](vertex: Vertex[A], direction: Direction, formatEdge: OrientFormat[B], formatVertex: OrientFormat[C]) extends VertexDSL[List[Vertex[C]]]

// TODO
case class SaveVertex()