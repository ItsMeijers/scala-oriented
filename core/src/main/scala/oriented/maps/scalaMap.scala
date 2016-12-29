package oriented.maps

import java.util.{Date, UUID}

import cats.data.NonEmptyList

import scala.collection.JavaConverters._


object scalaMap {
  type Row = Map[String, Any]

  trait ScalaMappableType[V] extends MappableType[Row, V] { self =>
    def from(value: Any): V
    def to(value: V): Any

    final def xmap[W](f: V => W)(g: W => V) = new ScalaMappableType[W] {
      override def from(value: Any): W = f(self.from(value))
      override def to(value: W): Any = self.to(g(value))
    }

    override def get(m: Row, key: String): Option[V] =
      m.get(key).map(from)

    override def getAll(m: Row, key: String): Seq[V] =
      m.get(key)
        .toSeq
        .flatMap(x => x.asInstanceOf[Seq[Any]].map(from))

    override def put(key: String, value: V, tail: Row): Row = {
      tail + (key -> to(value))
    }
    override def put(key: String, value: Option[V], tail: Row): Row = {
      value.fold[Map[String, Any]](Map.empty)(v => Map(key -> to(v))) ++ tail
    }
    override def put(key: String, values: Seq[V], tail: Row): Row = {
      tail + (key -> values)
    }
  }

  def createMapping[T](fromFn: Any => T, toFn: T => Any) = new ScalaMappableType[T] {
    override def from(value: Any): T = fromFn(value)
    override def to(value: T): Any = toFn(value)
  }

  implicit val bool: ScalaMappableType[Boolean] = createMapping[Boolean](_.toString.toBoolean, identity)
  implicit val int: ScalaMappableType[Int] = createMapping[Int](_.toString.toInt, identity)
  implicit val long: ScalaMappableType[Long] = createMapping[Long](_.toString.toLong, identity)
  implicit val short: ScalaMappableType[Short] = createMapping[Short](_.toString.toShort, identity)
  implicit val float: ScalaMappableType[Float] = createMapping[Float](_.toString.toFloat, identity)
  implicit val bigDecimal: ScalaMappableType[BigDecimal] = createMapping[BigDecimal](_.asInstanceOf[BigDecimal], identity)
  implicit val double: ScalaMappableType[Double] = createMapping[Double](_.toString.toDouble, identity)
  implicit val string: ScalaMappableType[String] = createMapping[String](_.toString, identity)
  implicit val date: ScalaMappableType[Date] = createMapping[Date](_.asInstanceOf[Date], identity)
  implicit val uuid: ScalaMappableType[UUID] = createMapping[UUID](_.asInstanceOf[UUID], identity)

  implicit val baseMappableType: BaseMappableType[Row] = new BaseMappableType[Row] {
    override def base: Row = Map.empty[String, Any]

    override def get(m: Row, key: String): Option[Row] =
      m.get(key).map(_.asInstanceOf[Row])

    override def getAll(m: Row, key: String): Seq[Row] =
      m.get(key)
        .toSeq
        .flatMap(_.asInstanceOf[Seq[Row]])

    override def put(key: String, value: Row, tail: Row): Row = {
      tail + (key -> value)
    }
    override def put(key: String, value: Option[Row], tail: Row): Row = {
      value.fold[Map[String, Any]](Map.empty)(v => Map(key -> v)) ++ tail
    }
    override def put(key: String, values: Seq[Row], tail: Row): Row = {
      tail + (key -> values)
    }
  }
}
