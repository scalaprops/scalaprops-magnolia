package scalaprops

import ScalapropsMagnolia._

sealed abstract class X
case object X1 extends X
case object X2 extends X
case object X3 extends X

sealed trait A
case class B(value: Boolean) extends A
case class C(value: Either[X, Boolean]) extends A
case class D(value: Int) extends A

sealed trait Tree[+T] {
  def size: Int
}
case class Leaf[+L](value: L) extends Tree[L] {
  def size = 1
}
case class Branch[+T](left: Tree[T], right: Tree[T]) extends Tree[T] {
  def size = left.size + right.size
}

object ScalapropsMagnoliaTest extends Scalaprops {

  val tree = Property.forAll { seed: Long =>
    println("--" * 55)
    Gen[Tree[Int]].samples(seed = seed).take(5) foreach println

    true
  }

  val test = Property.forAll { seed: Long =>
    val expected = List(
      B(true),
      B(false),
      C(Left(X1)),
      C(Left(X2)),
      C(Left(X3)),
      C(Right(true)),
      C(Right(false))
    )

    val values = Gen[A].infiniteStream(seed = seed)

    values.filter(_.isInstanceOf[D]).take(5).toList

    expected.forall { x =>
      values.contains(x)
    }
  }
}
