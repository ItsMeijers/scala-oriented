package dsl

import java.util.Date

object Test

case class BigDecTest(bigDecimal: BigDecimal)

case class Meta(date: Date, user: String)

case class Blog(tid: Long, content: String, meta: Meta)

case class BlogEmbed(tid: Long, blog: Blog, metas: List[Meta])
