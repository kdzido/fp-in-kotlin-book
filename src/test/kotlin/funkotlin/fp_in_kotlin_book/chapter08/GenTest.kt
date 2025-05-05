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

    "Gen.map" {
        val rng = SimpleRNG(1)
        val (v1, rng2) = Gen.choose(1, 6)
            .map { a -> a + 10 }.sample.run(rng)
        val (v2, rng3) = Gen.choose(1, 6)
            .map { a -> a + 10 }.sample.run(rng2)
        val (v3, rng4) = Gen.choose(1, 6)
            .map { a -> a + 10 }.sample.run(rng3)
        val (v4, rng5) = Gen.choose(1, 6)
            .map { a -> a + 10 }.sample.run(rng4)

        v1 shouldBe 14
        v2 shouldBe 15
        v3 shouldBe 13
        v4 shouldBe 12
    }

    "Gen odd, even integers" {
        val rng = SimpleRNG(1)
        val evenIntGen = Gen.choose(0, 100).flatMap { Gen.unit(if (it % 2 == 0) it else it - 1) }
        val oddIntGen = Gen.choose(0, 100).flatMap { Gen.unit(if (it % 2 == 1) it else it + 1) }

        val (e1, erng2) = evenIntGen.sample.run(rng)
        val (e2, erng3) = evenIntGen.sample.run(erng2)
        val (e3, erng4) = evenIntGen.sample.run(erng3)
        val (e4, erng5) = evenIntGen.sample.run(erng4)
        val (e5, erng6) = evenIntGen.sample.run(erng5)
        e1 shouldBe 48
        e2 shouldBe 38
        e3 shouldBe 46
        e4 shouldBe 40
        e5 shouldBe 42

        val (o1, orng2) = oddIntGen.sample.run(rng)
        val (o2, orng3) = oddIntGen.sample.run(orng2)
        val (o3, orng4) = oddIntGen.sample.run(orng3)
        val (o4, orng5) = oddIntGen.sample.run(orng4)
        val (o5, orng6) = oddIntGen.sample.run(orng5)
        o1 shouldBe 49
        o2 shouldBe 39
        o3 shouldBe 47
        o4 shouldBe 41
        o5 shouldBe 43
    }

    "Gen.union" {
        val rng = SimpleRNG(1)
        val evenIntGen = Gen.choose(0, 100).flatMap { Gen.unit(if (it % 2 == 0) it else it - 1) }
        val oddIntGen = Gen.choose(0, 100).flatMap { Gen.unit(if (it % 2 == 1) it else it + 1) }

        val (u1, rng2) = Gen.union(oddIntGen, evenIntGen).sample.run(rng)
        val (u2, rng3) = Gen.union(oddIntGen, evenIntGen).sample.run(rng2)
        val (u3, rng4) = Gen.union(oddIntGen, evenIntGen).sample.run(rng3)
        val (u4, rng5) = Gen.union(oddIntGen, evenIntGen).sample.run(rng4)
        val (u5, rng6) = Gen.union(oddIntGen, evenIntGen).sample.run(rng5)

        u1 shouldBe 39
        u2 shouldBe 40
        u3 shouldBe 89
        u4 shouldBe 49
        u5 shouldBe 90
    }

    "Gen.weighted" {
        val rng = SimpleRNG(1)
        val evenIntGen = Gen.choose(0, 100).flatMap { Gen.unit(if (it % 2 == 0) it else it - 1) }
        val oddIntGen = Gen.choose(0, 100).flatMap { Gen.unit(if (it % 2 == 1) it else it + 1) }
        val weightedGen = Gen.weighted(Pair(oddIntGen, 0.5), Pair(evenIntGen, 0.5))

        val (w1, rng2) = weightedGen.sample.run(rng)
        val (w2, rng3) = weightedGen.sample.run(rng2)
        val (w3, rng4) = weightedGen.sample.run(rng3)
        val (w4, rng5) = weightedGen.sample.run(rng4)
        val (w5, rng6) = weightedGen.sample.run(rng5)
        val (w6, rng7) = weightedGen.sample.run(rng6)

        w1 shouldBe 39
        w2 shouldBe 41
        w3 shouldBe 89
        w4 shouldBe 48
        w5 shouldBe 91
        w6 shouldBe 45
    }

    "SGen.listOf" {
        val rng = SimpleRNG(1)
        val sgen = Gen.choose(1, 100).listOf()

        val (l1, rng2) = sgen.forSize(5).sample.run(rng)
        val (l2, rng3) = sgen.forSize(5).sample.run(rng2)
        val (l3, rng4) = sgen.forSize(5).sample.run(rng3)
        val (l4, rng5) = sgen.forSize(5).sample.run(rng4)

        l1 shouldBe listOf(35, 51, 79, 34, 21)
        l2 shouldBe listOf(12, 44, 71, 45, 47)
        l3 shouldBe listOf(45, 15, 99, 59, 21)
        l4 shouldBe listOf(54, 23, 28, 46, 60)
    }
})

