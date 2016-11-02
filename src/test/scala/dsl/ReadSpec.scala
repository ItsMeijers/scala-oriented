package dsl

import models.{Container, Container2, Embedded}
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

  case class BigDecTest(bigDecimal: BigDecimal)

  implicit val bdFormat: OrientFormat[BigDecTest] = new OrientFormat[BigDecTest] {

    override def name: String = "BigDecTest"

    override def properties(model: BigDecTest): Map[String, Any] = Map("bigDecimal" -> model.bigDecimal)

    override def read: OrientRead[BigDecTest] = readBigDecimal("bigDecimal").map(BigDecTest.apply)
  }


  implicit val orientClient = InMemoryClient("test")

  "Read constructor" should "save edge with no fields" in {
    val edge = orientClient.addVertex(Test)
    edge.runGraphUnsafe(enableTransactions = false).element should ===(Test)
  }

  "Read big decimal" should "be able to read an decimal from an OrientElement" in {
    val model = BigDecTest(BigDecimal(1000000))
    val bd = orientClient.addVertex(model)
    bd.runGraphUnsafe(enableTransactions = false).element should ===(model)
  }

  "Read embedded" should "be able to read embedded record from an OrientElement" in {
    val model = Container(10, "container", "this is a container with a inner record", Embedded(123, "hey I'm inside"))
    val vertex = orientClient.addVertex(model)
    vertex.runGraphUnsafe(enableTransactions = false).element should ===(model)
  }

  "Read embedded" should "be able to read multiple embedded records from an OrientElement" in {
    val model = Container2(10, "container", List(Embedded(123, "hey I'm inside"), Embedded(321, "hey I'm also inside"), Embedded(33, "hey me too")))
    val vertex = orientClient.addVertex(model)
    vertex.runGraphUnsafe(enableTransactions = false).element should ===(model)
  }
}
