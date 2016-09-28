package oriented.free.interpreters

import cats.data.EitherT
import cats.{Id, ~>}
import oriented.free.dsl._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * An ElementIntepreter forms a natural transformation from ElementDSL to a higher kinded G.
  */
sealed trait ElementInterpreter[G[_]] extends (ElementDSL ~> G) {

  /**
    * Interprets the ElementDSL[A] resulting in A.
    */
  def interpretDSL[A](fa: ElementDSL[A]): A = fa match {
    case GetBaseClassName(element) => element.orientElement.getBaseClassName
    case GetElementType(element)   => element.orientElement.getElementType
    case GetIdentity(element)      => element.orientElement.getIdentity
    case GetLabel(element)         => element.orientElement.getLabel
    case RemoveElement(element)    =>
      element.orientElement.remove()
      ()
  }

}

/**
  * The UnsafeElementInterpreter interprets the ElementDSL without any containment of side effect.
  */
object UnsafeElementInterpreter extends ElementInterpreter[Id] {

  /**
    * Transformation from ElementDSL to Id.
    */
  override def apply[A](fa: ElementDSL[A]): Id[A] = interpretDSL(fa)

}

/**
  * The TryElementInterpreter interprets the ElementDSL and contains all side effect in the Try Monad.
  */
object TryElementInterpreter extends ElementInterpreter[Try] {

  /**
    * Transformation from ElementDSL to Try.
    */
  override def apply[A](fa: ElementDSL[A]): Try[A] = Try(interpretDSL(fa))

}

/**
  * The AsyncElementInterpreter interprets each of the DSL components of one OrientAction in a Future, but does not
  * contain any side effect in the case of errors.
  */
case class AsyncElementInterpreter(implicit val executionContext: ExecutionContext) extends ElementInterpreter[Future] {

  /**
    * Transformation from ElementDSL to Future.
    */
  override def apply[A](fa: ElementDSL[A]): Future[A] = Future(interpretDSL(fa))

}

/**
  * The SafeAsyncElementInterpreter interprets each of the DSL Constructors in a Future but resulting into an Either
  * containing the errors of side effect in the Left branch of the Either instance.
  */
case class SafeAsyncElementInterpreter(implicit val executionContext: ExecutionContext) extends ElementInterpreter[EitherT[Future, Throwable, ?]] {

  /**
    * Transformation from ElementDSL to EitherT
    */
  override def apply[A](fa: ElementDSL[A]): EitherT[Future, Throwable, A] = EitherT(Future(
    Try(interpretDSL(fa)) match {
      case Failure(exc) => Left(exc)
      case Success(fa)  => Right(fa)
    }
  ))

}
