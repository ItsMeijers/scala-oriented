package dsl

import java.util.Date

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import oriented.{InMemoryClient, OrientFormat}
import oriented.syntax._

/**
  * Test spec for Read DSL
  */
class ReadSpec extends FlatSpec with Matchers with BeforeAndAfter {

  implicit val orientClient = InMemoryClient("test")

  "Read constructor" should "save edge with no fields" in {
    val edge = orientClient.addVertex(Test)
    edge.runGraphUnsafe(enableTransactions = false).element should === (Test)
  }

  "Read big decimal" should "be able to read an decimal from an OrientElement" in {
    val model = BigDecTest(1000000)
    val bd = orientClient.addVertex(model)
    bd.runGraphUnsafe(enableTransactions = false).element should === (model)
  }

  "Read Embdedded" should "be able to read an embedded object" in {

    val vertex = Blog(1, "Some content", Meta(new Date(), "Thomas"))
    val blogVertex = orientClient
      .addVertex(vertex)
      .runGraphUnsafe(enableTransactions = false)

    val blogVertexFromQuery = sql"SELECT FROM Blog WHERE tid = '1'"
      .vertex[Blog]
      .unique
      .runGraphUnsafe(enableTransactions = false)

    vertex should equal(blogVertexFromQuery.element)
  }

  "Read embedded" should "be able to read an embedded object in an embedded object" in {
    val metaList = List(Meta(new Date(), "Foo"), Meta(new Date(), "Bar"), Meta(new Date(), "Baz"))
    val embededBlogVertex = orientClient
      .addVertex(BlogEmbed(1, Blog(2, "Bla bla", Meta(new Date(), "Thomasso")), metaList))
      .runGraphUnsafe(enableTransactions = false)

    val embeddedBlogQuery = sql"SELECT FROM BlogEmbed WHERE tid = '1'"
      .vertex[BlogEmbed]
      .unique
      .runGraphUnsafe(enableTransactions = false)

    embededBlogVertex.element should equal(embeddedBlogQuery.element)
    embededBlogVertex.element.metas.size should equal(embeddedBlogQuery.element.metas.size)
  }

}
