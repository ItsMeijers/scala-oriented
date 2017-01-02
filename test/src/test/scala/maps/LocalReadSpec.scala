package maps
import oriented.OrientFormat

trait LocalReadSpec extends ReadSpec {

  override def roundTrip[A](value: A)
                           (implicit OF: OrientFormat[A]): Boolean =
    OF.readerMap.run(OF.properties(value)) == value
}
