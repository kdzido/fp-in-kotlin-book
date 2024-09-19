package funkotlin.fp_in_kotlin_book.chapter04

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Chapter04KtTest : FunSpec({
    test("should throw for mean of empty list") {
        val e = shouldThrow<ArithmeticException> {
            mean(listOf())
        }
        e.message shouldBe "mean of empty list"
    }

    test("should get default value for mean on emtpy list") {
        mean(listOf(), 0.0) shouldBe 0.0
    }

    test("should calc mean of list") {
        mean(listOf(1.0 , 2.0)) shouldBe 1.5
    }
})
