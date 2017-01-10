package oriented.maps

import cats.implicits._
import enum.Enum
import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.generic.{CanBuildFrom, IsTraversableOnce}

trait MapEncoder[L, M] {
  def apply(l: L): M
}

/**
  * The hcons case works here with `Lazy[MapEncoder[H, M]]`
  */
trait LowestPrioMapEncoder {

  implicit def hconsMapEncoder[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], bmt: BaseMappableType[M], H: Lazy[MapEncoder[H, M]], T: MapEncoder[T, M])
  : MapEncoder[FieldType[K, H] :: T, M] = new MapEncoder[FieldType[K, H] :: T, M] {
    override def apply(l: FieldType[K, H] :: T): M =
      bmt.put(wit.value.name, H.value(l.head), T(l.tail))
  }

  implicit def enumTraversableMapDecoderMapEncoder[K <: Symbol, E, H, T <: HList, C[_], M](implicit
                                                                                              BMT: BaseMappableType[M],
                                                                                              K: Witness.Aux[K],
                                                                                              E: Enum[E],
                                                                                              H: Lazy[MapEncoder[H, M]],
                                                                                              T: MapEncoder[T, M],
                                                                                              IS: IsTraversableOnceAux[C[H], H]): MapEncoder[FieldType[K, Map[E, C[H]]] :: T, M] =
    new MapEncoder[FieldType[K, Map[E, C[H]]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[E, C[H]]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => BMT.put(E.encode(k), IS.conversion(v).toList.map(H.value.apply), acc) }, T(l.tail))
    }

}

/**
  * The instances for MapEncoder here work with shapeless machinery
  *
  * Note the hcons case works with MappableType[M, H]
  */
trait ShapelessMapEncoder extends LowestPrioMapEncoder {

  implicit def hnilMapEncoder[M](implicit mbt: BaseMappableType[M])
  : MapEncoder[HNil, M] = new MapEncoder[HNil, M] {
    override def apply(l: HNil): M = mbt.base
  }

  implicit def cnilMapEncoder[M](implicit mbt: BaseMappableType[M]): MapEncoder[CNil, M] = new MapEncoder[CNil, M] {
    override def apply(l: CNil): M = mbt.base
  }


  implicit def hconsMappableType[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], H: MappableType[M, H], T: MapEncoder[T, M])
  : MapEncoder[FieldType[K, H] :: T, M] = new MapEncoder[FieldType[K, H] :: T, M] {
    override def apply(l: FieldType[K, H] :: T): M =
      H.put(wit.value.name, l.head, T(l.tail))
  }

  implicit def cconsMapEncoder[M, K <: Symbol, H, T <: Coproduct](implicit key: Witness.Aux[K], bmt: BaseMappableType[M], H: Lazy[MapEncoder[H, M]], T: MapEncoder[T, M]): MapEncoder[FieldType[K, H] :+: T, M] = new MapEncoder[FieldType[K, H] :+: T, M] {
    override def apply(l: :+:[FieldType[K, H], T]): M = l match {
      case Inl(v) => bmt.put(s"__${key.value.name}", bmt.base, H.value(v))
      case Inr(r) => T.apply(r)
    }
  }

  implicit def genericMapEncoder[T, R, M](implicit gen: LabelledGeneric.Aux[T, R], repr: Lazy[MapEncoder[R, M]], mbt: BaseMappableType[M]): MapEncoder[T, M] = new MapEncoder[T, M] {
    override def apply(l: T): M = repr.value(gen.to(l))
  }
}

/**
  * The instances for MapEncoder here work with a `H` which is a Lazy[MapEncoder[H, M]
  */
trait MapEncoderMapEncoders extends ShapelessMapEncoder {

  implicit def optionMapEncoderMapEncoder[K <: Symbol, H, T <: HList, M](implicit
                                                               BMT: BaseMappableType[M],
                                                               K: Witness.Aux[K],
                                                               H: Lazy[MapEncoder[H, M]],
                                                               T: MapEncoder[T, M]): MapEncoder[FieldType[K, Option[H]] :: T, M] =
    new MapEncoder[FieldType[K, Option[H]] :: T, M] {
      override def apply(l: ::[FieldType[K, Option[H]], T]): M =
        BMT.put(K.value.name, l.head.map(H.value.apply), T(l.tail))
    }

  implicit def mapMapEncoderMapEncoder[K <: Symbol, H, T <: HList, M](implicit
                                                            K: Witness.Aux[K],
                                                            BMT: BaseMappableType[M],
                                                            H: Lazy[MapEncoder[H, M]],
                                                            T: MapEncoder[T, M]): MapEncoder[FieldType[K, Map[String, H]] :: T, M] =
    new MapEncoder[FieldType[K, Map[String, H]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[String, H]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => BMT.put(k, H.value.apply(v), acc) }, T(l.tail))
    }

  implicit def mapTraversableMapEncoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                                 K: Witness.Aux[K],
                                                                                 BMT: BaseMappableType[M],
                                                                                 H: Lazy[MapEncoder[H, M]],
                                                                                 T: MapEncoder[T, M],
                                                                                 IS: IsTraversableOnceAux[C[H], H]): MapEncoder[FieldType[K, Map[String, C[H]]] :: T, M] =
    new MapEncoder[FieldType[K, Map[String, C[H]]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[String, C[H]]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => BMT.put(k, IS.conversion(v).toList.map(H.value.apply), acc) }, T(l.tail))
    }

  def traversableMapEncoderMapEncoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                               K: Witness.Aux[K],
                                                                               BMT: BaseMappableType[M],
                                                                               H: Lazy[MapEncoder[H, M]],
                                                                               T: MapEncoder[T, M],
                                                                               IS: IsTraversableOnceAux[C[H], H]): MapEncoder[FieldType[K, C[H]] :: T, M] = new MapEncoder[FieldType[K, C[H]] :: T, M] {
    override def apply(l: ::[FieldType[K, C[H]], T]): M =
      BMT.put(K.value.name, IS.conversion(l.head).toList.map(H.value.apply), T(l.tail))
  }


  implicit def seqMapEncoderMapEncoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapEncoder[H, M]], T: MapEncoder[T, M]): MapEncoder[FieldType[K, Seq[H]] :: T, M]  =
    traversableMapEncoderMapEncoder[K, H, T, Seq, M]

  implicit def setMapEncoderMapEncoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapEncoder[H, M]], T: MapEncoder[T, M]): MapEncoder[FieldType[K, Set[H]] :: T, M]  =
    traversableMapEncoderMapEncoder[K, H, T, Set, M]

  implicit def listMapEncoderMapEncoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapEncoder[H, M]], T: MapEncoder[T, M]): MapEncoder[FieldType[K, List[H]] :: T, M]  =
    traversableMapEncoderMapEncoder[K, H, T, List, M]

  implicit def vectorMapEncoderMapEncoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapEncoder[H, M]], T: MapEncoder[T, M]): MapEncoder[FieldType[K, Vector[H]] :: T, M]  =
    traversableMapEncoderMapEncoder[K, H, T, Vector, M]


}

/**
  * The instances for MapEncoder here work with a `H` which is a MappableType[M, H]
  */
trait MappableTypeMapEncoders extends MapEncoderMapEncoders {
  implicit def optionMappableTypeMapEncoder[K <: Symbol, H, T <: HList, M](implicit
                                                     K: Witness.Aux[K],
                                                     H: MappableType[M, H],
                                                     T: MapEncoder[T, M]): MapEncoder[FieldType[K, Option[H]] :: T, M] =
    new MapEncoder[FieldType[K, Option[H]] :: T, M] {
      override def apply(l: ::[FieldType[K, Option[H]], T]): M =
        H.put(K.value.name, l.head, T(l.tail))
    }




  implicit def mapMappableTypeMapEncoder[K <: Symbol, H, T <: HList, M](implicit
                                                          K: Witness.Aux[K],
                                                          BMT: BaseMappableType[M],
                                                          H: MappableType[M, H],
                                                          T: MapEncoder[T, M]): MapEncoder[FieldType[K, Map[String, H]] :: T, M] =
    new MapEncoder[FieldType[K, Map[String, H]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[String, H]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => H.put(k, v, acc) }, T(l.tail))
    }


  implicit def mapTraversableMappableTypeMapEncoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                               K: Witness.Aux[K],
                                                                               BMT: BaseMappableType[M],
                                                                               H: MappableType[M, H],
                                                                               T: MapEncoder[T, M],
                                                                               IS: IsTraversableOnceAux[C[H], H]): MapEncoder[FieldType[K, Map[String, C[H]]] :: T, M] =
    new MapEncoder[FieldType[K, Map[String, C[H]]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[String, C[H]]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => H.put(k, IS.conversion(v).toList, acc) }, T(l.tail))
    }

  def traversableMappableTypeMapEncoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                             K: Witness.Aux[K],
                                                                             H: MappableType[M, H],
                                                                             T: MapEncoder[T, M],
                                                                             IS: IsTraversableOnceAux[C[H], H]
                                                                            ): MapEncoder[FieldType[K, C[H]] :: T, M] = new MapEncoder[FieldType[K, C[H]] :: T, M] {
    override def apply(l: ::[FieldType[K, C[H]], T]): M = {
      H.put(K.value.name, IS.conversion(l.head).toList, T(l.tail))
    }
  }


  implicit def seqMappableTypeMapEncoder[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: MapEncoder[T, M]): MapEncoder[FieldType[K, Seq[H]] :: T, M] =
    traversableMappableTypeMapEncoder[K, H, T, Seq, M]

  implicit def setMappableTypeMapEncoder[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: MapEncoder[T, M]): MapEncoder[FieldType[K, Set[H]] :: T, M] =
    traversableMappableTypeMapEncoder[K, H, T, Set, M]

  implicit def listMappableTypeMapEncoder[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: MapEncoder[T, M]): MapEncoder[FieldType[K, List[H]] :: T, M]  =
    traversableMappableTypeMapEncoder[K, H, T, List, M]

  implicit def vectorMappableTypeMapEncoder[K <: Symbol, H, T <: HList, M](implicit K: Witness.Aux[K], H: MappableType[M, H], T: MapEncoder[T, M]): MapEncoder[FieldType[K, Vector[H]] :: T, M]  =
    traversableMappableTypeMapEncoder[K, H, T, Vector, M]

  implicit def enumTraversableMappableTypeMapEncoder[K <: Symbol, E, H, T <: HList, C[_], M](implicit
                                                                                             BMT: BaseMappableType[M],
                                                                                             K: Witness.Aux[K],
                                                                                             E: Enum[E],
                                                                                             H: MappableType[M, H],
                                                                                             T: MapEncoder[T, M],
                                                                                             IS: IsTraversableOnceAux[C[H], H]): MapEncoder[FieldType[K, Map[E, C[H]]] :: T, M] =
    new MapEncoder[FieldType[K, Map[E, C[H]]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[E, C[H]]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => H.put(E.encode(k), IS.conversion(v).toList, acc) }, T(l.tail))
    }
}

object MapEncoder extends MappableTypeMapEncoders