package funkotlin.fp_in_kotlin_book.chapter02

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Chapter02KtTest : FunSpec({

    // Exercise 2-2, implement isSorted
    test("isSorted should check if collection is sorted as per predicate") {
        val gteq: (Int, Int) -> Boolean = { a, b -> a >= b }
        val gt: (Int, Int) -> Boolean = { a, b -> a > b }

        isSorted(listOf(1), gteq) shouldBe true
        isSorted(listOf(3, 2, 1), gteq) shouldBe true
        isSorted(listOf(3, 3, 3), gteq) shouldBe true

        isSorted(listOf(1, 2), gteq) shouldBe false
        isSorted(listOf(1, 2, 3), gteq) shouldBe false

        isSorted(listOf(1), gt) shouldBe true
        isSorted(listOf(3, 3, 3), gt) shouldBe false
        isSorted(listOf(3, 3, 3), gt) shouldBe false
    }
})
