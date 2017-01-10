package oriented.maps

import java.util.{Date, UUID}
import cats.implicits._
import enum.Enum

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
      m.get(key).flatMap(safeCast[Map[String, Any]])

    override def getAll(m: Map[String, Any], key: String): Seq[Map[String, Any]] =
      m.get(key)
        .flatMap(safeCast[Seq[Map[String, Any]]])
        .getOrElse(Seq.empty)

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
    def from(value: Any): Option[V]
    def to(value: V): Any

    /**
      * Invariant mapping of V to W and W to V with taking error into account while "decoding" (V => W)
      * @param f A decoding function with taking error into account
      * @param g A encoding function
      */
    final def xmapF[W](f: V => Option[W])(g: W => V) = new ScalaMappableType[W] {
      override def from(value: Any): Option[W] = self.from(value).flatMap(f)
      override def to(value: W): Any = self.to(g(value))
    }

    override def get(m: Map[String, Any], key: String): Option[V] =
      m.get(key).flatMap(from)

    override def getAll(m: Map[String, Any], key: String): Seq[V] =
      (for {
        x <- m.get(key)
        seq <- safeCast[Seq[Any]](x)
        converted <- seq.toList.traverse(from)
      } yield converted) getOrElse Seq.empty

    override def put(key: String, value: V, tail: Map[String, Any]): Map[String, Any] = {
      tail + (key -> to(value))
    }
    override def put(key: String, value: Option[V], tail: Map[String, Any]): Map[String, Any] = {
      value.fold[Map[String, Any]](Map.empty)(v => Map(key -> to(v))) ++ tail
    }
    override def put(key: String, values: Seq[V], tail: Map[String, Any]): Map[String, Any] = {
      tail + (key -> values.map(to))
    }
  }

  def createMapping[T](fromFn: Any => Option[T], toFn: T => Any) = new ScalaMappableType[T] {
    override def from(value: Any): Option[T] = fromFn(value)
    override def to(value: T): Any = toFn(value)
  }

  implicit val bool: ScalaMappableType[Boolean] = createMapping(safeCast[Boolean], identity)
  implicit val int: ScalaMappableType[Int] = createMapping(safeCast[Int], identity)
  implicit val long: ScalaMappableType[Long] = createMapping(safeCast[Long], identity)
  implicit val short: ScalaMappableType[Short] = createMapping(safeCast[Short], identity)
  implicit val float: ScalaMappableType[Float] = createMapping(safeCast[Float], identity)
  implicit val bigDecimal: ScalaMappableType[BigDecimal] = createMapping(safeCast[BigDecimal], identity)
  implicit val double: ScalaMappableType[Double] = createMapping(safeCast[Double], identity)
  implicit val string: ScalaMappableType[String] = createMapping(safeCast[String], identity)
  implicit val date: ScalaMappableType[Date] = createMapping(safeCast[Date], identity)
  implicit val uuid: ScalaMappableType[UUID] = createMapping(safeCast[UUID], identity)

  implicit def enum[E](implicit E: Enum[E]): MappableType[Map[String, Any], E] =
    MappableType.string.xmapF(x => E.decodeOpt(x))(E.encode)

}
