package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monads.genMonad
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MonadTest : StringSpec({
    "genMonad should flatMap" {
        val rng = SimpleRNG(1)
        val gen = Gen.choose(1, 6)
        fun genTransform(a: Int): Gen<List<Int>> = Gen.listOfSpecifiedN(a, Gen.choose(1, 10))

        genMonad.flatMap(genMonad.unit(1)) { a: Int -> genMonad.unit(a + 1) }
            .fix().sample.run(rng).first shouldBe 2

        val (l1, rng2) = genMonad.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng)
        val (l2, rng3) = genMonad.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng2)
        val (l3, rng4) = genMonad.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng3)
        val (l4, rng5) = genMonad.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng4)

        l1 shouldBe listOf(5, 6, 7, 2)
        l2 shouldBe listOf(8, 7, 9, 1, 8)
        l3 shouldBe listOf(9, 5, 2, 9, 4)
        l4 shouldBe listOf(1)
    }
})
