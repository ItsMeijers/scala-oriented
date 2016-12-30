package enterprisedomain

import enumeratum._
import oriented.enumeratum.OrientedEnum

case class Wrapped[A](value: A)



sealed trait Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
case class Leaf[A](value: A) extends Tree[A]

sealed trait LastReservableTime

object LastReservableTime {

  case object SameAsLastTeeTime extends LastReservableTime

  case object RightBeforeSunset extends LastReservableTime

  case object OneHourBeforeSunset extends LastReservableTime

  case object TwoHoursBeforeSunset extends LastReservableTime

  case class Manual(time: Long) extends LastReservableTime

}


case class Time(offset: Long)

case class Range[T](from: T, till: T)

sealed abstract class WeekDay(override val entryName: String) extends EnumEntry

object WeekDay extends OrientedEnum[WeekDay] with Enum[WeekDay] {

  val values = findValues

  case object Monday extends WeekDay("Mon")
  case object Tuesday extends WeekDay("Tue")
  case object Wednesday extends WeekDay("Wed")
  case object Thursday extends WeekDay("Thu")
  case object Friday extends WeekDay("Fri")
  case object Saturday extends WeekDay("Sat")
  case object Sunday extends WeekDay("Sun")
}

final case class OpeningHours(map: Map[WeekDay, Set[Range[Time]]])