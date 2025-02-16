package funkotlin.fp_in_kotlin_book.chapter06

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

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
})
