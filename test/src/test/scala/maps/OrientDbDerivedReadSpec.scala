package maps

import java.util.Date

import enterprisedomain._
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck._

class OrientDbDerivedReadSpec extends Properties("OrientDbDerivedReadSpec") with OrientDbReadSpec {

  property("int") = forAll { m: Wrapped[Int] => roundTrip(m) }
  property("long") = forAll { m: Wrapped[Long] => roundTrip(m) }
  property("bigDecimal") = forAll { m: Wrapped[BigDecimal] => roundTrip(m) }
  property("float") = forAll { m: Wrapped[Float] => roundTrip(m) }
  property("double") = forAll { m: Wrapped[Double] => roundTrip(m) }
  property("short") = forAll { m: Wrapped[Short] => roundTrip(m) }
  property("date") = forAll { m: Wrapped[Date] => roundTrip(m) }
  property("string") = forAll { m: Wrapped[String] => roundTrip(m) }

  property("seq - primitives") = forAll { m: Wrapped[Seq[Int]] => roundTrip(m) }
  property("seq - products") = forAll { m: Wrapped[Seq[Wrapped[Int]]] => roundTrip(m) }
  property("seq - coproducts") = forAll { m: Wrapped[Seq[LastReservableTime]] => roundTrip(m) }

  property("option - primitives") = forAll { m: Wrapped[Option[Int]] => roundTrip(m) }
  property("option - products") = forAll { m: Wrapped[Option[Wrapped[Int]]] => roundTrip(m) }
  property("option - coproducts") = forAll { m: Wrapped[Option[LastReservableTime]] => roundTrip(m) }

  property("map - primitives") = forAll { m: Wrapped[Map[String, Int]] => roundTrip(m) }
  property("map - products") = forAll { m: Wrapped[Map[String, Wrapped[Int]]] => roundTrip(m) }
  property("map - coproducts") = forAll { m: Wrapped[Map[String, LastReservableTime]] => roundTrip(m) }

  property("map - seq :: primitives") = forAll { m: Wrapped[Map[String, Seq[Int]]] => roundTrip(m) }
  property("map - seq :: products") = forAll { m: Wrapped[Map[String, Seq[Wrapped[Int]]]] => roundTrip(m) }
  property("map - seq :: coproducts") = forAll { m: Wrapped[Map[String, Seq[LastReservableTime]]] => roundTrip(m) }

  property("coproducts - primitives") = forAll { m: Wrapped[Tree[Int]] => roundTrip(m) }
  property("coproducts - products") = forAll { m: Wrapped[Tree[Wrapped[Int]]] => roundTrip(m) }
  property("coproducts - seq") = forAll { m: Wrapped[Tree[Seq[Int]]] => roundTrip(m) }
  property("coproducts - option") = forAll { m: Wrapped[Tree[Option[Int]]] => roundTrip(m) }
  property("coproducts - map :: primitives") = forAll { m: Wrapped[Tree[Map[String, Int]]] => roundTrip(m) }
  property("coproducts - map :: products") = forAll { m: Wrapped[Tree[Map[String, Wrapped[Int]]]] => roundTrip(m) }
  property("coproducts - map :: coproducts") = forAll { m: Wrapped[Tree[Map[String, LastReservableTime]]] => roundTrip(m) }

  property("complex nested domain model should work") = forAll { m: BookingCondition => roundTrip(m) }

  implicit val arbUUID = Arbitrary(Gen.uuid)
}
