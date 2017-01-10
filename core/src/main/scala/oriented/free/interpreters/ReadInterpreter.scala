package oriented.free.interpreters

import java.util.Date

import cats.data.Reader
import cats.~>
import com.tinkerpop.blueprints.impls.orient.OrientElement
import oriented.free.dsl._

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * TODO
  */
object ReadInterpreter extends (ReadDSL ~> Reader[OrientElement, ?]) {
  override def apply[A](fa: ReadDSL[A]): Reader[OrientElement, A] = Reader((o: OrientElement) =>
    fa match {
      case Read(a)                    => a
      case ReadCustom(f)  => f(o.getProperties.asScala.toMap)
      case ReadEmbedded(fieldName, of) => of.readerMap.run(o.getProperty[Map[String, Any]](fieldName))
      case ReadList(fieldName, of)    => o.getProperty[List[Map[String, Any]]](fieldName).map(of.readerMap.run)
      case ReadListOpt(fieldName, of) => Try(o.getProperty[List[Map[String, Any]]](fieldName).map(of.readerMap.run)).toOption
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
      case ReadBigDecimalOpt(fieldName)  => Try(o.getProperty[BigDecimal](fieldName)).toOption
      case ReadBinary(fieldName)      => ??? //o.getProperty[](fieldName)
    })
}

/**
  * TODO
  */
object ReadMapInterpreter extends (ReadDSL ~> Reader[Map[String, Any], ?]) {
  override def apply[A](fa: ReadDSL[A]): Reader[Map[String, Any], A] = Reader(map => fa match {
    case Read(a) => a
    case ReadCustom(f) => f(map)
    case ReadEmbedded(fieldName, orientFormat) => orientFormat.readerMap(map(fieldName).asInstanceOf[Map[String, Any]])
    case ReadList(fieldName, orientFormat)     => map(fieldName).asInstanceOf[List[Map[String, Any]]].map(orientFormat.readerMap.run)
    case ReadListOpt(fieldName, orientFormat)  => map.get(fieldName).map(_.asInstanceOf[List[Map[String, Any]]].map(orientFormat.readerMap.run))
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