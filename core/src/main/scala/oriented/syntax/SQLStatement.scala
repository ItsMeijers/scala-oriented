package oriented.syntax

import cats.data.Reader
import oriented._
import oriented.free.dsl.Sqls._

/**
  * TODO
  */
case class SQLStatement(query: String) {

  implicit def sql = sqls[OrientProgram]

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
  def update: OrientIO[Unit] = sql.unit(query)

  /**
    * TODO
    */
  def as[A](field: String): OrientIO[A] = sql.as[A](query, field, Reader(_.getProperty[A](field)))


}
