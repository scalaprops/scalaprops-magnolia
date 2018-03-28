package scalaprops

import magnolia.{CaseClass, Magnolia, SealedTrait}

object ScalapropsMagnoliaCogen {
  type Typeclass[T] = scalaprops.Cogen[T]

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] =
    new Cogen[T] {
      def cogen[B](t: T, g: CogenState[B]) = {
        ctx.parameters.zipWithIndex.foldLeft(g) {
          case (state, (p, i)) =>
            Cogen[Int].cogen(i, Cogen[String].cogen(p.label, p.typeclass.cogen(p.dereference(t), state)))
        }
      }
    }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] =
    new Cogen[T] {
      def cogen[B](t: T, g: CogenState[B]) = {
        val index = ctx.subtypes.indexWhere(_.cast.isDefinedAt(t))
        if (index >= 0) {
          def loop(i: Int, r: Rand): Rand = {
            if (i <= 0) r
            else loop(i - 1, g.gen.f(index, r)._1)
          }
          val newR = loop(index, g.rand)
          val s = ctx.subtypes(index)
          Cogen[String].cogen(s.typeName.full, s.typeclass.cogen(s.cast(t), g.copy(rand = newR)))
        } else {
          sys.error(s"bug? $ctx $t $g $index")
        }
      }
    }

  implicit def derivingScalapropsCogen[T]: Typeclass[T] =
    macro Magnolia.gen[T]
}
