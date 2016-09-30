package oriented.free.dsl

import cats.free.{Free, Inject}
import com.orientechnologies.orient.core.id.ORID
import oriented.Element

/**
  * Algebra for actions on the Element type class (super type of Vertex and Edge).
  */
sealed trait ElementDSL[A]

/**
  * Constructor for the action of retrieving the base class name of an element.
  * Action results in a String.
  */
case class GetBaseClassName[A](element: Element[A]) extends ElementDSL[String]

/**
  * Constructor for the action of retrieving the element type of an element. Either Vertex or Edge.
  * Action results in a String.
  */
case class GetElementType[A](element: Element[A]) extends ElementDSL[String]

/**
  * Constructor for the action of retrieving the Orient Identity of an element. Results in an ORID object.
  */
case class GetIdentity[A](element: Element[A]) extends ElementDSL[ORID]

/**
  * Constructor for the action of retrieving the label of an element. Results in an String.
  * (This will be the same value as field name in the orientFormat instance of A)
  */
case class GetLabel[A](element: Element[A]) extends ElementDSL[String]

/**
  * Constructor for the action of removing an element. Result of the action is Unit.
  */
case class RemoveElement[A](element: Element[A]) extends ElementDSL[Unit]

class Elements[F[_]](implicit inject: Inject[ElementDSL, F]) {

  def getBaseClassName[A](element: Element[A]): Free[F, String] =
    Free.inject[ElementDSL, F](GetBaseClassName(element))

  def getElementType[A](element: Element[A]): Free[F, String] =
    Free.inject[ElementDSL, F](GetElementType[A](element))

  def getIdentity[A](element: Element[A]): Free[F, ORID] =
    Free.inject[ElementDSL, F](GetIdentity[A](element))

  def getLabel[A](element: Element[A]): Free[F, String] =
    Free.inject[ElementDSL, F](GetLabel[A](element))

  def removeElement[A](element: Element[A]): Free[F, Unit] =
    Free.inject[ElementDSL, F](RemoveElement[A](element))

}

object Elements {
  def elements[F[_]](implicit inject: Inject[ElementDSL, F]): Elements[F] = new Elements[F]
}