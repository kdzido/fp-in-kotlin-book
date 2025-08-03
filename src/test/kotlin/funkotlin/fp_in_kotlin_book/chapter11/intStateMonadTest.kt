package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import funkotlin.fp_in_kotlin_book.chapter06.State
import funkotlin.fp_in_kotlin_book.chapter06.StateOf
import funkotlin.fp_in_kotlin_book.chapter06.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monads.stateMonad
import io.kotest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class intStateMonadTest : StringSpec({
    "stateMonad should flatMap" {
        val f: State<Int, Int> = State { s -> Pair( s, s + 1) }

        val m = stateMonad<Int>()

        val incr1 = m.flatMap(f) { i -> m.unit(i + 1) }.fix()
        val (a2, acc1) = incr1.run(1)
        val (a3, acc2) = incr1.run(acc1)
        a2 shouldBe 2
        a3 shouldBe 3
    }

    "stateMonad should compose" {
        val f: State<Int, Int> = State { s -> Pair( s, s + 1) }

        val m = stateMonad<Int>()

        val incr1: State<Int, Int> = m.flatMap(f) { i -> m.unit(i + 1) }.fix()
        val res: (Int) -> StateOf<Int, Int> = m.compose({ a: Int -> incr1}, { b: Int -> incr1})

        val (a3, acc2) = res(1).fix().run(1)
        a3 shouldBe 3
    }

    "explore state monads" {
        // given
        val stateA: State<Int, Int> = State { a: Int -> a to (10 + a) }
        val stateB: State<Int, Int> = State { b: Int -> b to (10 * b) }

        // and
        val m = stateMonad<Int>()
        // and
        fun replicateIntState(): StateOf<Int, ListCh3<Int>> =
            m.replicateM(5, stateA)

        fun map2IntState(): StateOf<Int, Int> =
            m.map2(stateA, stateB) { a, b -> a * b }

        fun sequenceIntState(): StateOf<Int, ListCh3<Int>> =
            m.sequence(ListCh3.of(stateA, stateB))

        // expect:
        replicateIntState().fix().run(1) shouldBe
                Pair(ListCh3.of(1, 11, 21, 31, 41), 51)
        // and:
        map2IntState().fix().run(1) shouldBe
                Pair(11, 110)
        // and:
        sequenceIntState().fix().run(1) shouldBe
                Pair(ListCh3.of(1, 11), 110)
    }

    "zipWithIndex" {
        val expectedList = ListCh3.of(
            0 to 1,
            1 to 2,
            2 to 3,
        )

        zipWithIndex(ListCh3.of(1, 2, 3)) shouldBe expectedList
    }
})
