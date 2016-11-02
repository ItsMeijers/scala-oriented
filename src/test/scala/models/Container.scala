package models

import oriented.OrientFormat
import oriented.syntax._

case class Embedded(id: Long, name: String)
case class Container(id: Long, name: String, description: String, embeddedRecord: Embedded)
case class Container2(id: Long, name: String, embeddeds: List[Embedded])

object Embedded {

  implicit val format = new OrientFormat[Embedded] {
    override def name: String = "EmbeddedRecord"
    override def properties(model: Embedded): Map[String, Any] = {
      println("EmbeddedRecord :: properties")
      Map("mid" -> model.id, "name" -> model.name)
    }

    override def read: OrientRead[Embedded] =
      for {
        id <- readLong("mid")
        name <- readString("name")
      } yield Embedded(id, name)
  }

}

object Container {

  implicit val format = new OrientFormat[Container] {
    override def name: String = "ContainerRecord"
    override def properties(model: Container): Map[String, Any] = {
      println("ContainerRecord :: properties")
      Map(
        "mid" -> model.id,
        "name" -> model.name,
        "description" -> model.description,
        "inside" -> Embedded.format.properties(model.embeddedRecord)
      )
    }

    override def read: OrientRead[Container] =
      for {
        id <- readLong("mid")
        name <- readString("name")
        description <- readString("description")
        embedded <- readEmbedded(classOf[Embedded], "inside")
      } yield Container(id, name, description, embedded)
  }

}

object Container2 {

  implicit val format = new OrientFormat[Container2] {
    override def name: String = "ContainerRecord2"
    override def properties(model: Container2): Map[String, Any] = {
      println("ContainerRecord2 :: properties")
      Map(
        "mid" -> model.id,
        "name" -> model.name,
        "inside" -> model.embeddeds.map(Embedded.format.properties)
      )
    }

    override def read: OrientRead[Container2] =
      for {
        id <- readLong("mid")
        name <- readString("name")
        embedded <- readEmbedded(classOf[List[Embedded]], "inside")
      } yield Container2(id, name, embedded)
  }

}