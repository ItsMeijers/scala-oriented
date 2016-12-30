package oriented.maps

import java.util.{Date, UUID}


trait BaseMappableType[M] {
  def base: M
  def keys(m: M): Set[String]
  def get(m: M, key: String): Option[M]
  def getAll(m: M, key: String): Seq[M]
  def put(key: String, value: M, tail: M): M
  def put(key: String, value: Option[M], tail: M): M
  def put(key: String, values: Seq[M], tail: M): M
}

object BaseMappableType {
  implicit val scalaMap: BaseMappableType[Map[String, Any]] = new BaseMappableType[Map[String, Any]] {
    override def base: Map[String, Any] = Map.empty[String, Any]

    override def get(m: Map[String, Any], key: String): Option[Map[String, Any]] =
      m.get(key).map(_.asInstanceOf[Map[String, Any]])

    override def getAll(m: Map[String, Any], key: String): Seq[Map[String, Any]] =
      m.get(key)
        .toSeq
        .flatMap(_.asInstanceOf[Seq[Map[String, Any]]])

    override def put(key: String, value: Map[String, Any], tail: Map[String, Any]): Map[String, Any] = {
      tail + (key -> value)
    }
    override def put(key: String, value: Option[Map[String, Any]], tail: Map[String, Any]): Map[String, Any] = {
      value.fold[Map[String, Any]](Map.empty)(v => Map(key -> v)) ++ tail
    }
    override def put(key: String, values: Seq[Map[String, Any]], tail: Map[String, Any]): Map[String, Any] = {
      tail + (key -> values)
    }

    override def keys(m: Map[String, Any]): Set[String] = m.keySet
  }
}

trait MappableType[M, V] {
  def get(m: M, key: String): Option[V]
  def getAll(m: M, key: String): Seq[V]
  def put(key: String, value: V, tail: M): M
  def put(key: String, value: Option[V], tail: M): M
  def put(key: String, values: Seq[V], tail: M): M
}

object MappableType {
  trait ScalaMappableType[V] extends MappableType[Map[String, Any], V] { self =>
    def from(value: Any): V
    def to(value: V): Any

    final def xmap[W](f: V => W)(g: W => V) = new ScalaMappableType[W] {
      override def from(value: Any): W = f(self.from(value))
      override def to(value: W): Any = self.to(g(value))
    }

    override def get(m: Map[String, Any], key: String): Option[V] =
      m.get(key).map(from)

    override def getAll(m: Map[String, Any], key: String): Seq[V] =
      m.get(key)
        .toSeq
        .flatMap(x => x.asInstanceOf[Seq[Any]].map(from))

    override def put(key: String, value: V, tail: Map[String, Any]): Map[String, Any] = {
      tail + (key -> to(value))
    }
    override def put(key: String, value: Option[V], tail: Map[String, Any]): Map[String, Any] = {
      value.fold[Map[String, Any]](Map.empty)(v => Map(key -> to(v))) ++ tail
    }
    override def put(key: String, values: Seq[V], tail: Map[String, Any]): Map[String, Any] = {
      tail + (key -> values)
    }
  }

  def createMapping[T](fromFn: Any => T, toFn: T => Any) = new ScalaMappableType[T] {
    override def from(value: Any): T = fromFn(value)
    override def to(value: T): Any = toFn(value)
  }

  implicit val bool: ScalaMappableType[Boolean] = createMapping[Boolean](_.asInstanceOf[Boolean], identity)
  implicit val int: ScalaMappableType[Int] = createMapping[Int](_.asInstanceOf[Int], identity)
  implicit val long: ScalaMappableType[Long] = createMapping[Long](_.asInstanceOf[Long], identity)
  implicit val short: ScalaMappableType[Short] = createMapping[Short](_.asInstanceOf[Short], identity)
  implicit val float: ScalaMappableType[Float] = createMapping[Float](_.asInstanceOf[Float], identity)
  implicit val bigDecimal: ScalaMappableType[BigDecimal] = createMapping[BigDecimal](_.asInstanceOf[BigDecimal], identity)
  implicit val double: ScalaMappableType[Double] = createMapping[Double](_.asInstanceOf[Double], identity)
  implicit val string: ScalaMappableType[String] = createMapping[String](_.asInstanceOf[String], identity)
  implicit val date: ScalaMappableType[Date] = createMapping[Date](_.asInstanceOf[Date], identity)
  implicit val uuid: ScalaMappableType[UUID] = createMapping[UUID](_.asInstanceOf[UUID], identity)

}
