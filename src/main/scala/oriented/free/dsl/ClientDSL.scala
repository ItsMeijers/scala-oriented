package oriented.free.dsl

import oriented._

/**
  * Algebra for actions on the client that provides a graph instance.
  */
sealed trait ClientDSL[A]

/**
  * Constructor for creating a vertex type, action results into a VertexType[A].
  */
case class CreateVertexType[A](orientFormat: OrientFormat[A]) extends ClientDSL[VertexType[A]]

/**
  * Constructor for creating a edge type, action results into a EdgeType[A].
  */
case class CreateEdgeType[A](orientFormat: OrientFormat[A]) extends ClientDSL[EdgeType[A]]

/**
  * Constructor for adding a vertex, action results into a Vertex[A].
  * @param vertexModel forms the model that needs to be saved as a Vertex.
  * @param orientFormat is an instance of the orientFormat typeclass for A.
  */
case class AddVertex[A](vertexModel: A, orientFormat: OrientFormat[A]) extends ClientDSL[Vertex[A]]

/**
  * Constructor for adding a vertex, action results into a Edge[A].
  * @param edgeModel model that will be saved as an Edge (needs to have an orientFormat instance).
  */
case class AddEdge[A, B, C](edgeModel: A, inVertex: Vertex[B], outVertex: Vertex[C], orientFormat: OrientFormat[A]) extends ClientDSL[Edge[A]]
