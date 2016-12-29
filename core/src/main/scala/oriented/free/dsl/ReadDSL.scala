package oriented.free.dsl

import java.util.Date

import cats.free.{Free, Inject}
import oriented.OrientFormat

/**
  * DSL for reading OrientElements to a specific A.
  */
sealed trait ReadDSL[A]

/**
  * Pure action to insert A into ReadDSL.
  * Usage: case classes with no fields or objects.
  */
case class Read[A](a: A) extends ReadDSL[A]

case class ReadCustom[A](f: Map[String, Any] => A) extends ReadDSL[A]

case class ReadEmbedded[T](fieldName: String, orientFormat: OrientFormat[T]) extends ReadDSL[T]

case class ReadList[T](fieldName: String, orientFormat: OrientFormat[T]) extends ReadDSL[List[T]]

case class ReadListOpt[T](fieldName: String, orientFormat: OrientFormat[T]) extends ReadDSL[Option[List[T]]]

case class ReadBoolean(fieldName: String) extends ReadDSL[Boolean]

case class ReadBooleanOpt(fieldName: String) extends ReadDSL[Option[Boolean]]

case class ReadInt(fieldName: String) extends ReadDSL[Int]

case class ReadIntOpt(fieldName: String) extends ReadDSL[Option[Int]]

case class ReadShort(fieldName: String) extends ReadDSL[Short]

case class ReadShortOpt(fieldName: String) extends ReadDSL[Option[Short]]

case class ReadLong(fieldName: String) extends ReadDSL[Long]

case class ReadLongOpt(fieldName: String) extends ReadDSL[Option[Long]]

case class ReadFloat(fieldName: String) extends ReadDSL[Float]

case class ReadFloatOpt(fieldName: String) extends ReadDSL[Option[Float]]

case class ReadDouble(fieldName: String) extends ReadDSL[Double]

case class ReadDoubleOpt(fieldName: String) extends ReadDSL[Option[Double]]

case class ReadDatetime(fieldName: String) extends ReadDSL[Date]

case class ReadDatetimeOpt(fieldName: String) extends ReadDSL[Option[Date]]

case class ReadString(fieldName: String) extends ReadDSL[String]

case class ReadStringOpt(fieldName: String) extends ReadDSL[Option[String]]

case class ReadBinary(fieldName: String) extends ReadDSL[List[Byte]]

case class ReadBigDecimal(fieldName: String) extends ReadDSL[BigDecimal]

case class ReadBigDecimalOpt(fieldName: String) extends ReadDSL[Option[BigDecimal]]

class Reads[F[_]](implicit inject: Inject[ReadDSL, F]) {

  def read[R](r: R): Free[F, R] =
    Free.inject[ReadDSL, F](Read(r))

  def readCustom[R](r: Map[String, Any] => R): Free[F, R] =
    Free.inject[ReadDSL, F](ReadCustom(r))

  def readEmbedded[T](fieldName: String, orientFormat: OrientFormat[T]): Free[F, T] =
    Free.inject[ReadDSL, F](ReadEmbedded(fieldName, orientFormat))

  def readList[T](fieldName: String, orientFormat: OrientFormat[T]): Free[F, List[T]] =
    Free.inject[ReadDSL, F](ReadList(fieldName, orientFormat))

  def readListOpt[T](fieldName: String, orientFormat: OrientFormat[T]): Free[F, Option[List[T]]] =
    Free.inject[ReadDSL, F](ReadListOpt(fieldName, orientFormat))

  def readBoolean(fieldName: String): Free[F, Boolean] =
    Free.inject[ReadDSL, F](ReadBoolean(fieldName))

  def readBooleanOpt(fieldName: String): Free[F, Option[Boolean]] =
    Free.inject[ReadDSL, F](ReadBooleanOpt(fieldName))

  def readInt(fieldName: String): Free[F, Int] =
    Free.inject[ReadDSL, F](ReadInt(fieldName))

  def readIntOpt(fieldName: String): Free[F, Option[Int]] =
    Free.inject[ReadDSL, F](ReadIntOpt(fieldName))

  def readShort(fieldName: String): Free[F, Short] =
    Free.inject[ReadDSL, F](ReadShort(fieldName))

  def readShortOpt(fieldName: String): Free[F, Option[Short]] =
    Free.inject[ReadDSL, F](ReadShortOpt(fieldName))

  def readLong(fieldName: String): Free[F, Long] =
    Free.inject[ReadDSL, F](ReadLong(fieldName))

  def readLongOpt(fieldName: String): Free[F, Option[Long]] =
    Free.inject[ReadDSL, F](ReadLongOpt(fieldName))

  def readFloat(fieldName: String): Free[F, Float] =
    Free.inject[ReadDSL, F](ReadFloat(fieldName))

  def readFloatOpt(fieldName: String): Free[F, Option[Float]] =
    Free.inject[ReadDSL, F](ReadFloatOpt(fieldName))

  def readDouble(fieldName: String): Free[F, Double] =
    Free.inject[ReadDSL, F](ReadDouble(fieldName))

  def readDoubleOpt(fieldName: String): Free[F, Option[Double]] =
    Free.inject[ReadDSL, F](ReadDoubleOpt(fieldName))

  def readDatetime(fieldName: String): Free[F, Date] =
    Free.inject[ReadDSL, F](ReadDatetime(fieldName))

  def readDatetimeOpt(fieldName: String): Free[F, Option[Date]] =
    Free.inject[ReadDSL, F](ReadDatetimeOpt(fieldName))

  def readString(fieldName: String): Free[F, String] =
    Free.inject[ReadDSL, F](ReadString(fieldName))

  def readStringOpt(fieldName: String): Free[F, Option[String]] =
    Free.inject[ReadDSL, F](ReadStringOpt(fieldName))

  def readBinary(fieldName: String): Free[F, List[Byte]] =
    Free.inject[ReadDSL, F](ReadBinary(fieldName))

  def readBigDecimal(fieldName: String): Free[F, BigDecimal] =
    Free.inject[ReadDSL, F](ReadBigDecimal(fieldName))

  def readBigDecimalOpt(fieldName: String): Free[F, Option[BigDecimal]] =
    Free.inject[ReadDSL, F](ReadBigDecimalOpt(fieldName))
}

object Reads {
  def reads[F[_]](implicit inject: Inject[ReadDSL, F]): Reads[F] = new Reads[F]
}
