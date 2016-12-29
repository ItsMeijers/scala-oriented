package oriented.maps

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.generic.{CanBuildFrom, IsTraversableOnce}

trait FromMappable[L, M] {
  def apply(m: M): Option[L]
}

trait LowPriorityFromMappableOption1 {
  implicit def hconsFromMappableOption1[K <: Symbol, V, T <: HList, M]
  (implicit wit: Witness.Aux[K], H: Lazy[MappableType[M, V]], T: FromMappable[T, M])
  : FromMappable[FieldType[K, Option[V]] :: T, M] = new FromMappable[FieldType[K, Option[V]] :: T, M] {
    override def apply(m: M): Option[FieldType[K, Option[V]] :: T] = T(m).map(t => field[K](H.value.get(m, wit.value.name)) :: t)
  }
}

trait LowPriorityFromMappableOption0 extends LowPriorityFromMappableOption1 {
  implicit def hconsFromMappableOption0[K <: Symbol, V, H <: HList, T <: HList, M]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], bmt: BaseMappableType[M],
   H: Lazy[FromMappable[H, M]], T: FromMappable[T, M])
  : FromMappable[FieldType[K, Option[V]] :: T, M] = new FromMappable[FieldType[K, Option[V]] :: T, M] {
    override def apply(m: M): Option[FieldType[K, Option[V]] :: T] = for {
      t <- T(m)
    } yield {
      val o = for {
        n <- bmt.get(m, wit.value.name)
        h <- H.value(n)
      } yield gen.from(h)
      field[K](o) :: t
    }
  }
}

trait LowPriorityFromMappableSeq0 extends LowPriorityFromMappableOption0 {
  implicit def hconsFromMappableSeq0[K <: Symbol, V, H <: HList, T <: HList, M, S[_]]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], bmt: BaseMappableType[M],
   H: Lazy[FromMappable[H, M]], T: FromMappable[T, M],
   cbf: CanBuildFrom[_, V, S[V]], toSeq: S[V] => Seq[V])
  : FromMappable[FieldType[K, S[V]] :: T, M] = new FromMappable[FieldType[K, S[V]] :: T, M] {
    override def apply(m: M): Option[FieldType[K, S[V]] :: T] = for {
      t <- T(m)
    } yield {
      val b = cbf()
      b ++= (for {
        n <- bmt.getAll(m, wit.value.name)
        h <- H.value(n)
      } yield gen.from(h))
      field[K](b.result()) :: t
    }
  }
}

trait LowerPrioFromMappable {
  implicit def hnilFromMappable[M]: FromMappable[HNil, M] = new FromMappable[HNil, M] {
    override def apply(m: M): Option[HNil] = Some(HNil)
  }

  implicit def cnilFromMappable[M]: FromMappable[CNil, M] = new FromMappable[CNil, M] {
    override def apply(m: M): Option[CNil] = None
  }

  implicit def hconsFromMappable0[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], bmt: BaseMappableType[M], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M])
  : FromMappable[FieldType[K, H] :: T, M] = new FromMappable[FieldType[K, H] :: T, M] {
    override def apply(m: M): Option[FieldType[K, H] :: T] = for {
      map <- bmt.get(m, wit.value.name)
      h <- H.value(map)
      t <- T(m)
    } yield field[K](h) :: t
  }


  implicit def hconsFromMappable1[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], mbt: BaseMappableType[M], H: MappableType[M, H], T: FromMappable[T, M])
  : FromMappable[FieldType[K, H] :: T, M] = new FromMappable[FieldType[K, H] :: T, M] {
    override def apply(m: M): Option[FieldType[K, H] :: T] = for {
      h <- H.get(m, wit.value.name)
      t <- T(m)
    } yield field[K](h) :: t
  }

  implicit def cconsFromMappable[M, K <: Symbol, H, T <: Coproduct](implicit key: Witness.Aux[K], mt: BaseMappableType[M], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]): FromMappable[FieldType[K, H] :+: T, M] = new FromMappable[FieldType[K, H] :+: T, M] {
    override def apply(m: M): Option[:+:[FieldType[K, H], T]] = {
      if(mt.get(m, s"__${key.value.name}").nonEmpty) H.value(m).map(v => Inl(field[K](v)))
      else T(m).map(Inr.apply)
    }
  }

  implicit def generic[T, R, M](implicit gen: LabelledGeneric.Aux[T, R], repr: Lazy[FromMappable[R, M]]): FromMappable[T, M] = new FromMappable[T, M] {
    override def apply(m: M): Option[T] = repr.value(m).map(x => gen.from(x))
  }
}

object FromMappable extends LowerPrioFromMappable {

  def fromMappableTraversableOnceMappable[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                               K: Witness.Aux[K],
                                                                               H: MappableType[M, H],
                                                                               T: FromMappable[T, M],
                                                                               BMT: BaseMappableType[M],
                                                                               CBF: CanBuildFrom[_, H, C[H]]
                                                                            ): FromMappable[FieldType[K, C[H]] :: T, M] = new FromMappable[FieldType[K, C[H]] :: T, M] {
    override def apply(m: M): Option[::[FieldType[K, C[H]], T]] = for {
      tail <- T(m)
    } yield {
      val b = CBF()
      b ++= H.getAll(m, K.value.name)
      field[K](b.result()) :: tail
    }
  }


  implicit def seqMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: FromMappable[T, M]) =
    fromMappableTraversableOnceMappable[K, H, T, Seq, M]

  implicit def setMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: FromMappable[T, M]) =
    fromMappableTraversableOnceMappable[K, H, T, Set, M]

  implicit def listMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: FromMappable[T, M]) =
    fromMappableTraversableOnceMappable[K, H, T, List, M]

  def fromMappableTraversableOnceFromMappable[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                                            K: Witness.Aux[K],
                                                                                            H: Lazy[FromMappable[H, M]],
                                                                                            T: FromMappable[T, M],
                                                                                            BMT: BaseMappableType[M],
                                                                                            CBF: CanBuildFrom[_, H, C[H]]): FromMappable[FieldType[K, C[H]] :: T, M] = new FromMappable[FieldType[K, C[H]] :: T, M] {
    override def apply(m: M): Option[::[FieldType[K, C[H]], T]] = for {
      tail <- T(m)
    } yield {
      val b = CBF()
      b ++= BMT.getAll(m, K.value.name).flatMap(x => H.value(x))

      field[K](b.result()) :: tail
    }
  }

  implicit def seqFromMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]) =
    fromMappableTraversableOnceFromMappable[K, H, T, Seq, M]

  implicit def setFromMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]) =
    fromMappableTraversableOnceFromMappable[K, H, T, Set, M]

  implicit def listFromMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]) =
    fromMappableTraversableOnceFromMappable[K, H, T, List, M]

}
