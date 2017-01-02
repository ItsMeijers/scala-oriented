package dsl

import org.scalatest._
import oriented._
import oriented.syntax._

class SqlSpec extends Suite with WordSpecLike with Matchers with BeforeAndAfter  {

  implicit val orientClient = InMemoryClient("SqlSpec")

  case class Person(name: String, age: Int)

  "SqlSpec" should {
    "be to use as cast" in {

      val prg = for {
        _ <- orientClient.addVertex(Person("Mark", 1337))
        age <- sql"SELECT age FROM Person LIMIT 1".as[Int]("age")
      } yield age

      prg.runGraphUnsafe(enableTransactions = false) shouldBe 1337
    }
  }
}
