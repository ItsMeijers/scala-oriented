package oriented

import com.tinkerpop.blueprints.impls.orient.{OrientEdgeType, OrientElementType, OrientVertexType}

/**
  * TODO
  * Extend with all the actions on orientType + create the schema when the types gets created!
  */
trait Type[A] {

  /**
    * A Element Type in an OrientDB
    */
  def orientType: OrientElementType

  /** Add more methods */

}

/**
  * A Vertex Type in an OrientDB
  */
case class VertexType[A](orientType: OrientVertexType) extends Type[A]

/**
  * A Edge Type in an OrientDB
  */
case class EdgeType[A](orientType: OrientEdgeType) extends Type[A]
