package scalaprops

sealed abstract class X
case object X1 extends X
case object X2 extends X
case object X3 extends X

sealed trait A extends Product with Serializable
sealed trait Y extends A
case class B(value1: X, value2: Boolean) extends Y
case class C(value: Either[X, Boolean]) extends Y
case class D(x: Boolean) extends Y
case class E(value: Int) extends A

object ScalapropsMagnoliaTest extends Scalaprops {
  private[this] val testValues = List(
    B(X1, true),
    B(X2, true),
    B(X3, true),
    B(X1, false),
    B(X2, false),
    B(X3, false),
    C(Left(X1)),
    C(Left(X2)),
    C(Left(X3)),
    C(Right(true)),
    C(Right(false)),
    D(true),
    D(false)
  )

  val testCogen = Property.forAll { seed: Long =>
    import ScalapropsMagnoliaCogen._
    val xs = Gen[Y => Int].samples(seed = seed, listSize = 10)
    xs.exists { f =>
      // TODO
      // testValues.map(f).distinct.size == testValues.size
      testValues.map(f).distinct.size >= 10
    }
  }

  val testGen = Property.forAll { seed: Long =>
    import ScalapropsMagnoliaGen._
    val values = Gen[A].infiniteStream(seed = seed)

    assert(values.filter(_.isInstanceOf[E]).distinct.take(100).size == 100)

    testValues.forall { x =>
      values.contains(x)
    }
  }
}
