package oriented.syntax

import oriented._
import oriented.free.dsl.UnitDSL
import freek._

/**
  * TODO
  */
case class SQLStatement(statement: String) {

  /**
    * TODO
    */
  def vertex[A](implicit orientFormat: OrientFormat[A]) = new VertexQuery[A](statement, orientFormat)

  /**
    * TODO
    */
  def edge[A](implicit orientFormat: OrientFormat[A]) = new EdgeQuery[A](statement, orientFormat)

  /**
    * TODO
    */
  def update: OrientIO[Unit] = UnitDSL(statement).freek[OrientProgram]

}
