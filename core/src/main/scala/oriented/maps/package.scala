package oriented

import scala.collection.generic.IsTraversableOnce
import scala.reflect.ClassTag


package object maps {

  type IsTraversableOnceAux[Repr, A0] = IsTraversableOnce[Repr] { type A = A0 }

  def safeCast[T : ClassTag](value: Any): Option[T] =
    value match {
      case x: T => Some(x)
      case _ => None
    }

}
