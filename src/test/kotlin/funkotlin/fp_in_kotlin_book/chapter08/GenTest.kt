package funkotlin.fp_in_kotlin_book.chapter08

import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class GenTest : StringSpec({
    "Gen.choose random int in <1, 10)" {
        val intGen: Gen<Int> = Gen.choose(1, 10)

        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)

            val (n1, rng2) = intGen.sample.run(rng)
            n1 shouldBeInRange (1..9)
        }
    }

    "Gen.choosePair random int in <1, 10)" {
        val intGen: Gen<Pair<Int, Int>> = Gen.choosePair(1, 10)

        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)

            val (p1, rng2) = intGen.sample.run(rng)
            p1.first shouldBeInRange (1..9)
            p1.second shouldBeInRange (1..9)
        }
    }

    "Gen.unit" {
        checkAll<Long>(10_000) { seed ->
            val rng = SimpleRNG(seed)

            val (a, rng2) = Gen.unit(seed).sample.run(rng)
            a shouldBe seed
        }
    }

    "Gen.boolean" {
        val rng = SimpleRNG(1)
        val (b1, rng2) = Gen.boolean().sample.run(rng)
        val (b2, rng3) = Gen.boolean().sample.run(rng2)
        val (b3, rng4) = Gen.boolean().sample.run(rng3)

        b1 shouldBe true
        b2 shouldBe false
        b3 shouldBe false
    }

    "Gen.listOfN" {
        val rng = SimpleRNG(1)
        val (l1, rng2) = Gen.listOfSpecifiedN(5, Gen.choose(1, 10)).sample.run(rng)
        val (l2, rng3) = Gen.listOfSpecifiedN(5, Gen.choose(1, 10)).sample.run(rng2)
        val (l3, rng4) = Gen.listOfSpecifiedN(5, Gen.choose(1, 10)).sample.run(rng3)
        val (l4, rng5) = Gen.listOfSpecifiedN(5, Gen.choose(1, 10)).sample.run(rng4)

        l1 shouldBe listOf(8, 6, 7, 7, 3)
        l2 shouldBe listOf(3, 8, 8, 9, 2)
        l3 shouldBe listOf(9, 6, 9, 5, 3)
        l4 shouldBe listOf(9, 5, 1, 1, 6)
    }

    "Gen.listOfNDyn" {
        val rng = SimpleRNG(1)
        val (l1, rng2) = Gen.listOfN(Gen.choose(1, 5), Gen.choose(1, 10)).sample.run(rng)
        val (l2, rng3) = Gen.listOfN(Gen.choose(1, 5), Gen.choose(1, 10)).sample.run(rng2)
        val (l3, rng4) = Gen.listOfN(Gen.choose(1, 5), Gen.choose(1, 10)).sample.run(rng3)
        val (l4, rng5) = Gen.listOfN(Gen.choose(1, 5), Gen.choose(1, 10)).sample.run(rng4)

        l1 shouldBe listOf(6)
        l2 shouldBe listOf(7, 3, 3, 8)
        l3 shouldBe listOf(9)
        l4 shouldBe listOf(9, 6, 9)
    }

    "Gen.flatMap" {
        val rng = SimpleRNG(1)
        val (l1, rng2) = Gen.choose(1, 6)
            .flatMap { a -> Gen.listOfSpecifiedN(a, Gen.choose(1, 10)) }.sample.run(rng)
        val (l2, rng3) = Gen.choose(1, 6)
            .flatMap { a -> Gen.listOfSpecifiedN(a, Gen.choose(1, 10)) }.sample.run(rng2)
        val (l3, rng4) = Gen.choose(1, 6)
            .flatMap { a -> Gen.listOfSpecifiedN(a, Gen.choose(1, 10)) }.sample.run(rng3)
        val (l4, rng5) = Gen.choose(1, 6)
            .flatMap { a -> Gen.listOfSpecifiedN(a, Gen.choose(1, 10)) }.sample.run(rng4)

        l1 shouldBe listOf(6, 7, 7, 3)
        l2 shouldBe listOf(8, 8, 9, 2, 9)
        l3 shouldBe listOf(9)
        l4 shouldBe listOf(3, 9, 5)
    }
})

