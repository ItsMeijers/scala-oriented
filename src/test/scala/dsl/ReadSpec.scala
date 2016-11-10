package dsl

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import oriented.{InMemoryClient, OrientFormat}
import oriented.syntax._

/**
  * Test spec for Read DSL
  */
class ReadSpec extends FlatSpec with Matchers with BeforeAndAfter {

  object Test

  implicit val testFormat: OrientFormat[Test.type] = new OrientFormat[Test.type] {

    override def read: OrientRead[Test.type] = read(Test)

    override def name: String = "Test"

    override def properties(model: Test.type): Map[String, Any] = Map()
  }

  implicit val orientClient = InMemoryClient("test")

  "Read constructor" should "save edge with no fields" in {
    val edge = orientClient.addVertex(Test)
    edge.runGraphUnsafe(enableTransactions = false).element should === (Test)
  }

  case class FooBar(name: String, age: Option[Int])

  implicit val foobarFormat = new OrientFormat[FooBar] {

    override def read: OrientRead[FooBar] =
      for {
        name <- readString("name")
        age  <- readIntOpt("age")
      } yield FooBar(name, age)

    override def name: String = "FooBar"

    override def properties(model: FooBar): Map[String, Any] =
      Map("name" -> model.name, "age" -> model.age)
  }

  "Read constructor" should "read optional values correctly" in {
    val foo = FooBar("foo", Some(12))
    val fooVertex = orientClient.addVertex(foo).runGraphUnsafe

    val dbFooVertex = sql"SELECT FROM FooBar WHERE name = '${foo.name}'".vertex[FooBar].unique.runGraphUnsafe

    dbFooVertex.element shouldBe foo
  }
}
