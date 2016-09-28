package oriented

import com.orientechnologies.orient.core.id.ORID
import com.tinkerpop.blueprints.impls.orient.{OrientEdge, OrientElement, OrientVertex}
import oriented.free.dsl._
import oriented.syntax.{OrientIO, OrientProgram}
import freek._

/**
  * Element interface for typeclasses Vertex and Edge.
  */
sealed trait Element[A] {

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
  def getBaseClassName: OrientIO[String] =
    GetBaseClassName[A](this)
      .upcast[ElementDSL[String]]
      .freek[OrientProgram]

  /**
    * Constructor for OrientIO action of retrieving the element type in String.
    */
  def getElementType: OrientIO[String] =
    GetElementType(this)
      .upcast[ElementDSL[String]]
      .freek[OrientProgram]

  // TODO
  //  def getGraph = ???

  /**
    * Constructor for OrientIO action of retrieving the ORID.
    */
  def getIdentity: OrientIO[ORID] =
    GetIdentity(this)
      .upcast[ElementDSL[ORID]]
      .freek[OrientProgram]

  // TODO
  //  def getId = ???

  /**
    * Constructor for OrientIO action of retrieving the label (will be the same as the classname unless multiple labels
    * are assigned to the vertex).
    */
  def getLabel: OrientIO[String] =
    GetLabel(this)
      .upcast[ElementDSL[String]]
      .freek[OrientProgram]

  // TODO
  //  def getRecord = ???

//  def save(a: A)(implicit orientFormat: OrientFormat[A]): OrientAction[Element[A]] =
//    SaveElement(this, a, orientFormat)
//      .upcast[GraphElementDSL[Element[A]]]
//      .freek[OrientProgram]

  /**
    * Constructor for OrientIO action of deleting an element from OrientDB.
    */
  def remove: OrientIO[Unit] =
    RemoveElement(this)
      .upcast[ElementDSL[Unit]]
      .freek[OrientProgram]

}

/**
  * TODO
  */
case class Vertex[A](element: A, orientElement: OrientVertex) extends Element[A] {

  def addEdge[B, C](edgeModel: B,
                    inVertex: Vertex[C],
                    clusterName: Option[String] = None)
                   (implicit orientFormat: OrientFormat[B]): OrientIO[Edge[B]] =

    AddEdgeToVertex(this, edgeModel, inVertex, clusterName, orientFormat)
      .upcast[VertexDSL[Edge[B]]]
      .freek[OrientProgram]

  def countEdges[B](direction: Direction)(implicit orientFormat: OrientFormat[B]) : OrientIO[Long] =
    CountEdges(this, direction, orientFormat)
      .upcast[VertexDSL[Long]]
      .freek[OrientProgram]

  // How to handle the types?
  def getEdges[B, C](destination: Vertex[B],
                     direction: Direction)
                    (implicit orientFormat: OrientFormat[C]): OrientIO[List[Edge[C]]] =
  GetEdgesDestination(this, destination, direction, orientFormat)
    .upcast[VertexDSL[List[Edge[C]]]]
    .freek[OrientProgram]

  def getEdges[B](direction: Direction)(implicit orientFormat: OrientFormat[B]): OrientIO[List[Edge[B]]] =
    GetEdges(this, direction, orientFormat)
      .upcast[VertexDSL[List[Edge[B]]]]
      .freek[OrientProgram]

  def getType: OrientIO[VertexType[A]] =
    GetType(this)
      .upcast[VertexDSL[VertexType[A]]]
      .freek[OrientProgram]
//
//  // How to handle the types?
  def getVertices[E, V](direction: Direction)(implicit formatEdge: OrientFormat[E], formatVertex: OrientFormat[V]): OrientIO[List[Vertex[V]]] =
    GetVertices(this, direction, formatEdge, formatVertex)
      .upcast[VertexDSL[List[Vertex[V]]]]
      .freek[OrientProgram]
//
//  // TODO orientElement.moveTo()
//
//  // TODO orientElement.query

}

/**
  * TODO
  */
case class Edge[A](element: A, orientElement: OrientEdge) extends Element[A] {

  def getInVertex[B](implicit orientFormat: OrientFormat[B]): OrientIO[Vertex[B]] =
    GetInVertex(this, orientFormat)
      .upcast[EdgeDSL[Vertex[B]]]
      .freek[OrientProgram]

  def getOutVertex[B](implicit orientFormat: OrientFormat[B]): OrientIO[Vertex[B]] =
    GetOutVertex(this, orientFormat)
      .upcast[EdgeDSL[Vertex[B]]]
      .freek[OrientProgram]

  def getVertices[In, Out](implicit inFormat: OrientFormat[In], outFormat: OrientFormat[Out]): OrientIO[(Vertex[In], Vertex[Out])] =
    for {
      inVertex <- getInVertex(inFormat)
      outVertex <- getOutVertex(outFormat)
    } yield (inVertex, outVertex)

}
