package oriented.free.dsl

import cats.free.{Free, Inject}
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

// TODO
case class SaveEdge()

class Edges[F[_]](implicit inject: Inject[EdgeDSL, F]) {

  def getInVertex[A, B](edge: Edge[A], orientFormat: OrientFormat[B]): Free[F, Vertex[B]] =
    Free.inject[EdgeDSL, F](GetInVertex[A, B](edge, orientFormat))

  def getOutVertex[A, B](edge: Edge[A], orientFormat: OrientFormat[B]): Free[F, Vertex[B]] =
    Free.inject[EdgeDSL, F](GetInVertex[A, B](edge, orientFormat))

}

object Edges {
  def edges[F[_]](implicit inject: Inject[EdgeDSL, F]): Edges[F] = new Edges[F]
}