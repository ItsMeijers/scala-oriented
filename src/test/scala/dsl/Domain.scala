package dsl

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

//case class WrappedLastReservableTime(value: LastReservableTime)