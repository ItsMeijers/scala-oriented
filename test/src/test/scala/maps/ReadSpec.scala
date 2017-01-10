package maps

import oriented._

trait ReadSpec {
  def roundTrip[A](value: A)(implicit OF: OrientFormat[A]): Boolean
}
