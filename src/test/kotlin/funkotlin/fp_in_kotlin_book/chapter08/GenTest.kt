package funkotlin.fp_in_kotlin_book.chapter08

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeInRange
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

})

