package maps

import org.scalacheck.Properties
import oriented._
import oriented.syntax._

trait OrientDbReadSpec extends ReadSpec { self: Properties =>

  implicit val orientClient = InMemoryClient(self.name)

  override def roundTrip[A](value: A)(implicit OF: OrientFormat[A]): Boolean = {
    val prg = for {
      vertex <- orientClient.addVertex(value)
      res <- sql"SELECT FROM ${vertex.orientElement.getIdentity}"
        .vertex[A]
        .unique
    } yield res

    prg.runGraphUnsafe(enableTransactions = false).element == value
  }
}
