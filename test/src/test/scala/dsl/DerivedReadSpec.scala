package dsl

import java.util.{Date, UUID}

import cats.data.NonEmptyList
import enterprisedomain._
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck.{Arbitrary, Gen, Properties}
import oriented.maps.{BaseMappableType, FromMappable, ToMappable}
import oriented.syntax._
import oriented.{InMemoryClient, OrientFormat}
import oriented.maps.scalaMap._

class DerivedReadSpec extends Properties("DerivedReadSpec") {

  implicit val dayMapping = string.xmap(Day.fromString)(_.str)


  property("int") = forAll { m: Wrapped[Int] => roundTrip(m) }
  property("long") = forAll { m: Wrapped[Long] => roundTrip(m) }
  property("bigDecimal") = forAll { m: Wrapped[BigDecimal] => roundTrip(m) }
  property("float") = forAll { m: Wrapped[Float] => roundTrip(m) }
  property("double") = forAll { m: Wrapped[Double] => roundTrip(m) }
  property("short") = forAll { m: Wrapped[Short] => roundTrip(m) }
  property("date") = forAll { m: Wrapped[Date] => roundTrip(m) }
  property("string") = forAll { m: Wrapped[String] => roundTrip(m) }

  property("xmap") = forAll { m: Wrapped[Day] => roundTrip(m) }

  property("list - primitives") = forAll { m: Wrapped[List[Int]] => roundTrip(m) }
  property("list - products") = forAll { m: Wrapped[List[Wrapped[Int]]] => roundTrip(m) }
  property("list - coproducts") = forAll { m: Wrapped[List[Tree[Int]]] => roundTrip(m) }

  property("set - primitives") = forAll { m: Wrapped[Set[Int]] => roundTrip(m) }
  property("set - products") = forAll { m: Wrapped[Set[Wrapped[Int]]] => roundTrip(m) }
  property("set - coproducts") = forAll { m: Wrapped[Set[Tree[Int]]] => roundTrip(m) }

  property("seq - primitives") = forAll { m: Wrapped[Seq[Int]] => roundTrip(m) }
  property("seq - products") = forAll { m: Wrapped[Seq[Wrapped[Int]]] => roundTrip(m) }
  property("seq - coproducts") = forAll { m: Wrapped[Seq[Tree[Int]]] => roundTrip(m) }

  property("vector - primitives") = forAll { m: Wrapped[Vector[Int]] => roundTrip(m) }
  property("vector - products") = forAll { m: Wrapped[Vector[Wrapped[Int]]] => roundTrip(m) }
  property("vector - coproducts") = forAll { m: Wrapped[Vector[Tree[Int]]] => roundTrip(m) }

  property("option - primitives") = forAll { m: Wrapped[Option[Int]] => roundTrip(m) }
  property("option - products") = forAll { m: Wrapped[Option[Wrapped[Int]]] => roundTrip(m) }
  property("option - coproducts") = forAll { m: Wrapped[Option[Tree[Int]]] => roundTrip(m) }

  property("map - primitives") = forAll { m: Wrapped[Map[String, Int]] => roundTrip(m) }
  property("map - products") = forAll { m: Wrapped[Map[String, Wrapped[Int]]] => roundTrip(m) }
  property("map - coproducts") = forAll { m: Wrapped[Map[String, Tree[Int]]] => roundTrip(m) }
  property("map - list") = forAll { m: Wrapped[Map[String, List[Int]]] => roundTrip(m) }
//  property("map - seq") = forAll { m: Wrapped[Map[String, Seq[Int]]] => roundTrip(m) }
//  property("map - set") = forAll { m: Wrapped[Map[String, Set[Int]]] => roundTrip(m) }

  property("coproducts - primitives") = forAll { m: Wrapped[Tree[Int]] => roundTrip(m) }
  property("coproducts - products") = forAll { m: Wrapped[Tree[Wrapped[Int]]] => roundTrip(m) }
  property("coproducts - list") = forAll { m: Wrapped[Tree[List[Int]]] => roundTrip(m) }
  property("coproducts - set") = forAll { m: Wrapped[Tree[Set[Int]]] => roundTrip(m) }
  property("coproducts - seq") = forAll { m: Wrapped[Tree[Seq[Int]]] => roundTrip(m) }
  property("coproducts - vector") = forAll { m: Wrapped[Tree[Vector[Int]]] => roundTrip(m) }
  property("coproducts - option") = forAll { m: Wrapped[Tree[Option[Int]]] => roundTrip(m) }
  property("coproducts - map with primitives") = forAll { m: Wrapped[Tree[Map[String, Int]]] => roundTrip(m) }
  property("coproducts - map with products") = forAll { m: Wrapped[Tree[Map[String, Wrapped[Int]]]] => roundTrip(m) }
  property("coproducts - map with coproducts") = forAll { m: Wrapped[Tree[Map[String, Tree[Int]]]] => roundTrip(m) }

//  property("openinghours") = forAll { m: OpeningHours => roundTrip(m) }
//  property("openinghours") = forAll { m: Map[String, List[Range[Time]]] => roundTrip(m) }

  implicit val orientClient = InMemoryClient("DerivedReadSpec")

  def roundTrip[A](value: A)(implicit OF: OrientFormat[A]): Boolean = {

//    println(OF.properties(value))

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


