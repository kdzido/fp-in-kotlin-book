package funkotlin.fp_in_kotlin_book.chapter03

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Chapter03KtTest : FunSpec({
    test("should get sum of list") {
        List.sum(Nil) shouldBe 0
        List.sum(List.of(6)) shouldBe 6
        List.sum(List.of(1, 2, 3)) shouldBe 6
    }

    test("should get product of list") {
        List.product(Nil) shouldBe 1.0
        List.product(List.of(6.0)) shouldBe 6.0
        List.product(List.of(2.0, 3.0)) shouldBe 6.0
    }

    // Exercise 3.1
    test("should return tail") {
        List.tail(Nil) shouldBe Nil
        List.tail(List.of(1)) shouldBe Nil
        List.tail(List.of(1, 2, 3)) shouldBe List.of(2, 3)
    }
})
