package funkotlin.fp_in_kotlin_book.chapter06

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class RNGPropTest : StringSpec({

    // EXER 6.1
    "non-negative random int" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (n1, rng2) = rng.nonNegativeInt(rng)
            (n1 >= 0) shouldBe true
            (n1 <= Int.MAX_VALUE) shouldBe true
        }
    }

    // EXER 6.2
    "<0, 1) random double" {
        checkAll<Long>(100_000) { seed ->
            val rng = SimpleRNG(seed)
            val (n1, rng2) = rng.double(rng)
            (n1 >= 0.0) shouldBe true
            (n1 < 1.0) shouldBe true
        }
    }
})
