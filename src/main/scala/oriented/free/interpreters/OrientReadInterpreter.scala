package oriented.free.interpreters

import java.util.Date

import cats.data.Reader
import oriented.free.dsl.{OrientRead, OrientReadF}

import scala.util.Try

object MapInterpreter {

  def run[X](x: OrientRead[X], map: Map[String, Any]): X = {
    type InterpreterType[A] = Reader[Map[String, Any], A]

    val interpreter = x.apply(new OrientReadF[InterpreterType] {
      override def pure[A](value: A): InterpreterType[A] = Reader(_ => value)

      override def embedded[A](fieldName: String, read: InterpreterType[A]): InterpreterType[A] = Reader(map => read.run(map(fieldName).asInstanceOf[Map[String, Any]]))

      override def option[A](opt: InterpreterType[A]): InterpreterType[Option[A]] = Reader(map => Try(opt.run(map)).toOption)

      override def list[A](fieldName: String, prg: InterpreterType[A]): InterpreterType[List[A]] = Reader(map => map(fieldName).asInstanceOf[List[Map[String, Any]]].map(prg.run))

      override def int(fieldName: String): InterpreterType[Int] = Reader(map => map(fieldName).asInstanceOf[Int])

      override def long(fieldName: String): InterpreterType[Long] = Reader(map => map(fieldName).asInstanceOf[Long])

      override def double(fieldName: String): InterpreterType[Double] = Reader(map => map(fieldName).asInstanceOf[Double])

      override def short(fieldName: String): InterpreterType[Short] = Reader(map => map(fieldName).asInstanceOf[Short])

      override def string(fieldName: String): InterpreterType[String] = Reader(map => map(fieldName).asInstanceOf[String])

      override def bigDecimal(fieldName: String): InterpreterType[BigDecimal] = Reader(map => map(fieldName).asInstanceOf[BigDecimal])

      override def date(fieldName: String): InterpreterType[Date] = Reader(map => map(fieldName).asInstanceOf[Date])

      override def flatMap[A, B](fa: InterpreterType[A])(f: (A) => InterpreterType[B]): InterpreterType[B] = fa.flatMap(f)

      override def tailRecM[A, B](a: A)(f: (A) => InterpreterType[Either[A, B]]): InterpreterType[B] = flatMap(f(a)) {
        case Left(left) => tailRecM(left)(f)
        case Right(right) => pure(right)
      }
    })

    interpreter.run(map)
  }
}