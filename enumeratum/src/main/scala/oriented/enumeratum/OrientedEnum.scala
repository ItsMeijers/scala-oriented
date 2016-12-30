package oriented.enumeratum

import cats.implicits._
import enumeratum._
import oriented.maps._
import shapeless.labelled.{FieldType, field}
import shapeless._

import scala.collection.generic.CanBuildFrom

trait OrientedEnumLowerPrioImplicits[EntryType <: EnumEntry] { enum: Enum[EntryType] =>

  implicit def enumeratumTraversableMapDecoderMapDecoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                                          BMT: BaseMappableType[M],
                                                                                          K: Witness.Aux[K],
                                                                                          H: Lazy[MapDecoder[H, M]],
                                                                                          T: MapDecoder[T, M],
                                                                                          CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] =
    new MapDecoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] {
      def apply(m: M): Option[FieldType[K, Map[EntryType, C[H]]] :: T] =
        for {
          map <- BMT.get(m, K.value.name)
          keys = BMT.keys(map)
          pairs <- keys.toList.traverse[Option, (EntryType, C[H])] { key =>
            for {
              entryType <- enum.withNameOption(key)
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

  implicit def enumeratumTraversableMapDecoderMapEncoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                                      BMT: BaseMappableType[M],
                                                                                      K: Witness.Aux[K],
                                                                                      H: Lazy[MapEncoder[H, M]],
                                                                                      T: MapEncoder[T, M],
                                                                                      IS: IsTraversableOnceAux[C[H], H]): MapEncoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] =
    new MapEncoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[EntryType, C[H]]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => BMT.put(k.entryName, IS.conversion(v).toList.map(H.value.apply), acc) }, T(l.tail))
    }

}

trait OrientedEnum[EntryType <: EnumEntry] extends OrientedEnumLowerPrioImplicits[EntryType] { enum: Enum[EntryType] =>

  implicit val enumeratumMapppable: MappableType[Map[String, Any], EntryType] =
    MappableType.string.xmapF(x => enum.withNameOption(x))(_.entryName)


  implicit def enumeratumTraversableMappableTypeMapEncoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                                          BMT: BaseMappableType[M],
                                                                                          K: Witness.Aux[K],
                                                                                          H: MappableType[M, H],
                                                                                          T: MapDecoder[T, M],
                                                                                          CBF: CanBuildFrom[_, H, C[H]]): MapDecoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] =
    new MapDecoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] {
      def apply(m: M): Option[FieldType[K, Map[EntryType, C[H]]] :: T] =
        for {
          map <- BMT.get(m, K.value.name)
          keys = BMT.keys(map)
          pairs <- keys.toList.traverse[Option, (EntryType, C[H])] { key =>
            for {
              entryType <- enum.withNameOption(key)
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

  implicit def enumeratumTraversableMappableTypeMapDecoder[K <: Symbol, H, T <: HList, C[_], M](implicit
                                                                                        BMT: BaseMappableType[M],
                                                                                        K: Witness.Aux[K],
                                                                                        H: MappableType[M, H],
                                                                                        T: MapEncoder[T, M],
                                                                                        IS: IsTraversableOnceAux[C[H], H]): MapEncoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] =
    new MapEncoder[FieldType[K, Map[EntryType, C[H]]] :: T, M] {
      override def apply(l: ::[FieldType[K, Map[EntryType, C[H]]], T]): M =
        BMT.put(K.value.name, l.head.foldLeft(BMT.base) { case (acc, (k,v)) => H.put(k.entryName, IS.conversion(v).toList, acc) }, T(l.tail))
    }



}
