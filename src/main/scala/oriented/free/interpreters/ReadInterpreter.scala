package oriented.free.interpreters

import java.util.Date

import cats.data.Reader
import oriented.free.dsl._
import cats.{Id, ~>}
import com.tinkerpop.blueprints.impls.orient.OrientElement


/**
  * TODO
  */
object ReadInterpreter extends (ReadDSL ~> Reader[OrientElement, ?]) {
  override def apply[A](fa: ReadDSL[A]): Reader[OrientElement, A] = Reader((o: OrientElement) =>
    fa match {
      case Read(a)                 => a
      case ReadBoolean(fieldName)  => o.getProperty[Boolean](fieldName)
      case ReadInt(fieldName)      => o.getProperty[Int](fieldName)
      case ReadShort(fieldName)    => o.getProperty[Short](fieldName)
      case ReadLong(fieldName)     => o.getProperty[Long](fieldName)
      case ReadFloat(fieldName)    => o.getProperty[Float](fieldName)
      case ReadDouble(fieldName)   => o.getProperty[Double](fieldName)
      case ReadDatetime(fieldName) => o.getProperty[Date](fieldName)
      case ReadString(fieldName)   => o.getProperty[String](fieldName)
      case ReadBinary(fieldName)   => ??? //o.getProperty[](fieldName)
    })
}
