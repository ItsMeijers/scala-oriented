package oriented.free.dsl

import java.util.Date

import cats.free.{Free, Inject}

/**
  * DSL for reading OrientElements to a specific A
  */
sealed trait ReadDSL[A]

case class ReadBoolean(fieldName: String) extends ReadDSL[Boolean]

case class ReadInt(fieldName: String) extends ReadDSL[Int]

case class ReadShort(fieldName: String) extends ReadDSL[Short]

case class ReadLong(fieldName: String) extends ReadDSL[Long]

case class ReadFloat(fieldName: String) extends ReadDSL[Float]

case class ReadDouble(fieldName: String) extends ReadDSL[Double]

case class ReadDatetime(fieldName: String) extends ReadDSL[Date]

case class ReadString(fieldName: String) extends ReadDSL[String]

case class ReadBinary(fieldName: String) extends ReadDSL[List[Byte]]

class Reads[F[_]](implicit inject: Inject[ReadDSL, F]) {

  def readBoolean(fieldName: String): Free[F, Boolean] =
    Free.inject[ReadDSL, F](ReadBoolean(fieldName))

  def readInt(fieldName: String): Free[F, Int] =
    Free.inject[ReadDSL, F](ReadInt(fieldName))

  def readShort(fieldName: String): Free[F, Short] =
    Free.inject[ReadDSL, F](ReadShort(fieldName))

  def readLong(fieldName: String): Free[F, Long] =
    Free.inject[ReadDSL, F](ReadLong(fieldName))

  def readFloat(fieldName: String): Free[F, Float] =
    Free.inject[ReadDSL, F](ReadFloat(fieldName))

  def readDouble(fieldName: String): Free[F, Double] =
    Free.inject[ReadDSL, F](ReadDouble(fieldName))

  def readDatetime(fieldName: String): Free[F, Date] =
    Free.inject[ReadDSL, F](ReadDatetime(fieldName))

  def readString(fieldName: String): Free[F, String] =
    Free.inject[ReadDSL, F](ReadString(fieldName))

  def readBinary(fieldName: String): Free[F, List[Byte]] =
    Free.inject[ReadDSL, F](ReadBinary(fieldName))

}

object Reads {
  def reads[F[_]](implicit inject: Inject[ReadDSL, F]): Reads[F] = new Reads[F]
}
