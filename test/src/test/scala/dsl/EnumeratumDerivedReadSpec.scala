package dsl

import enterprisedomain._
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck.{Arbitrary, Gen, Properties}
import oriented.syntax._
import oriented.{InMemoryClient, OrientFormat}

class EnumeratumDerivedReadSpec extends Properties("EnumeratumDerivedReadSpec") {

  property("product with enumeratum type") = forAll { m: Wrapped[WeekDay] => roundTrip(m) }
  property("seq with enumeratum type") = forAll { m: Wrapped[Seq[WeekDay]] => roundTrip(m) }
  property("set with enumeratum type") = forAll { m: Wrapped[Set[WeekDay]] => roundTrip(m) }
  property("list with enumeratum type") = forAll { m: Wrapped[List[WeekDay]] => roundTrip(m) }
  property("vector with enumeratum type") = forAll { m: Wrapped[Vector[WeekDay]] => roundTrip(m) }
  property("map - enumeratum key :: sequence of primitive types") = forAll { m: Wrapped[Map[WeekDay, Seq[Int]]] => roundTrip(m) }
  property("map - enumeratum key :: sequence of product types") = forAll { m: OpeningHours => roundTrip(m) }

  implicit val orientClient = InMemoryClient("EnumeratumDerivedReadSpec")

  def roundTrip[A](value: A)(implicit OF: OrientFormat[A]): Boolean = {
    val prg = for {
      vertex <- orientClient.addVertex(value)
      res <- sql"SELECT FROM ${vertex.orientElement.getIdentity}"
        .vertex[A]
        .unique
    } yield res

    prg.runGraphUnsafe(false).element == value
  }

  implicit val arbUUID = Arbitrary(Gen.uuid)
}


