package oriented.free.interpreters

import java.util.Date

import cats.data.Reader
import oriented.free.dsl._
import cats.{Id, ~>}
import com.tinkerpop.blueprints.impls.orient.OrientElement

import scala.util.Try


/**
  * TODO
  */
object ReadInterpreter extends (ReadDSL ~> Reader[OrientElement, ?]) {
  override def apply[A](fa: ReadDSL[A]): Reader[OrientElement, A] = Reader((o: OrientElement) =>
    fa match {
      case Read(a)                    => a
      case ReadBoolean(fieldName)     => o.getProperty[Boolean](fieldName)
      case ReadBooleanOpt(fieldName)  => Try(o.getProperty[Boolean](fieldName)).toOption
      case ReadInt(fieldName)         => o.getProperty[Int](fieldName)
      case ReadIntOpt(fieldName)      => Try(o.getProperty[Int](fieldName)).toOption
      case ReadShort(fieldName)       => o.getProperty[Short](fieldName)
      case ReadShortOpt(fieldName)    => Try(o.getProperty[Short](fieldName)).toOption
      case ReadLong(fieldName)        => o.getProperty[Long](fieldName)
      case ReadLongOpt(fieldName)     => Try(o.getProperty[Long](fieldName)).toOption
      case ReadFloat(fieldName)       => o.getProperty[Float](fieldName)
      case ReadFloatOpt(fieldName)    => Try(o.getProperty[Float](fieldName)).toOption
      case ReadDouble(fieldName)      => o.getProperty[Double](fieldName)
      case ReadDoubleOpt(fieldName)   => Try(o.getProperty[Double](fieldName)).toOption
      case ReadDatetime(fieldName)    => o.getProperty[Date](fieldName)
      case ReadDatetimeOpt(fieldName) => Try(o.getProperty[Date](fieldName)).toOption
      case ReadString(fieldName)      => o.getProperty[String](fieldName)
      case ReadStringOpt(fieldName)   => Try(o.getProperty[String](fieldName)).toOption
      case ReadBigDecimal(fieldName)  => o.getProperty[BigDecimal](fieldName)
      case ReadBigDecimalOpt(fieldName)   => Try(o.getProperty[BigDecimal](fieldName)).toOption
      case ReadEmbedded(c, fieldName)  =>   o.getProperty(fieldName)
      case ReadBinary(fieldName)      => ??? //o.getProperty[](fieldName)
    })
}
