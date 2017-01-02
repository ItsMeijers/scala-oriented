package maps

import enterprisedomain._
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalacheck.Shapeless._

class OrientDbEnumeratumDerivedReadSpec extends Properties("OrientDbEnumeratumDerivedReadSpec") with OrientDbReadSpec {

  property("product with enumeratum type") = forAll { m: Wrapped[Day] => roundTrip(m) }
  property("seq with enumeratum type") = forAll { m: Wrapped[Seq[Day]] => roundTrip(m) }
  property("set with enumeratum type") = forAll { m: Wrapped[Set[Day]] => roundTrip(m) }
  property("list with enumeratum type") = forAll { m: Wrapped[List[Day]] => roundTrip(m) }
  property("vector with enumeratum type") = forAll { m: Wrapped[Vector[Day]] => roundTrip(m) }
  property("map - enumeratum key :: sequence of primitive types") = forAll { m: Wrapped[Map[Day, Seq[Int]]] => roundTrip(m) }
  property("map - enumeratum key :: sequence of product types") = forAll { m: OpeningHours => roundTrip(m) }
}


