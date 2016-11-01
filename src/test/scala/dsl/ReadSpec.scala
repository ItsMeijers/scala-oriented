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


  case class EmbeddedRecord(id: Long, name: String)

  case class ContainerRecord(id: Long, name: String, description: String, embeddedRecord: EmbeddedRecord)

  implicit val embeddedFormat: OrientFormat[EmbeddedRecord] = new OrientFormat[EmbeddedRecord] {

    override def name: String = "EmbeddedRecord"

    override def properties(model: EmbeddedRecord): Map[String, Any] = {
      println("EmbeddedRecord :: properties")
      Map("mid" -> model.id, "name" -> model.name)
    }

    override def read: OrientRead[EmbeddedRecord] =
      for {
        id <- readLong("mid")
        name <- readString("name")
      } yield EmbeddedRecord(id, name)
  }

  implicit val containerFormat: OrientFormat[ContainerRecord] = new OrientFormat[ContainerRecord] {

    override def name: String = "ContainerRecord"

    override def properties(model: ContainerRecord): Map[String, Any] = {
      println("ContainerRecord :: properties")
      Map(
        "mid" -> model.id,
        "name" -> model.name,
        "description" -> model.description,
        "inside" -> Map(
          "mid" -> model.embeddedRecord.id,
          "name" -> model.embeddedRecord.name
        )
      )
    }

    override def read: OrientRead[ContainerRecord] =
      for {
        id <- readLong("mid")
        name <- readString("name")
        description <- readString("description")
        embedded <- readEmbedded(classOf[EmbeddedRecord], "inside")
      } yield ContainerRecord(id, name, description, embedded)
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

  "Read embedded" should "be able to read embedded records from an OrientElement" in {
    val model = ContainerRecord(10, "container", "this is a container with a inner record", EmbeddedRecord(123, "hey I'm inside"))
    val vertex = orientClient.addVertex(model)
    vertex.runGraphUnsafe(enableTransactions = false).element should ===(model)
  }
}
