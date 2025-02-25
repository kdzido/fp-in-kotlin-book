package funkotlin.fp_in_kotlin_book.chapter06

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class RNGPropTest : StringSpec({

    // EXER 6.1
    "non-negative random int" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (n1, rng2) = RNG.nonNegativeInt()(rng)
            (n1 >= 0) shouldBe true
            (n1 <= Int.MAX_VALUE) shouldBe true
        }
    }

    // EXER 6.2
    "<0, 1) random double" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (n1, rng2) = RNG.double(rng)
            (n1 >= 0.0) shouldBe true
            (n1 < 1.0) shouldBe true
        }
    }
    
    // EXER 6.5
    "<0, 1) random double2" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (n1, rng2) = RNG.double2()(rng)

            (n1 >= 0.0) shouldBe true
            (n1 < 1.0) shouldBe true
        }
    }

    // EXER 6.3
    "random intDouble" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (p, rng2) = RNG.intDouble(rng)
            val (n1, d2) = p
            // and
            (n1 >= 0) shouldBe true
            (n1 <= Int.MAX_VALUE) shouldBe true
            // and
            (d2 >= 0.0) shouldBe true
            (d2 < 1.0) shouldBe true
        }
    }
    "random doubleInt" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (p, rng2) = RNG.doubleInt(rng)
            val (d1, n2) = p
            // and
            (d1 >= 0.0) shouldBe true
            (d1 < 1.0) shouldBe true
            // and
            (n2 >= 0) shouldBe true
            (n2 <= Int.MAX_VALUE) shouldBe true
        }
    }
    "random double3" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (t, rng2) = RNG.double3(rng)
            val (d1, d2, d3) = t
            // and
            (d1 >= 0.0) shouldBe true
            (d1 < 1.0) shouldBe true
            // and
            (d2 >= 0.0) shouldBe true
            (d2 < 1.0) shouldBe true
            // and
            (d3 >= 0.0) shouldBe true
            (d3 < 1.0) shouldBe true
        }
    }
})
