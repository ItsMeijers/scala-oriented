package oriented.syntax

import oriented._
import oriented.free.dsl.Sqls._

/**
  * TODO
  */
case class SQLStatement(query: String) {

  implicit def sqlsId = sqls[OrientProgram]

  /**
    * TODO
    */
  def vertex[A](implicit orientFormat: OrientFormat[A]) = new VertexQuery[A](query, orientFormat)

  /**
    * TODO
    */
  def edge[A](implicit orientFormat: OrientFormat[A]) = new EdgeQuery[A](query, orientFormat)

  /**
    * TODO
    */
  def update: OrientIO[Unit] = sqlsId.unit(query)


}
