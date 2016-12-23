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
object ReadMapInterpreter extends (ReadDSL ~> Reader[scala.collection.Map[String, Any], ?]) {
  override def apply[A](fa: ReadDSL[A]): Reader[scala.collection.Map[String, Any], A] = Reader(map => fa match {
    case Read(a) => a
    case ReadEmbedded(fieldName, orientFormat) => orientFormat.readerMap(map(fieldName).asInstanceOf[scala.collection.Map[String, Any]])
    case ReadList(fieldName, orientFormat)     => map(fieldName).asInstanceOf[List[scala.collection.Map[String, Any]]].map(orientFormat.readerMap.run)
    case ReadListOpt(fieldName, orientFormat)  => map.get(fieldName).map(_.asInstanceOf[List[scala.collection.Map[String, Any]]].map(orientFormat.readerMap.run))
    case ReadBoolean(fieldName)                => map(fieldName).asInstanceOf[Boolean]
    case ReadBooleanOpt(fieldName)             => map.get(fieldName).map(_.asInstanceOf[Boolean])
    case ReadInt(fieldName)                    => map(fieldName).asInstanceOf[Int]
    case ReadIntOpt(fieldName)                 => map.get(fieldName).map(_.asInstanceOf[Int])
    case ReadShort(fieldName)                  => map(fieldName).asInstanceOf[Short]
    case ReadShortOpt(fieldName)               => map.get(fieldName).map(_.asInstanceOf[Short])
    case ReadLong(fieldName)                   => map(fieldName).asInstanceOf[Long]
    case ReadLongOpt(fieldName)                => map.get(fieldName).map(_.asInstanceOf[Long])
    case ReadFloat(fieldName)                  => map(fieldName).asInstanceOf[Float]
    case ReadFloatOpt(fieldName)               => map.get(fieldName).map(_.asInstanceOf[Float])
    case ReadDouble(fieldName)                 => map(fieldName).asInstanceOf[Double]
    case ReadDoubleOpt(fieldName)              => map.get(fieldName).map(_.asInstanceOf[Double])
    case ReadDatetime(fieldName)               => map(fieldName).asInstanceOf[Date]
    case ReadDatetimeOpt(fieldName)            => map.get(fieldName).map(_.asInstanceOf[Date])
    case ReadString(fieldName)                 => map(fieldName).asInstanceOf[String]
    case ReadStringOpt(fieldName)              => map.get(fieldName).map(_.asInstanceOf[String])
    case ReadBinary(fieldName)                 => ???
    case ReadBigDecimal(fieldName)             => map(fieldName).asInstanceOf[BigDecimal]
    case ReadBigDecimalOpt(fieldName)          => map.get(fieldName).map(_.asInstanceOf[BigDecimal])
  })
}

case class ReaderInterpreterG[A, B](a: A, get: A => B)
