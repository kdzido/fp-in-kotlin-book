package funkotlin.fp_in_kotlin_book.chapter10

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MonoidTest : StringSpec({
    "should combine strings" {
        stringMonoid.combine("one", "two") shouldBe "onetwo"
        stringMonoid.combine("one", stringMonoid.nil) shouldBe "one"
    }

    "should combine lists of strings" {
        listMonoid<String>().combine(listOf("one"), listOf("two")) shouldBe listOf("one", "two")
        listMonoid<String>().combine(listOf("one"), listMonoid<String>().nil) shouldBe listOf("one")
    }
})
