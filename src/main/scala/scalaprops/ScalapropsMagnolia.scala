package scalaprops

import magnolia.{CaseClass, Magnolia, SealedTrait}

object ScalapropsMagnolia {
  type Typeclass[T] = scalaprops.Gen[T]

  def combine[T](ctx: CaseClass[Gen, T]): Gen[T] =
    Gen.gen[T] { (size, rand) =>
      var r = rand
      val result = ctx.construct { p =>
        val (nextRand, x) = p.typeclass.f(size, r)
        r = nextRand
        x
      }

      r -> result
    }

  def dispatch[T](ctx: SealedTrait[Gen, T]): Gen[T] = {
    val gs: Seq[Gen[T]] = ctx.subtypes.map { x =>
      x.typeclass.asInstanceOf[Gen[T]]
    }
    Gen.choose(0, gs.size - 1).flatMap(gs)
  }

  implicit def gen[T]: Gen[T] = macro Magnolia.gen[T]
}
