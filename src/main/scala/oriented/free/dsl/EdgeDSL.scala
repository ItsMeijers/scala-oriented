package oriented.free.dsl

import cats.free.{Free, Inject}
import com.tinkerpop.blueprints.impls.orient.OrientEdge
import oriented.{Edge, OrientFormat, Vertex}

/**
  * Algebra for actions performed on the Edge typeclass.
  */
sealed trait EdgeDSL[A]

/**
  * Constructor for action on edge for getting the in vertex, results in a Vertex[B].
  * @param orientFormat for formatting the the Vertex of type B
  */
case class GetInVertex[A, B](edge: Edge[A], orientFormat: OrientFormat[B]) extends EdgeDSL[Vertex[B]]

/**
  * Constructor for action on edge for getting the out vertex, resulting in a Vertex[B].
  * @param orientFormat for formatting the the Vertex of type B
  */
case class GetOutVertex[A, B](edge: Edge[A], orientFormat: OrientFormat[B]) extends EdgeDSL[Vertex[B]]

case class UpdateEdge[A](newModel: A, orientEdge: OrientEdge, orientFormat: OrientFormat[A]) extends EdgeDSL[Edge[A]]

// TODO
case class SaveEdge()

class Edges[F[_]](implicit inject: Inject[EdgeDSL, F]) {

  def getInVertex[A, B](edge: Edge[A], orientFormat: OrientFormat[B]): Free[F, Vertex[B]] =
    Free.inject[EdgeDSL, F](GetInVertex[A, B](edge, orientFormat))

  def getOutVertex[A, B](edge: Edge[A], orientFormat: OrientFormat[B]): Free[F, Vertex[B]] =
    Free.inject[EdgeDSL, F](GetInVertex[A, B](edge, orientFormat))

  def update[A](newModel: A, orientEdge: OrientEdge, orientFormat: OrientFormat[A]): Free[F, Edge[A]] =
    Free.inject[EdgeDSL, F](UpdateEdge(newModel, orientEdge, orientFormat))

}

object Edges {
  def edges[F[_]](implicit inject: Inject[EdgeDSL, F]): Edges[F] = new Edges[F]
}