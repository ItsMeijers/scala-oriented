package maps
import oriented.OrientFormat

trait LocalReadSpec extends ReadSpec {

  override def roundTrip[A](value: A)
                           (implicit OF: OrientFormat[A]): Boolean = {
    val map = OF.properties(value)

    println(s"map: $map")

    OF.readerMap.run(map) == value
  }

}
