package oriented.maps

import cats.implicits._

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.generic.{CanBuildFrom, IsTraversableOnce}

trait FromMappable[L, M] {
  def apply(m: M): Option[L]
}

trait EvenLowerPrioFromMappable {
  implicit def hconsFromMappable0[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], bmt: BaseMappableType[M], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M])
  : FromMappable[FieldType[K, H] :: T, M] = new FromMappable[FieldType[K, H] :: T, M] {
    override def apply(m: M): Option[FieldType[K, H] :: T] = for {
      map <- bmt.get(m, wit.value.name)
      h <- H.value(map)
      t <- T(m)
    } yield field[K](h) :: t
  }
}

trait LowerPrioFromMappable extends EvenLowerPrioFromMappable {
  implicit def hnilFromMappable[M]: FromMappable[HNil, M] = new FromMappable[HNil, M] {
    override def apply(m: M): Option[HNil] = Some(HNil)
  }

  implicit def cnilFromMappable[M]: FromMappable[CNil, M] = new FromMappable[CNil, M] {
    override def apply(m: M): Option[CNil] = None
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

  implicit def optionMappable[K <: Symbol, H, T <: HList, M](implicit
    K: Witness.Aux[K],
    H: MappableType[M, H],
    T: FromMappable[T, M]): FromMappable[FieldType[K, Option[H]] :: T, M] =
    new FromMappable[FieldType[K, Option[H]] :: T, M] {
      override def apply(m: M): Option[::[FieldType[K, Option[H]], T]] = for {
        tail <- T(m)
      } yield {
        field[K](H.get(m, K.value.name)) :: tail
      }
    }

  implicit def optionFromMappable[K <: Symbol, H, T <: HList, M](implicit
                                                                 BMT: BaseMappableType[M],
                                                                 K: Witness.Aux[K],
                                                                 H: Lazy[FromMappable[H, M]],
                                                                 T: FromMappable[T, M]): FromMappable[FieldType[K, Option[H]] :: T, M] =
    new FromMappable[FieldType[K, Option[H]] :: T, M] {
      override def apply(m: M): Option[::[FieldType[K, Option[H]], T]] = for {
        tail <- T(m)
      } yield {
        field[K](BMT.get(m, K.value.name).flatMap(H.value.apply)) :: tail
      }
    }

  implicit def mapMappable[K <: Symbol, H, T <: HList, M](implicit
                                                          BMT: BaseMappableType[M],
                                                          K: Witness.Aux[K],
                                                          H: MappableType[M, H],
                                                          T: FromMappable[T, M]): FromMappable[FieldType[K, Map[String, H]] :: T, M] =
    new FromMappable[FieldType[K, Map[String, H]] :: T, M] {
      override def apply(m: M): Option[FieldType[K, Map[String, H]] :: T] = for {
        map <- BMT.get(m, K.value.name)
        keys = BMT.keys(map)
        pairs <- keys.toList.traverse[Option, (String, H)](key => H.get(map, key).map(v => key -> v))
        tail <- T(m)
      } yield {
        field[K](pairs.toMap) :: tail
      }
    }

  implicit def mapTraversableOnceMappable[K <: Symbol, H, T <: HList, M, C[_]](implicit
                                                          BMT: BaseMappableType[M],
                                                          K: Witness.Aux[K],
                                                          H: MappableType[M, H],
                                                          T: FromMappable[T, M],
                                                          CBF: CanBuildFrom[_, H, C[H]]): FromMappable[FieldType[K, Map[String, C[H]]] :: T, M] = new FromMappable[FieldType[K, Map[String, C[H]]] :: T, M] {
    override def apply(m: M): Option[::[FieldType[K, Map[String, C[H]]], T]] = for {
      map <- BMT.get(m, K.value.name)
      keys = BMT.keys(map)
      pairs = keys.toList.map { key =>
        val b = CBF()
        b ++= H.getAll(map, key)
        key -> b.result()
      }
      tail <- T(m)
    } yield {
      field[K](pairs.toMap) :: tail
    }
  }

  implicit def mapTraversableOnceFromMappable[K <: Symbol, H, T <: HList, M, C[_]](implicit
                                                                               BMT: BaseMappableType[M],
                                                                               K: Witness.Aux[K],
                                                                               H: Lazy[FromMappable[H, M]],
                                                                               T: FromMappable[T, M],
                                                                               CBF: CanBuildFrom[_, H, C[H]]): FromMappable[FieldType[K, Map[String, C[H]]] :: T, M] = new FromMappable[FieldType[K, Map[String, C[H]]] :: T, M] {
    override def apply(m: M): Option[::[FieldType[K, Map[String, C[H]]], T]] = for {
      map <- BMT.get(m, K.value.name)
      keys = BMT.keys(map)
      pairs <- keys.toList.traverse[Option, (String, C[H])] { key =>
        val members = BMT.getAll(map, key)
        val results = members.toList.traverse[Option, H](x => H.value.apply(x))
        results.map { v =>
          val b = CBF()
          b ++= v
          key -> b.result()
        }
      }
      tail <- T(m)
    } yield {
      field[K](pairs.toMap) :: tail
    }
  }

  implicit def mapFromMappable[K <: Symbol, H, T <: HList, M](implicit
                                                              BMT: BaseMappableType[M],
                                                              K: Witness.Aux[K],
                                                              H: Lazy[FromMappable[H, M]],
                                                              T: FromMappable[T, M]): FromMappable[FieldType[K, Map[String, H]] :: T, M] =
    new FromMappable[FieldType[K, Map[String, H]] :: T, M] {
      override def apply(m: M): Option[FieldType[K, Map[String, H]] :: T] = for {
        map <- BMT.get(m, K.value.name)
        keys = BMT.keys(map)
        pairs <- keys.toList.traverse[Option, (String, H)](key => BMT.get(map, key).flatMap(m => H.value(m).map(v => key -> v)))
        tail <- T(m)
      } yield {
        field[K](pairs.toMap) :: tail
      }
    }

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


  implicit def seqMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: FromMappable[T, M]): FromMappable[FieldType[K, Seq[H]] :: T, M] =
    fromMappableTraversableOnceMappable[K, H, T, Seq, M]

  implicit def setMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: FromMappable[T, M]): FromMappable[FieldType[K, Set[H]] :: T, M] =
    fromMappableTraversableOnceMappable[K, H, T, Set, M]

  implicit def listMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: FromMappable[T, M]): FromMappable[FieldType[K, List[H]] :: T, M] =
    fromMappableTraversableOnceMappable[K, H, T, List, M]

  implicit def vectorMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: FromMappable[T, M]): FromMappable[FieldType[K, Vector[H]] :: T, M] =
    fromMappableTraversableOnceMappable[K, H, T, Vector, M]

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

  implicit def seqFromMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]): FromMappable[FieldType[K, Seq[H]] :: T, M] =
    fromMappableTraversableOnceFromMappable[K, H, T, Seq, M]

  implicit def setFromMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]): FromMappable[FieldType[K, Set[H]] :: T, M] =
    fromMappableTraversableOnceFromMappable[K, H, T, Set, M]

  implicit def listFromMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]): FromMappable[FieldType[K, List[H]] :: T, M] =
    fromMappableTraversableOnceFromMappable[K, H, T, List, M]

  implicit def vectorFromMappable[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[FromMappable[H, M]], T: FromMappable[T, M]): FromMappable[FieldType[K, Vector[H]] :: T, M] =
    fromMappableTraversableOnceFromMappable[K, H, T, Vector, M]

}
