package oriented.enumeratum

import enumeratum._
import enumeratum.values.ValueEnumEntry
import oriented.maps.MappableType

trait OrientedEnum[EntryType <: EnumEntry] { enum: Enum[EntryType] =>

  implicit val enumeratumMapppable: MappableType[Map[String, Any], EntryType] =
    MappableType.string.xmapF(x => enum.withNameOption(x))(_.entryName)

}
