package dsl

import java.util.Date

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

  case class Meta(date: Date, user: String)

  implicit val orientFormatMeta: OrientFormat[Meta] = new OrientFormat[Meta] {
    override def read: OrientRead[Meta] = for {
      date <- readDatetime("date")
      user <- readString("user")
    } yield Meta(date, user)

    override def name: String = "Meta"

    override def properties(model: Meta): Map[String, Any] = Map("date" -> model.date, "user" -> model.user)
    // for {
    //   date <- writeDatetime("date", _.date)
    //   user <- writeString("user", _.user)
    // } yield List(date, user)
  }

  case class Blog(tid: Long, content: String, meta: Meta)

  implicit val orientFormatTweet: OrientFormat[Blog] = new OrientFormat[Blog] {

    override def read: OrientRead[Blog] = for {
      id <- readLong("tid")
      content <- readString("content")
      meta <- read[Meta]("meta")
    } yield Blog(id, content, meta)

    override def name: String = "Blog"

    /**
      * **** this is ugly
      */
    override def properties(model: Blog): Map[String, Any] =
      Map("tid" -> model.tid,
          "content" -> model.content,
          "meta" -> metaProperties(model.meta))
    // def write: List[OrientWrite] =
    // for {
    //   tid     <- writeLong("tid", _.tid)
    //   content <- writeString("content", _.content)
    //   meta    <- write("meta", _.meta)(implicit orientFormat: OrientFormat[Meta])
    // } yield OrientWrite(tid, content, meta)

    private def metaProperties(model: Meta): Map[String, Any] = Map("date" -> model.date, "user" -> model.user)
  }

  "Read Embdedded" should "be able to read an embedded object" in {
    val blogVertex = orientClient
      .addVertex(Blog(1, "Some content", Meta(new Date(), "Thomas")))
      .runGraphUnsafe(enableTransactions = false)

    val blogVertexFromQuery = sql"SELECT FROM Blog WHERE tid = '1'"
      .vertex[Blog]
      .unique
      .runGraphUnsafe(enableTransactions = false)

    blogVertex.element should equal(blogVertexFromQuery.element)
  }

  case class BlogEmbed(tid: Long, blog: Blog, metas: List[Meta])

  implicit val orientFormatBlogEmbeded: OrientFormat[BlogEmbed] = new OrientFormat[BlogEmbed] {
    def read: OrientRead[BlogEmbed] = for {
      tid <- readLong("tid")
      blog <- read[Blog]("blog")
      metas <- readList[Meta]("metas")
    } yield BlogEmbed(tid, blog, metas)

    def name: String = "BlogEmbed"

    def properties(model: BlogEmbed): Map[String, Any] =
      Map("tid" -> model.tid, "blog" ->
        Map("tid" -> model.blog.tid,
            "content" -> model.blog.content,
            "meta" -> Map("date" -> model.blog.meta.date,
                          "user" -> model.blog.meta.user)),
      "metas" -> model.metas.map(m => Map("date" -> m.date, "user" -> m.user)))
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
