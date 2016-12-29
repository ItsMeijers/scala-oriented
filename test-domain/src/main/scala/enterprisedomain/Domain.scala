package enterprisedomain

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

sealed trait Day {
  val str: String
}

case class Time(offset: Long)

case class Range[T](from: T, till: T)

object Day {

  case object Monday extends Day { val str = "Mon" }
  case object Tuesday extends Day { val str = "Tue" }
  case object Wednesday extends Day { val str = "Wed" }
  case object Thursday extends Day { val str = "Thu" }
  case object Friday extends Day { val str = "Fri" }
  case object Saturday extends Day { val str = "Sat" }
  case object Sunday extends Day { val str = "Sun" }

  val all: List[Day] = List(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)

  def fromString(str: String): Day = all.find(_.str == str).getOrElse(sys.error("Unable to parse day"))
}

final case class OpeningHours(map: Map[Day, Set[Range[Time]]])