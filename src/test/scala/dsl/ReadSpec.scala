package dsl

import java.util.Date

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import org.scalacheck.Prop._
import org.scalacheck.Shapeless._
import org.scalatest._
import oriented.syntax._
import oriented.{InMemoryClient, OrientFormat}

case class Meta(date: Date, user: String)
case class GenericValue[T](value: T)

class OrientFormatTests extends PropSpec {
  implicit val orientClient = InMemoryClient("test")

  property("int")(roundTrip[Int])
  property("double")(roundTrip[Double])
  property("long")(roundTrip[Long])
  property("bigdecimal")(roundTrip[BigDecimal])
  property("date")(roundTrip[Date])
  property("string")(roundTrip[String])
  property("option")(roundTrip[Option[Int]])
  property("embedded")(roundTrip[Meta])
  property("list objects")(roundTrip[List[Meta]])

  def roundTrip[T : Arbitrary](implicit F: OrientFormat[GenericValue[T]]) = forAll { (a: T) =>
    val value = GenericValue(a)
    val result = orientClient.addVertex(value).runGraphUnsafe(enableTransactions = false)
    val payload = sql"SELECT FROM ${result.orientElement.getIdentity}"
      .vertex[GenericValue[T]]
      .unique
      .runGraphUnsafe(enableTransactions = true)

    payload.element == value
  }
}