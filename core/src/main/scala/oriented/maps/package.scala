package oriented

import scala.reflect.ClassTag


package object maps {

  def safeCast[T : ClassTag](value: Any): Option[T] =
    value match {
      case x: T => Some(x)
      case _ => None
    }

}
