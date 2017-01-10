package enterprisedomain

case class Wrapped[A](value: A)



sealed trait Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
case class Leaf[A](value: A) extends Tree[A]

sealed trait FirstReservableTime

object FirstReservableTime {
  case object SameAsFirstTeeTime extends FirstReservableTime
  case object RightAfterSunrise  extends FirstReservableTime
  case class Manual(time: Time)  extends FirstReservableTime
}

sealed trait LastReservableTime

object LastReservableTime {
  case object SameAsLastTeeTime    extends LastReservableTime
  case object RightBeforeSunset    extends LastReservableTime
  case object OneHourBeforeSunset  extends LastReservableTime
  case object TwoHoursBeforeSunset extends LastReservableTime
  case class Manual(time: Time)    extends LastReservableTime
}

final case class Range[T](from: T, till: T)
final case class OpeningHours(map: Map[Day, Set[Range[Time]]])

sealed trait Day

object Day {
  case object Monday    extends Day
  case object Tuesday   extends Day
  case object Wednesday extends Day
  case object Thursday  extends Day
  case object Friday    extends Day
  case object Saturday  extends Day
  case object Sunday    extends Day
}

case class Time(minute: Int) extends AnyVal

case class BookingConditionSearchOptions(
                                          bookOnline: Boolean,
                                          daysInAdvance: Int,
                                          from: Time,
                                          player1: List[Day],
                                          player2: List[Day],
                                          player3: List[Day],
                                          player4: List[Day]
                                        )


case class BookingConditionAvailability(
                                         pairing: List[Day],
                                         holes18: OpeningHours,
                                         holes9: OpeningHours,
                                         firstReservableTime: FirstReservableTime,
                                         lastReservableTime: LastReservableTime
                                       )

case class BookingConditionChecks(bookingLimit: Option[Int], overlap: Boolean, maxHandicap: Int)
case class BookingConditionCancellationPolicy(numberHoursBeforeCancellation: Int)


case class BookingCondition(
                             mid: Long,
                             name: String,
                             searchOptions: BookingConditionSearchOptions,
                             availability: BookingConditionAvailability,
                             checks: BookingConditionChecks,
                             cancellationPolicy: BookingConditionCancellationPolicy
                           )
