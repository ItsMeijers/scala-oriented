package oriented.maps

import shapeless._
import shapeless.labelled.FieldType

import scala.collection.generic.IsTraversableOnce

trait ToMappable[L, M] {
  def apply(l: L): M
}

trait LowerPrioToMappable {

  implicit def hnilToMappable[M](implicit mbt: BaseMappableType[M])
  : ToMappable[HNil, M] = new ToMappable[HNil, M] {
    override def apply(l: HNil): M = mbt.base
  }

  implicit def cnilToMappable[M](implicit mbt: BaseMappableType[M]): ToMappable[CNil, M] = new ToMappable[CNil, M] {
    override def apply(l: CNil): M = mbt.base
  }

  implicit def hconsToMappable0[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], bmt: BaseMappableType[M], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M])
  : ToMappable[FieldType[K, H] :: T, M] = new ToMappable[FieldType[K, H] :: T, M] {
    override def apply(l: FieldType[K, H] :: T): M =
      bmt.put(wit.value.name, H.value(l.head), T(l.tail))
  }


  implicit def hconsToMappable1[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], H: MappableType[M, H], T: ToMappable[T, M])
  : ToMappable[FieldType[K, H] :: T, M] = new ToMappable[FieldType[K, H] :: T, M] {
    override def apply(l: FieldType[K, H] :: T): M =
      H.put(wit.value.name, l.head, T(l.tail))
  }

  implicit def cconsToMappable[M, K <: Symbol, H, T <: Coproduct](implicit key: Witness.Aux[K], bmt: BaseMappableType[M], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M]): ToMappable[FieldType[K, H] :+: T, M] = new ToMappable[FieldType[K, H] :+: T, M] {
    override def apply(l: :+:[FieldType[K, H], T]): M = l match {
      case Inl(v) => bmt.put(s"__${key.value.name}", bmt.base, H.value(v))
      case Inr(r) => T.apply(r)
    }
  }

  implicit def generic[T, R, M](implicit gen: LabelledGeneric.Aux[T, R], repr: Lazy[ToMappable[R, M]], mbt: BaseMappableType[M]): ToMappable[T, M] = new ToMappable[T, M] {
    override def apply(l: T): M = repr.value(gen.to(l))
  }
}

object ToMappable extends LowerPrioToMappable {


  type IsTraversableOnceAux[Repr, A0] = IsTraversableOnce[Repr] { type A = A0 }

  implicit def optionMappable[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: ToMappable[T, M]): ToMappable[FieldType[K, Option[H]] :: T, M] =
    new ToMappable[FieldType[K, Option[H]] :: T, M] {
      override def apply(l: ::[FieldType[K, Option[H]], T]): M =
        H.put(K.value.name, l.head, T(l.tail))
    }

  implicit def optionToMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M]): ToMappable[FieldType[K, Option[H]] :: T, M] =
    new ToMappable[FieldType[K, Option[H]] :: T, M] {
      override def apply(l: ::[FieldType[K, Option[H]], T]): M =
        BMT.put(K.value.name, l.head.map(H.value.apply), T(l.tail))
    }


  implicit def mapMappable[S <: Symbol, H, T <: HList, M](implicit
                                                             S: Witness.Aux[S],
                                                             V: MappableType[M, H],
                                                             BMT: BaseMappableType[M],
                                                             T: ToMappable[T, M]): ToMappable[FieldType[S, Map[String, H]] :: T, M] =
    new ToMappable[FieldType[S, Map[String, H]] :: T, M] {
      override def apply(l: ::[FieldType[S, Map[String, H]], T]): M =
        BMT.put(S.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => V.put(k, v, acc) }, T(l.tail))
    }

  implicit def mapToMappable[S <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], S: Witness.Aux[S], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M]): ToMappable[FieldType[S, Map[String, H]] :: T, M] =
    new ToMappable[FieldType[S, Map[String, H]] :: T, M] {
      override def apply(l: ::[FieldType[S, Map[String, H]], T]): M =
        BMT.put(S.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => BMT.put(k, H.value.apply(v), acc) }, T(l.tail))
    }

  def toMappableTraversableOnceMappable[K <: Symbol, H, T <: HList, C[_], M](implicit
    K: Witness.Aux[K],
    H: MappableType[M, H],
    T: ToMappable[T, M],
    IS: IsTraversableOnceAux[C[H], H]
  ): ToMappable[FieldType[K, C[H]] :: T, M] = new ToMappable[FieldType[K, C[H]] :: T, M] {
    override def apply(l: ::[FieldType[K, C[H]], T]): M = {
      H.put(K.value.name, IS.conversion(l.head).toList, T(l.tail))
    }
  }


  implicit def seqMappable[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: ToMappable[T, M]) =
    toMappableTraversableOnceMappable[K, H, T, Seq, M]

  implicit def setMappable[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: ToMappable[T, M]) =
    toMappableTraversableOnceMappable[K, H, T, Set, M]

  implicit def listMappable[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: ToMappable[T, M]) =
    toMappableTraversableOnceMappable[K, H, T, List, M]

  implicit def vectorMappable[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: ToMappable[T, M]) =
    toMappableTraversableOnceMappable[K, H, T, Vector, M]

  def toMappableTraversableOnceToMappable[K <: Symbol, H, T <: HList, C[_], M](implicit
    K: Witness.Aux[K],
    BMT: BaseMappableType[M],
    H: Lazy[ToMappable[H, M]],
    T: ToMappable[T, M],
    IS: IsTraversableOnceAux[C[H], H]): ToMappable[FieldType[K, C[H]] :: T, M] = new ToMappable[FieldType[K, C[H]] :: T, M] {
    override def apply(l: ::[FieldType[K, C[H]], T]): M =
      BMT.put(K.value.name, IS.conversion(l.head).toList.map(H.value.apply), T(l.tail))
  }


  implicit def seqToMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M]) =
    toMappableTraversableOnceToMappable[K, H, T, Seq, M]

  implicit def setToMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M]) =
    toMappableTraversableOnceToMappable[K, H, T, Set, M]

  implicit def listToMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M]) =
    toMappableTraversableOnceToMappable[K, H, T, List, M]

  implicit def vectorToMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[ToMappable[H, M]], T: ToMappable[T, M]) =
    toMappableTraversableOnceToMappable[K, H, T, Vector, M]





}