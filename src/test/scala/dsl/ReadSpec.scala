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

    override def format: OrientRead[Test.type] = read.read(Test)

    override def name: String = "Test"

    override def properties(model: Test.type): Map[String, Any] = Map()
  }

  implicit val orientClient = InMemoryClient("test")

  "Read constructor" should "save edge with no fields" in {
    val edge = orientClient.addVertex(Test)
    edge.runGraphUnsafe(enableTransactions = false).element should === (Test)
  }

}
