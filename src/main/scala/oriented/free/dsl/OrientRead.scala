package oriented.free.dsl

import java.util.Date

import cats.{Cartesian, Monad, SemigroupK}

trait OrientReadF[F[_]] extends Monad[F] with Cartesian[F] with SemigroupK[F] {
  def pure[A](value: A): F[A]

  def embedded[A](fieldName: String, read: F[A]): F[A]

  def option[A](opt: F[A]): F[Option[A]]

  def list[A](fieldName: String, prg: F[A]): F[List[A]]

  def int(fieldName: String): F[Int]

  def long(fieldName: String): F[Long]

  def double(fieldName: String): F[Double]

  def short(fieldName: String): F[Short]

  def string(fieldName: String): F[String]

  def bigDecimal(fieldName: String): F[BigDecimal]

  def date(fieldName: String): F[Date]
}

sealed trait OrientRead[A] {
  def apply[F[_] : OrientReadF]: F[A]
}

object OrientRead {

  def pure[A](value: A) = new OrientRead[A] {
    override def apply[F[_] : OrientReadF]: F[A] = implicitly[OrientReadF[F]].pure(value)
  }

  def product[A, B](left: OrientRead[A], right: OrientRead[B]) = new OrientRead[(A, B)] {
    override def apply[F[_] : OrientReadF]: F[(A, B)] = implicitly[OrientReadF[F]].product(left.apply[F], right.apply[F])
  }

  def option[A](value: OrientRead[A]) = new OrientRead[Option[A]] {
    override def apply[F[_] : OrientReadF]: F[Option[A]] = implicitly[OrientReadF[F]].option(value.apply[F])
  }

  def embedded[A](fieldName: String, read: OrientRead[A]) = new OrientRead[A] {
    override def apply[F[_] : OrientReadF]: F[A] = implicitly[OrientReadF[F]].embedded(fieldName, read.apply[F])
  }

  def list[A](fieldName: String, prg: OrientRead[A]) = new OrientRead[List[A]] {
    override def apply[F[_] : OrientReadF]: F[List[A]] = implicitly[OrientReadF[F]].list(fieldName, prg.apply[F])
  }

  def short(fieldName: String) = new OrientRead[Short] {
    override def apply[F[_] : OrientReadF]: F[Short] = implicitly[OrientReadF[F]].short(fieldName)
  }

  def int(fieldName: String) = new OrientRead[Int] {
    override def apply[F[_] : OrientReadF]: F[Int] = implicitly[OrientReadF[F]].int(fieldName)
  }

  def long(fieldName: String) = new OrientRead[Long] {
    override def apply[F[_] : OrientReadF]: F[Long] = implicitly[OrientReadF[F]].long(fieldName)
  }

  def double(fieldName: String) = new OrientRead[Double] {
    override def apply[F[_] : OrientReadF]: F[Double] = implicitly[OrientReadF[F]].double(fieldName)
  }

  def string(fieldName: String) = new OrientRead[String] {
    override def apply[F[_] : OrientReadF]: F[String] = implicitly[OrientReadF[F]].string(fieldName)
  }

  def bigDecimal(fieldName: String) = new OrientRead[BigDecimal] {
    override def apply[F[_] : OrientReadF]: F[BigDecimal] = implicitly[OrientReadF[F]].bigDecimal(fieldName)
  }

  def date(fieldName: String) = new OrientRead[Date] {
    override def apply[F[_] : OrientReadF]: F[Date] = implicitly[OrientReadF[F]].date(fieldName)
  }


  def flatMap[A, B](prg: OrientRead[A], f: A => OrientRead[B]) = new OrientRead[B] {
    override def apply[F[_] : OrientReadF]: F[B] = implicitly[OrientReadF[F]].flatMap(prg.apply[F])(x => f(x).apply[F])
  }

  implicit val monad = new Monad[OrientRead] {
    override def flatMap[A, B](fa: OrientRead[A])(f: (A) => OrientRead[B]): OrientRead[B] = OrientRead.flatMap(fa, f)

    override def tailRecM[A, B](a: A)(f: (A) => OrientRead[Either[A, B]]): OrientRead[B] = flatMap(f(a)) {
      case Left(left) => tailRecM(left)(f)
      case Right(right) => pure(right)
    }

    override def pure[A](x: A): OrientRead[A] = OrientRead.pure(x)
  }
}