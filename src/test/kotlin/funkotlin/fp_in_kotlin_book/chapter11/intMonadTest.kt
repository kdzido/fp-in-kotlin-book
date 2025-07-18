package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter06.State
import funkotlin.fp_in_kotlin_book.chapter06.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monads.intStateMonad
import io.kotest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class intMonadTest : StringSpec({
    "intStateMonad should flatMap" {
        val f: State<Int, Int> = State { s -> Pair(s * s, s + 1) }

        val m = intStateMonad()

        val incr1 = m.flatMap(f) { i -> m.unit(i + 1) }.fix()
        val (a2, acc1) = incr1.run(1)
        val (a3, acc2) = incr1.run(acc1)
        a2 shouldBe 2
        a3 shouldBe 5
    }
})
