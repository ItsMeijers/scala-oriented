package dsl

import java.util.{Date, UUID}

import cats.data.NonEmptyList
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck.{Arbitrary, Gen, Properties}
import oriented.maps.{BaseMappableType, FromMappable, ToMappable}
import oriented.syntax._
import oriented.{InMemoryClient, OrientFormat}
import oriented.maps.scalaMap._

class DerivedReadSpec extends Properties("DerivedReadSpec") {



  property("int") = forAll { m: Wrapped[Int] => roundTrip(m) }
  property("long") = forAll { m: Wrapped[Long] => roundTrip(m) }
  property("bigDecimal") = forAll { m: Wrapped[BigDecimal] => roundTrip(m) }
  property("float") = forAll { m: Wrapped[Float] => roundTrip(m) }
  property("double") = forAll { m: Wrapped[Double] => roundTrip(m) }
  property("short") = forAll { m: Wrapped[Short] => roundTrip(m) }
  property("date") = forAll { m: Wrapped[Date] => roundTrip(m) }
  property("string") = forAll { m: Wrapped[String] => roundTrip(m) }

  property("list - primitives") = forAll { m: Wrapped[List[Int]] => roundTrip(m) }
  property("list - products") = forAll { m: Wrapped[List[Wrapped[Int]]] => roundTrip(m) }
  property("list - coproducts") = forAll { m: Wrapped[List[Tree[Int]]] => roundTrip(m) }

  property("set - primitives") = forAll { m: Wrapped[Set[Int]] => roundTrip(m) }
  property("set - products") = forAll { m: Wrapped[Set[Wrapped[Int]]] => roundTrip(m) }
  property("set - coproducts") = forAll { m: Wrapped[Set[Tree[Int]]] => roundTrip(m) }

  property("seq - primitives") = forAll { m: Wrapped[Seq[Int]] => roundTrip(m) }
  property("seq - products") = forAll { m: Wrapped[Seq[Wrapped[Int]]] => roundTrip(m) }
  property("seq - coproducts") = forAll { m: Wrapped[Seq[Tree[Int]]] => roundTrip(m) }


//  property("option") = forAll { m: Wrapped[Option[Int]] => roundTrip(m) }
//  property("coproducts") = forAll { m: Wrapped[LastReservableTime] => roundTrip(m) }
  //TODO: map, set?

  implicit val orientClient = InMemoryClient("DerivedReadSpec")


//  implicitly[FromMappable[Branch[Int], Map[String, Any]]]
//  implicitly[FromMappable[Leaf[Int], Map[String, Any]]]
//  implicitly[FromMappable[Tree[Int], Map[String, Any]]]
//  implicitly[FromMappable[List[LastReservableTime], Map[String, Any]]]
//  implicitly[FromMappable[WrappedLastReservableTime, Map[String, Any]]]
//  implicitly[FromMappable[Wrapped[LastReservableTime], Map[String, Any]]]

  //[GenTraversableOnce] x [Product, Coproduct, Primitive]
  //Option x [Product, Coproduct, Primitive]


  implicitly[FromMappable[Wrapped[Seq[Int]], Map[String, Any]]]
//  implicitly[FromMappable[Wrapped[Option[Tree[Int]]], Map[String, Any]]]
//  implicitly[FromMappable[Wrapped[List[Int]], Map[String, Any]]]
  implicitly[FromMappable[Wrapped[Seq[Wrapped[Int]]], Map[String, Any]]]
//  implicitly[FromMappable[Wrapped[List[Tree[Int]]], Map[String, Any]]]

  implicitly[ToMappable[Wrapped[Seq[Int]], Map[String, Any]]]
//  implicitly[ToMappable[Wrapped[Option[Tree[Int]]], Map[String, Any]]]
//  implicitly[ToMappable[Wrapped[List[Int]], Map[String, Any]]]
//  implicitly[ToMappable[Wrapped[List[Wrapped[Int]]], Map[String, Any]]]
//  implicitly[ToMappable[Wrapped[List[Tree[Int]]], Map[String, Any]]]
//  implicitly[ToMappable[Leaf[Int], Map[String, Any]]]
//  implicitly[ToMappable[Tree[Int], Map[String, Any]]]
//  implicitly[ToMappable[List[LastReservableTime], Map[String, Any]]]
//  implicitly[ToMappable[Wrapped[Wrapped[Int]], Map[String, Any]]]
//  implicitly[ToMappable[Wrapped[LastReservableTime], Map[String, Any]]]

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


