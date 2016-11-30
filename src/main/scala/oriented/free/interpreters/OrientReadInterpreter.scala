package oriented.free.interpreters

import java.util.Date

import cats.implicits._
import cats.data.ReaderT
import oriented.free.dsl.{OrientRead, OrientReadF}

import scala.util.{Success, Try}

object MapInterpreter {

  def run[X](x: OrientRead[X], map: Map[String, Any]): Try[X] = {
    type InterpreterType[A] = ReaderT[Try, Map[String, Any], A]

    val interpreter = x.apply(new OrientReadF[InterpreterType] {
      override def pure[A](value: A): InterpreterType[A] = ReaderT(_ => Success(value))

      override def embedded[A](fieldName: String, read: InterpreterType[A]): InterpreterType[A] = ReaderT(m => Try(m(fieldName).asInstanceOf[Map[String, Any]]).flatMap(read.run))

      override def option[A](opt: InterpreterType[A]): InterpreterType[Option[A]] = ReaderT(map => Success(opt.run(map).toOption))

      override def list[A](fieldName: String, prg: InterpreterType[A]): InterpreterType[List[A]] =
        ReaderT(map => Try(map(fieldName).asInstanceOf[List[Map[String, Any]]]).flatMap(_.traverse(prg.run)))

      override def int(fieldName: String): InterpreterType[Int] = ReaderT(map => Try(map(fieldName).asInstanceOf[Int]))

      override def long(fieldName: String): InterpreterType[Long] = ReaderT(map => Try(map(fieldName).asInstanceOf[Long]))

      override def double(fieldName: String): InterpreterType[Double] = ReaderT(map => Try(map(fieldName).asInstanceOf[Double]))

      override def short(fieldName: String): InterpreterType[Short] = ReaderT(map => Try(map(fieldName).asInstanceOf[Short]))

      override def string(fieldName: String): InterpreterType[String] = ReaderT(map => Try(map(fieldName).asInstanceOf[String]))

      override def bigDecimal(fieldName: String): InterpreterType[BigDecimal] = ReaderT(map => Try(map(fieldName).asInstanceOf[BigDecimal]))

      override def date(fieldName: String): InterpreterType[Date] = ReaderT(map => Try(map(fieldName).asInstanceOf[Date]))

      override def flatMap[A, B](fa: InterpreterType[A])(f: (A) => InterpreterType[B]): InterpreterType[B] = fa.flatMap(f)

      override def tailRecM[A, B](a: A)(f: (A) => InterpreterType[Either[A, B]]): InterpreterType[B] = flatMap(f(a)) {
        case Left(left) => tailRecM(left)(f)
        case Right(right) => pure(right)
      }

      override def combineK[A](x: InterpreterType[A], y: InterpreterType[A]): InterpreterType[A] = ReaderT(m => x.run(m).orElse(y.run(m)))
    })

    interpreter.run(map)
  }
}