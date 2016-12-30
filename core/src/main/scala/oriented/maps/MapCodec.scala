package oriented.maps

import cats.functor.Invariant

trait MapCodec[A, M] { self =>
  def encode(value: A): M
  def decode(map: M): Option[A]

  def xmap[B](f: A => B)(g: B => A): MapCodec[B, M] = new MapCodec[B, M] {
    override def encode(value: B): M = self.encode(g(value))
    override def decode(map: M): Option[B] = self.decode(map).map(f)
  }
}

object MapCodec {
  implicit def derived[A, M](implicit E: MapEncoder[A, M], D: MapDecoder[A, M]) = new MapCodec[A, M] {
    override def encode(value: A): M = E(value)
    override def decode(map: M): Option[A] = D(map)
  }

  implicit def invariant[M] = new Invariant[MapCodec[?, M]] {
    override def imap[A, B](fa: MapCodec[A, M])(f: (A) => B)(g: (B) => A): MapCodec[B, M] = fa.xmap(f)(g)
  }
}
