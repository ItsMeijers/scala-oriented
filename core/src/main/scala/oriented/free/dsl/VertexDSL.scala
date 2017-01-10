package oriented.free.dsl

import cats.free.{Free, Inject}
import com.tinkerpop.blueprints.impls.orient.{OrientElement, OrientVertex}
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
case class GetVertices[A, B, C](vertex: Vertex[A],
                                direction: Direction,
                                formatEdge: OrientFormat[B],
                                formatVertex: OrientFormat[C]) extends VertexDSL[List[Vertex[C]]]

case class UpdateVertex[A](newModel: A, orientVertex: OrientVertex, orientFormat: OrientFormat[A]) extends VertexDSL[Vertex[A]]

// TODO
case class SaveVertex()

class Vertices[F[_]](implicit inject: Inject[VertexDSL, F]) {

  def addEdgeToVertex[A, B, C](vertex: Vertex[A],
                               edgeModel: B,
                               inVertex: Vertex[C],
                               clusterName: Option[String],
                               orientFormat: OrientFormat[B]): Free[F, Edge[B]] =
    Free.inject[VertexDSL, F](AddEdgeToVertex[A, B, C](vertex, edgeModel, inVertex, clusterName, orientFormat))

  def countEdges[A, B](vertex: Vertex[A], direction: Direction, orientFormat: OrientFormat[B]): Free[F, Long] =
    Free.inject[VertexDSL, F](CountEdges(vertex, direction, orientFormat))

  def getEdgesDestination[A, B, C](vertex: Vertex[A],
                                   destination: Vertex[B],
                                   direction: Direction,
                                   orientFormat: OrientFormat[C]): Free[F, List[Edge[C]]] =
    Free.inject[VertexDSL, F](GetEdgesDestination(vertex, destination, direction, orientFormat))

  def getEdges[A, B](vertex: Vertex[A], direction: Direction, orientFormat: OrientFormat[B]): Free[F, List[Edge[B]]] =
    Free.inject[VertexDSL, F](GetEdges(vertex, direction, orientFormat))

  def getType[A](vertex: Vertex[A]): Free[F, VertexType[A]] =
    Free.inject[VertexDSL, F](GetType(vertex))

  def getVertices[A, B, C](vertex: Vertex[A],
                           direction: Direction,
                           formatEdge: OrientFormat[B],
                           formatVertex: OrientFormat[C]): Free[F, List[Vertex[C]]] =
    Free.inject[VertexDSL, F](GetVertices(vertex, direction, formatEdge, formatVertex))

  def update[A](newModel: A, orientVertex: OrientVertex, orientFormat: OrientFormat[A]) =
    Free.inject[VertexDSL, F](UpdateVertex(newModel, orientVertex, orientFormat))

}

object Vertices {
  def vertices[F[_]](implicit inject: Inject[VertexDSL, F]): Vertices[F] = new Vertices[F]
}