package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ReaderTest : StringSpec({
    "ask" {
        val a = 1
        val v = 123

        Reader.unit<Int, Int>(a).ask<Int>().run2(v) shouldBe Reader.unit<Int, Int>(v).run2(v)
    }

    "readerMonad should flatMap" {
        val f: Reader<Int, Int> = Reader { r -> r + 1 }
        val m = readerMonad<Int>()

        val incr1 = m.flatMap(f) { i -> m.unit(i + 1) }.fix()
        val a2 = incr1.run2(1)
        val a3 = incr1.run2(a2)
        a2 shouldBe 3
        a3 shouldBe 5
    }

    "readerMonad should map" {
        val f: Reader<Int, Int> = Reader { r -> r + 1 }
        val m = readerMonad<Int>()

        val incr1 = m.map(f) { i -> i + 1 }.fix()
        val a2 = incr1.run2(1)
        val a3 = incr1.run2(a2)
        a2 shouldBe 3
        a3 shouldBe 5
    }

    "explore state monads" {
        // given
        val readerA: Reader<Int, Int> = Reader { a: Int -> 10 + a }
        val readerB: Reader<Int, Int> = Reader { b: Int -> 10 * b }

        // and
        val m = readerMonad<Int>()
        // and
        fun replicateIntState(): ReaderOf<Int, ListCh3<Int>> =
            m.replicateM(5, readerA)

        fun map2IntState(): ReaderOf<Int, Int> =
            m.map2(readerA, readerB) { a, b -> a * b }

        fun sequenceIntState(): ReaderOf<Int, ListCh3<Int>> =
            m.sequence(ListCh3.of(readerA, readerB, readerA, readerB))

        // expect:
        replicateIntState().fix().run2(1) shouldBe ListCh3.of(11, 11, 11, 11, 11)
        // and:
        map2IntState().fix().run2(1) shouldBe 110
        // and:
        sequenceIntState().fix().run2(1) shouldBe ListCh3.of(11, 10, 11, 10)
    }
})
