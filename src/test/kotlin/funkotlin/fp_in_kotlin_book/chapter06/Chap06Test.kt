package funkotlin.fp_in_kotlin_book.chapter06

import funkotlin.fp_in_kotlin_book.chapter06.State.Companion.sequence
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

    // EXER 6.8
    test("Rand flatMap") {
        val rng = SimpleRNG(42)
        val f: Rand<Int> = State { r -> RNG.nonNegativeInt().run(r) }

        val incr1 = f.flatMap { i -> State.unit(i + 1) }
        val (n2, rng2) = incr1.run(rng)
        val (n3, rng3) = incr1.run(rng2)
        n2 shouldBe 16159454
        n3 shouldBe 1281479698
    }

    // EXER 6.5, 6.9
    test("Rand map") {
        val rng = SimpleRNG(42)
        val f: Rand<ListL<Int>> = State { r -> RNG.ints(3, r) }

        val incr2: (RNG) -> Pair<ListL<Int>, RNG> = f.map { ls -> ListL.map(ls) { it + 1 } }.run
        incr2(rng).first shouldBe ListL.of(16159454, -1281479696, -340305901)
    }

    // EXER 6.6, 6.9
    test("Rand map2") {
        val rng = SimpleRNG(42)
        val f1: Rand<Int> = State { r -> RNG.nonNegativeInt().run(r) }
        val f2: Rand<Double> = State { r -> RNG.double(r) }

        val res: (RNG) -> Pair<Double, RNG> = State.map2(f1, f2) { ass, bss -> ass.toDouble() + bss }.run
        res(rng).first shouldBe 1.6159453596735485E7
    }

    // EXER 6.7
    test("sequence Rands") {
        val rng1 = SimpleRNG(42)

        sequence<Int>(ListL.of()).run(rng1).first shouldBe ListL.of()
        sequence(ListL.of(RNG.nonNegativeInt())).run(rng1).first shouldBe ListL.of(16159453)

        val seq3 = sequence(ListL.of(RNG.nonNegativeInt(), RNG.nonNegativeInt(), RNG.nonNegativeInt())).run(rng1)
        seq3.first shouldBe ListL.of(16159453, 1281479697, 340305902)
        RNG.nonNegativeInt().run(seq3.second).first shouldBe 2015756020 // valid next random int
    }
})
