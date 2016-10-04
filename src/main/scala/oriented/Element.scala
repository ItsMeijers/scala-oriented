package oriented

import com.orientechnologies.orient.core.id.ORID
import com.tinkerpop.blueprints.impls.orient.{OrientEdge, OrientElement, OrientVertex}
import oriented.free.dsl._
import oriented.syntax.{OrientIO, OrientProgram}

/**
  * Element interface for typeclasses Vertex and Edge.
  */
sealed trait Element[A] {

  val E: Elements[OrientProgram] = Elements.elements[OrientProgram]

  /**
    * The model of the Element.
    */
  def element: A

  /**
    * To OrientElement of the Element.
    * Thi is the actual value retrieved and wrapped form OrientDB.
    */
  def orientElement: OrientElement

  /**
    * Constructor for OrientIO action of retrieving the base class name.
    */
  def getBaseClassName: OrientIO[String] = E.getBaseClassName(this)

  /**
    * Constructor for OrientIO action of retrieving the element type in String.
    */
  def getElementType: OrientIO[String] = E.getElementType(this)

  // TODO
  //  def getGraph = ???

  /**
    * Constructor for OrientIO action of retrieving the ORID.
    */
  def getIdentity: OrientIO[ORID] = E.getIdentity(this)

  // TODO
  //  def getId = ???

  /**
    * Constructor for OrientIO action of retrieving the label (will be the same as the classname unless multiple labels
    * are assigned to the vertex).
    */
  def getLabel: OrientIO[String] = E.getLabel(this)

  // TODO
  //  def getRecord = ???

//  def save(a: A)(implicit orientFormat: OrientFormat[A]): OrientAction[Element[A]] =
//    SaveElement(this, a, orientFormat)
//      .upcast[GraphElementDSL[Element[A]]]
//      .freek[OrientProgram]

  /**
    * Constructor for OrientIO action of deleting an element from OrientDB.
    */
  def remove: OrientIO[Unit] = E.removeElement(this)

}

/**
  * TODO
  */
case class Vertex[A](element: A, orientElement: OrientVertex) extends Element[A] {

  val V: Vertices[OrientProgram] = Vertices.vertices[OrientProgram]

  def addEdge[B, C](edgeModel: B,
                    inVertex: Vertex[C],
                    clusterName: Option[String] = None)
                   (implicit orientFormat: OrientFormat[B]): OrientIO[Edge[B]] =
    V.addEdgeToVertex(this, edgeModel, inVertex, clusterName, orientFormat)

  def countEdges[B](direction: Direction)(implicit orientFormat: OrientFormat[B]) : OrientIO[Long] =
    V.countEdges(this, direction, orientFormat)

  def getEdges[B, C](destination: Vertex[B],
                     direction: Direction)
                    (implicit orientFormat: OrientFormat[C]): OrientIO[List[Edge[C]]] =
    V.getEdgesDestination(this, destination, direction, orientFormat)

  def getEdges[B](direction: Direction)(implicit orientFormat: OrientFormat[B]): OrientIO[List[Edge[B]]] =
    V.getEdges(this, direction, orientFormat)

  def getType: OrientIO[VertexType[A]] = V.getType(this)

  def getVertices[E, V](direction: Direction)
                       (implicit formatEdge: OrientFormat[E],
                        formatVertex: OrientFormat[V]): OrientIO[List[Vertex[V]]] =
    V.getVertices(this, direction, formatEdge, formatVertex)

  def update(newModel: A)(implicit orientFormat: OrientFormat[A]): OrientIO[Vertex[A]] =
    V.update(newModel, orientElement, orientFormat)
}

/**
  * TODO
  */
case class Edge[A](element: A, orientElement: OrientEdge) extends Element[A] {

  val Ed: Edges[OrientProgram] = Edges.edges[OrientProgram]

  def getInVertex[B](implicit orientFormat: OrientFormat[B]): OrientIO[Vertex[B]] =
    Ed.getInVertex(this, orientFormat)

  def getOutVertex[B](implicit orientFormat: OrientFormat[B]): OrientIO[Vertex[B]] =
    Ed.getOutVertex(this, orientFormat)

  def getVertices[In, Out](implicit inFormat: OrientFormat[In], outFormat: OrientFormat[Out]): OrientIO[(Vertex[In], Vertex[Out])] =
    for {
      inVertex <- getInVertex(inFormat)
      outVertex <- getOutVertex(outFormat)
    } yield (inVertex, outVertex)

  def update(newModel: A)(implicit orientFormat: OrientFormat[A]): OrientIO[Edge[A]] =
    Ed.update(newModel, orientElement, orientFormat)

}
