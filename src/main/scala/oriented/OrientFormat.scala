package oriented

import java.util.Date

import cats.implicits._
import oriented.free.dsl._
import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

import scala.reflect.ClassTag

/**
  * OrientFormat typeclass makes it able to transform from and to OrientElements from a specific model A.
  */
trait OrientFormat[A] extends OrientObjectRead[A] with OrientObjectWrite[A] {
  val name: String
}

object OrientFormat {
  implicit def apply[A](implicit R: OrientObjectRead[A], W: OrientObjectWrite[A], CT: ClassTag[A]): OrientFormat[A] = new OrientFormat[A] {
    val name: String = CT.runtimeClass.getSimpleName
    def decode: OrientRead[A] = R.decode
    def encode(value: A): Map[String, Any] = W.encode(value)
  }
}

trait OrientObjectRead[A] {
  def decode: OrientRead[A]
}

trait OrientFieldRead[A] {
  def decode(fieldName: String): OrientRead[A]
}

trait OrientFieldWrite[A] {
  def encode(fieldName: String, value: A): Map[String, Any]
}

trait OrientObjectWrite[A] {
  def encode(value: A): Map[String, Any]
}


object OrientFieldWrite {

  def anyWrite[T] = new OrientFieldWrite[T] {
    override def encode(fieldName: String, value: T): Map[String, Any] = Map(fieldName -> value)
  }

  implicit val int: OrientFieldWrite[Int] = anyWrite[Int]
  implicit val string: OrientFieldWrite[String] = anyWrite[String]
  implicit val bigDecimal: OrientFieldWrite[BigDecimal] = anyWrite[BigDecimal]
  implicit val long: OrientFieldWrite[Long] = anyWrite[Long]
  implicit val double: OrientFieldWrite[Double] = anyWrite[Double]
  implicit val short: OrientFieldWrite[Short] = anyWrite[Short]
  implicit val date: OrientFieldWrite[Date] = anyWrite[Date]


  implicit def embeddedObject[T](implicit T: OrientObjectWrite[T]): OrientFieldWrite[T] = new OrientFieldWrite[T] {
    override def encode(fieldName: String, value: T): Map[String, Any] = Map(fieldName -> T.encode(value))
  }

  implicit def listObject[T](implicit T: OrientObjectWrite[T]): OrientFieldWrite[List[T]] = new OrientFieldWrite[List[T]] {
    override def encode(fieldName: String, value: List[T]): Map[String, Any] = Map(fieldName -> value.map(T.encode))
  }

  implicit def option[T] = new OrientFieldWrite[Option[T]] {
    override def encode(fieldName: String, value: Option[T]): Map[String, Any] = value.fold[Map[String, Any]](Map())(v => Map(fieldName -> v))
  }
}

object OrientFieldRead {

  import OrientRead._

  def fieldRead[A](f: String => OrientRead[A]) = new OrientFieldRead[A] {
    override def decode(fieldName: String): OrientRead[A] = f(fieldName)
  }

  implicit val intField: OrientFieldRead[Int] = fieldRead(int)
  implicit val stringField: OrientFieldRead[String] = fieldRead(string)
  implicit val bigDecimalField: OrientFieldRead[BigDecimal] = fieldRead(bigDecimal)
  implicit val longField: OrientFieldRead[Long] = fieldRead(long)
  implicit val doubleField: OrientFieldRead[Double] = fieldRead(double)
  implicit val shortField: OrientFieldRead[Short] = fieldRead(short)
  implicit val dateField: OrientFieldRead[Date] = fieldRead(date)

  implicit def embeddedObject[T](implicit T: OrientObjectRead[T]): OrientFieldRead[T] = new OrientFieldRead[T] {
    override def decode(fieldName: String): OrientRead[T] = embedded(fieldName, T.decode)
  }

  implicit def listField[T](implicit T: OrientObjectRead[T]): OrientFieldRead[List[T]] = new OrientFieldRead[List[T]] {
    override def decode(fieldName: String): OrientRead[List[T]] = list(fieldName, T.decode)
  }

  implicit def optionObject[T](implicit T: OrientObjectRead[T]): OrientFieldRead[Option[T]] = new OrientFieldRead[Option[T]] {
    override def decode(fieldName: String): OrientRead[Option[T]] = option(embedded(fieldName, T.decode))
  }

  implicit def optionField[T](implicit T: OrientFieldRead[T]): OrientFieldRead[Option[T]] = new OrientFieldRead[Option[T]] {
    override def decode(fieldName: String): OrientRead[Option[T]] = option(T.decode(fieldName))
  }
}

object OrientObjectRead {
  implicit val hnil = new OrientObjectRead[HNil] {
    override def decode: OrientRead[HNil] = OrientRead.pure(HNil)
  }

  implicit def hcons[K <: Symbol, V, T <: HList](implicit key: Witness.Aux[K],
                                                 sv: Lazy[OrientFieldRead[V]],
                                                 st: Lazy[OrientObjectRead[T]]) = new OrientObjectRead[FieldType[K, V] :: T] {
    override def decode: OrientRead[FieldType[K, V] :: T] =
      OrientRead.product(sv.value.decode(key.value.name), st.value.decode).map { case (h, t) => field[K](h) :: t}
  }

  implicit def generic[T, R](implicit gen: LabelledGeneric.Aux[T, R], readRepr: Lazy[OrientObjectRead[R]]) = new OrientObjectRead[T] {
    override def decode: OrientRead[T] = readRepr.value.decode.map(r => gen.from(r))
  }

  def apply[A](implicit A: OrientObjectRead[A]) = A
}

object OrientObjectWrite {
  implicit val hnil = new OrientObjectWrite[HNil] {
    override def encode(value: HNil): Map[String, Any] = Map.empty
  }

  implicit def hcons[K <: Symbol, V, T <: HList](implicit key: Witness.Aux[K],
                                                 sv: Lazy[OrientFieldWrite[V]],
                                                 st: Lazy[OrientObjectWrite[T]]) = new OrientObjectWrite[FieldType[K, V] :: T] {
    override def encode(value: FieldType[K, V] :: T): Map[String, Any] = sv.value.encode(key.value.name, value.head) ++ st.value.encode(value.tail)
  }

  implicit def generic[T, R](implicit gen: LabelledGeneric.Aux[T, R], writeRepr: Lazy[OrientObjectWrite[R]]) = new OrientObjectWrite[T] {
    override def encode(value: T): Map[String, Any] = writeRepr.value.encode(gen.to(value))
  }

  def apply[A](implicit A: OrientObjectWrite[A]) = A
}
