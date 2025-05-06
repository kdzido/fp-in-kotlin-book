package funkotlin.fp_in_kotlin_book.chapter08

import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class SGenTest : StringSpec({
    "SGen.forSize with Gen.listOfSpecifiedN" {
        val rng = SimpleRNG(1)
        val sgen = SGen({ n->
            Gen.listOfSpecifiedN(n, Gen.choose(1, 100))
        })

        val (l1, rng2) = sgen.forSize(5).sample.run(rng)
        val (l2, rng3) = sgen.forSize(5).sample.run(rng2)
        val (l3, rng4) = sgen.forSize(5).sample.run(rng3)
        val (l4, rng5) = sgen.forSize(5).sample.run(rng4)

        l1 shouldBe listOf(35, 50, 78, 34, 20)
        l2 shouldBe listOf(12, 44, 70, 45, 46)
        l3 shouldBe listOf(44, 14, 99, 59, 20)
        l4 shouldBe listOf(54, 22, 28, 46, 59)
    }

    "Gen.unsize" {
        val rng = SimpleRNG(1)
        val sgen: (Int) -> SGen<List<Int>> = { n: Int ->
            Gen.listOfSpecifiedN(n, Gen.choose(1, 100)).unsized()
        }

        val (l1, rng2) = sgen(5).forSize(1).sample.run(rng)
        val (l2, rng3) = sgen(5).forSize(2).sample.run(rng2)
        val (l3, rng4) = sgen(5).forSize(3).sample.run(rng3)
        val (l4, rng5) = sgen(5).forSize(4).sample.run(rng4)

        l1 shouldBe listOf(35, 50, 78, 34, 20)
        l2 shouldBe listOf(12, 44, 70, 45, 46)
        l3 shouldBe listOf(44, 14, 99, 59, 20)
        l4 shouldBe listOf(54, 22, 28, 46, 59)
    }

    "SGen.map" {
        val rng = SimpleRNG(1)
        val sgen = SGen({ n ->
            Gen.choose(1, n)
        })

        val (v1, rng2) = sgen.map { it + 5 }.forSize(5).sample.run(rng)
        val (v2, rng3) = sgen.map { it + 5 }.forSize(5).sample.run(rng2)
        val (v3, rng4) = sgen.map { it + 5 }(5).sample.run(rng3)
        val (v4, rng5) = sgen.map { it + 5 }(5).sample.run(rng4)

        v1 shouldBe 6
        v2 shouldBe 8
        v3 shouldBe 8
        v4 shouldBe 7
    }

    "SGen.flatMap" {
        val rng = SimpleRNG(1)
        val sgen = SGen({ n ->
            Gen.choose(1, n)
        })

        val (v1, rng2) = sgen.flatMap { Gen.unit(it + 5) }.forSize(5).sample.run(rng)
        val (v2, rng3) = sgen.flatMap { Gen.unit(it + 5) }.forSize(5).sample.run(rng2)
        val (v3, rng4) = sgen.flatMap { Gen.unit(it + 5) }(5).sample.run(rng3)
        val (v4, rng5) = sgen.flatMap { Gen.unit(it + 5) }(5).sample.run(rng4)

        v1 shouldBe 6
        v2 shouldBe 8
        v3 shouldBe 8
        v4 shouldBe 7
    }
})

