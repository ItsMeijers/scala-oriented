package oriented.maps

import cats.implicits._
import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.generic.CanBuildFrom

import enum._

trait MapDecoder[A, M] {
  def apply(m: M): Option[A]
}

/**
  * The hcons case works here with `Lazy[MapDecoder[H, M]]`
  */
trait LowestPrioMapDecoder {
  implicit def hconsMapDecoder[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], bmt: BaseMappableType[M], H: Lazy[MapDecoder[H, M]], T: MapDecoder[T, M])
  : MapDecoder[FieldType[K, H] :: T, M] = new MapDecoder[FieldType[K, H] :: T, M] {
    override def apply(m: M): Option[FieldType[K, H] :: T] = for {
      map <- bmt.get(m, wit.value.name)
      h <- H.value(map)
      t <- T(m)
    } yield field[K](h) :: t
  }

  implicit def enumTraversableMapDecoderMapDecoder[K <: Symbol, E, H, T <: HList, C[_], M](implicit
                                                                                              BMT: BaseMappableType[M],
                                                                                              E: Enum[E],
                                                                                              K: Witness.Aux[K],
                                                                                              H: Lazy[MapDecoder[H, M]],
                                                                                              T: MapDecoder[T, M],
                                                                                              CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, Map[E, C[H]]] :: T, M] =
    new MapDecoder[FieldType[K, Map[E, C[H]]] :: T, M] {
      def apply(m: M): Option[FieldType[K, Map[E, C[H]]] :: T] =
        for {
          map <- BMT.get(m, K.value.name)
          keys = BMT.keys(map)
          pairs <- keys.toList.traverse[Option, (E, C[H])] { key =>
            for {
              entryType <- E.decodeOpt(key)
              rawEntries = BMT.getAll(map, key)
              entries <- rawEntries.toList.traverse(H.value.apply)
            } yield {
              val b = CBF()
              b ++= entries
              entryType -> b.result()
            }
          }
          tail <- T(m)
        } yield {
          field[K](pairs.toMap) :: tail
        }
    }
}

/**
  * The instances for MapDecoder here work with shapeless machinery
  *
  * Note the hcons case works with MappableType[M, H]
  */
trait ShapelessMapDecoder extends LowestPrioMapDecoder {
  implicit def hnilMapDecoder[M]: MapDecoder[HNil, M] = new MapDecoder[HNil, M] {
    override def apply(m: M): Option[HNil] = Some(HNil)
  }

  implicit def cnilMapDecoder[M]: MapDecoder[CNil, M] = new MapDecoder[CNil, M] {
    override def apply(m: M): Option[CNil] = None
  }

  implicit def hconsMappableType[K <: Symbol, H, T <: HList, M]
  (implicit wit: Witness.Aux[K], mbt: BaseMappableType[M], H: MappableType[M, H], T: MapDecoder[T, M])
  : MapDecoder[FieldType[K, H] :: T, M] = new MapDecoder[FieldType[K, H] :: T, M] {
    override def apply(m: M): Option[FieldType[K, H] :: T] = for {
      h <- H.get(m, wit.value.name)
      t <- T(m)
    } yield field[K](h) :: t
  }

  implicit def cconsMapDecoder[M, K <: Symbol, H, T <: Coproduct](implicit key: Witness.Aux[K], mt: BaseMappableType[M], H: Lazy[MapDecoder[H, M]], T: MapDecoder[T, M]): MapDecoder[FieldType[K, H] :+: T, M] = new MapDecoder[FieldType[K, H] :+: T, M] {
    override def apply(m: M): Option[:+:[FieldType[K, H], T]] = {
      if(mt.get(m, s"__${key.value.name}").nonEmpty) H.value(m).map(v => Inl(field[K](v)))
      else T(m).map(Inr.apply)
    }
  }

  implicit def genericMapDecoder[T, R, M](implicit gen: LabelledGeneric.Aux[T, R], repr: Lazy[MapDecoder[R, M]]): MapDecoder[T, M] = new MapDecoder[T, M] {
    override def apply(m: M): Option[T] = repr.value(m).map(x => gen.from(x))
  }
}

/**
  * The instances for MapDecoder here work with a `H` which is a Lazy[MapDecoder[H, M]
  */
trait MapDecoderMapDecoders extends ShapelessMapDecoder {

  implicit def optionMapDecoderMapDecoder[K <: Symbol, H, T <: HList, M](implicit
                                                                 BMT: BaseMappableType[M],
                                                                 K: Witness.Aux[K],
                                                                 H: Lazy[MapDecoder[H, M]],
                                                                 T: MapDecoder[T, M]): MapDecoder[FieldType[K, Option[H]] :: T, M] =
    new MapDecoder[FieldType[K, Option[H]] :: T, M] {
      override def apply(m: M): Option[::[FieldType[K, Option[H]], T]] = for {
        tail <- T(m)
      } yield {
        field[K](BMT.get(m, K.value.name).flatMap(H.value.apply)) :: tail
      }
    }

  implicit def mapTraversableOnceMapDecoderMapDecoder[K <: Symbol, H, T <: HList, M, C[_]](implicit
                                                                                   BMT: BaseMappableType[M],
                                                                                   K: Witness.Aux[K],
                                                                                   H: Lazy[MapDecoder[H, M]],
                                                                                   T: MapDecoder[T, M],
                                                                                   CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, Map[String, C[H]]] :: T, M] = new MapDecoder[FieldType[K, Map[String, C[H]]] :: T, M] {
    override def apply(m: M): Option[::[FieldType[K, Map[String, C[H]]], T]] = for {
      map <- BMT.get(m, K.value.name)
      keys = BMT.keys(map)
      pairs <- keys.toList.traverse[Option, (String, C[H])] { key =>
        val members = BMT.getAll(map, key)
        val results = members.toList.traverse(H.value.apply)
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


  implicit def mapMapDecoderMapDecoder[K <: Symbol, H, T <: HList, M](implicit
                                                              BMT: BaseMappableType[M],
                                                              K: Witness.Aux[K],
                                                              H: Lazy[MapDecoder[H, M]],
                                                              T: MapDecoder[T, M]): MapDecoder[FieldType[K, Map[String, H]] :: T, M] =
    new MapDecoder[FieldType[K, Map[String, H]] :: T, M] {
      override def apply(m: M): Option[FieldType[K, Map[String, H]] :: T] = for {
        map <- BMT.get(m, K.value.name)
        keys = BMT.keys(map)
        pairs <- keys.toList.traverse[Option, (String, H)](key => BMT.get(map, key).flatMap(m => H.value(m).map(v => key -> v)))
        tail <- T(m)
      } yield {
        field[K](pairs.toMap) :: tail
      }
    }

  private def traversableMapDecoderMapDecoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                           K: Witness.Aux[K],
                                                                           H: Lazy[MapDecoder[H, M]],
                                                                           T: MapDecoder[T, M],
                                                                           BMT: BaseMappableType[M],
                                                                           CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, C[H]] :: T, M] = new MapDecoder[FieldType[K, C[H]] :: T, M] {
    override def apply(m: M): Option[::[FieldType[K, C[H]], T]] = for {
      tail <- T(m)
    } yield {
      val b = CBF()
      b ++= BMT.getAll(m, K.value.name).flatMap(x => H.value(x))

      field[K](b.result()) :: tail
    }
  }

  implicit def seqMapDecoderMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapDecoder[H, M]], T: MapDecoder[T, M]): MapDecoder[FieldType[K, Seq[H]] :: T, M] =
    traversableMapDecoderMapDecoder[K, H, T, Seq, M]

  implicit def setMapDecoderMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapDecoder[H, M]], T: MapDecoder[T, M]): MapDecoder[FieldType[K, Set[H]] :: T, M] =
    traversableMapDecoderMapDecoder[K, H, T, Set, M]

  implicit def listMapDecoderMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapDecoder[H, M]], T: MapDecoder[T, M]): MapDecoder[FieldType[K, List[H]] :: T, M] =
    traversableMapDecoderMapDecoder[K, H, T, List, M]

  implicit def vectorMapDecoderMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: Lazy[MapDecoder[H, M]], T: MapDecoder[T, M]): MapDecoder[FieldType[K, Vector[H]] :: T, M] =
    traversableMapDecoderMapDecoder[K, H, T, Vector, M]



  implicit def enumTraversableMappableTypeMapDecoder[K <: Symbol, E, H, T <: HList, C[_], M](implicit
                                                                                             BMT: BaseMappableType[M],
                                                                                             K: Witness.Aux[K],
                                                                                             E: Enum[E],
                                                                                             H: MappableType[M, H],
                                                                                             T: MapDecoder[T, M],
                                                                                             CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, Map[E, C[H]]] :: T, M] =
    new MapDecoder[FieldType[K, Map[E, C[H]]] :: T, M] {
      def apply(m: M): Option[FieldType[K, Map[E, C[H]]] :: T] =
        for {
          map <- BMT.get(m, K.value.name)
          keys = BMT.keys(map)
          pairs <- keys.toList.traverse[Option, (E, C[H])] { key =>
            for {
              entryType <- E.decodeOpt(key)
            } yield {
              val entries = H.getAll(map, key)
              val b = CBF()
              b ++= entries
              entryType -> b.result()
            }
          }
          tail <- T(m)
        } yield {
          field[K](pairs.toMap) :: tail
        }
    }
}

/**
  * The instances for MapDecoder here work with a `H` which is a MappableType[M, H]
  */
trait MappableTypeMapDecoders extends MapDecoderMapDecoders {
  implicit def optionMappableType[K <: Symbol, H, T <: HList, M](implicit
                                                             K: Witness.Aux[K],
                                                             H: MappableType[M, H],
                                                             T: MapDecoder[T, M]): MapDecoder[FieldType[K, Option[H]] :: T, M] =
    new MapDecoder[FieldType[K, Option[H]] :: T, M] {
      override def apply(m: M): Option[::[FieldType[K, Option[H]], T]] = for {
        tail <- T(m)
      } yield {
        field[K](H.get(m, K.value.name)) :: tail
      }
    }


  implicit def mapMappableType[K <: Symbol, H, T <: HList, M](implicit
                                                          BMT: BaseMappableType[M],
                                                          K: Witness.Aux[K],
                                                          H: MappableType[M, H],
                                                          T: MapDecoder[T, M]): MapDecoder[FieldType[K, Map[String, H]] :: T, M] =
    new MapDecoder[FieldType[K, Map[String, H]] :: T, M] {
      override def apply(m: M): Option[FieldType[K, Map[String, H]] :: T] = for {
        map <- BMT.get(m, K.value.name)
        keys = BMT.keys(map)
        pairs <- keys.toList.traverse[Option, (String, H)](key => H.get(map, key).map(v => key -> v))
        tail <- T(m)
      } yield {
        field[K](pairs.toMap) :: tail
      }
    }



  implicit def mapTraversableMappableType[K <: Symbol, H, T <: HList, M, C[_]](implicit
                                                                               BMT: BaseMappableType[M],
                                                                               K: Witness.Aux[K],
                                                                               H: MappableType[M, H],
                                                                               T: MapDecoder[T, M],
                                                                               CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, Map[String, C[H]]] :: T, M] =
    new MapDecoder[FieldType[K, Map[String, C[H]]] :: T, M] {
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



  private def traversableMappableTypeMapDecoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                             K: Witness.Aux[K],
                                                                             H: MappableType[M, H],
                                                                             T: MapDecoder[T, M],
                                                                             BMT: BaseMappableType[M],
                                                                             CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, C[H]] :: T, M] =
    new MapDecoder[FieldType[K, C[H]] :: T, M] {
      override def apply(m: M): Option[::[FieldType[K, C[H]], T]] = for {
        tail <- T(m)
      } yield {
        val b = CBF()
        b ++= H.getAll(m, K.value.name)
        field[K](b.result()) :: tail
      }
    }


  implicit def seqMappableTypeMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: MapDecoder[T, M]): MapDecoder[FieldType[K, Seq[H]] :: T, M] =
    traversableMappableTypeMapDecoder[K, H, T, Seq, M]

  implicit def setMappableTypeMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: MapDecoder[T, M]): MapDecoder[FieldType[K, Set[H]] :: T, M] =
    traversableMappableTypeMapDecoder[K, H, T, Set, M]

  implicit def listMappableTypeMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: MapDecoder[T, M]): MapDecoder[FieldType[K, List[H]] :: T, M] =
    traversableMappableTypeMapDecoder[K, H, T, List, M]

  implicit def vectorMappableTypeMapDecoder[K <: Symbol, H, T <: HList, M](implicit BMT: BaseMappableType[M], K: Witness.Aux[K], H: MappableType[M, H], T: MapDecoder[T, M]): MapDecoder[FieldType[K, Vector[H]] :: T, M] =
    traversableMappableTypeMapDecoder[K, H, T, Vector, M]
}

object MapDecoder extends MappableTypeMapDecoders
