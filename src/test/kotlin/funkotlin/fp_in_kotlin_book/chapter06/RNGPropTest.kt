package funkotlin.fp_in_kotlin_book.chapter06

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class RNGPropTest : StringSpec({

    // EXER 6.1
    "non-negative random int" {
        checkAll<Long>(100_000) { ssed ->
            val rng = SimpleRNG(ssed)
            val (n1, rng2) = rng.nonNegativeInt(rng)
            (n1 >= 0) shouldBe true
            (n1 <= Int.MAX_VALUE) shouldBe true
        }
    }
})
