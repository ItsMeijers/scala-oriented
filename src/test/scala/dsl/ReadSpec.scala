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

  case class BigDecTest(bigDecimal: BigDecimal)

  implicit val bdFormat: OrientFormat[BigDecTest] = new OrientFormat[BigDecTest] {

  override def name: String = "BigDecTest"

    override def properties(model: BigDecTest): Map[String, Any] = Map("bigDecimal" -> model.bigDecimal)

    override def read: OrientRead[BigDecTest] = readBigDecimal("bigDecimal").map(BigDecTest.apply)
  }

  implicit val orientClient = InMemoryClient("test")

  "Read constructor" should "save edge with no fields" in {
    val edge = orientClient.addVertex(Test)
    edge.runGraphUnsafe(enableTransactions = false).element should === (Test)
  }

  "Read big decimal" should "be able to read an decimal from an OrientElement" in {
    val model = BigDecTest(BigDecimal(1000000))
    val bd = orientClient.addVertex(model)
    bd.runGraphUnsafe(enableTransactions = false).element should === (model)
  }

}
