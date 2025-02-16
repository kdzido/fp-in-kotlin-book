package funkotlin.fp_in_kotlin_book.chapter06

import funkotlin.fp_in_kotlin_book.chapter03.List as ListL

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Chap06Test : FunSpec({
    test("should pass RNG") {
        val rng = SimpleRNG(42)

        val (n1, rng2) = rng.nextInt()
        n1 shouldBe 16159453

        val (n2, rng3) = rng2.nextInt()
        n2 shouldBe -1281479697

        val (n3, rng4) = rng3.nextInt()
        n3 shouldBe -340305902
    }

    test("random pair") {
        val rng = SimpleRNG(42)

        val (nums, rng2) = randomPair2(rng)
        nums shouldBe Pair(16159453, -1281479697)

        val (nums2, rng3) = randomPair2(rng2)
        nums2 shouldBe Pair(-340305902, -2015756020)
    }

    // EXER 6.4
    test("random ints") {
        val rng = SimpleRNG(42)

        val (ints1, rng2) = RNG.ints(3, rng)
        ints1 shouldBe ListL.of(16159453, -1281479697, -340305902)

        val (ints2, rng3) = RNG.ints(3, rng2)
        ints2 shouldBe ListL.of(-2015756020, 1770001318, -1934589059)
    }

    // EXER 6.5
    test("Rand map") {
        val rng = SimpleRNG(42)
        val f: Rand<ListL<Int>> = { r -> RNG.ints(3, r) }

        val res: (RNG) -> Pair<ListL<Int>, RNG> = RNG.map(f) { ls -> ListL.map(ls) { it + 1 } }
        res(rng).first shouldBe ListL.of(16159454, -1281479696, -340305901)
    }

    // EXER 6.6
    test("Rand map2") {
        val rng = SimpleRNG(42)
        val f1: Rand<Int> = { r -> RNG.nonNegativeInt(r) }
        val f2: Rand<Double> = { r -> RNG.double(r) }

        val res: (RNG) -> Pair<Double, RNG> = RNG.map2(f1, f2) { ass, bss -> ass.toDouble() + bss }
        res(rng).first shouldBe 1.6159453007524831E7
    }

})
