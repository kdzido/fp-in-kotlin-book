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
    test("should return tail of list") {
        List.tail(Nil) shouldBe Nil
        List.tail(List.of(1)) shouldBe Nil
        List.tail(List.of(1, 2, 3)) shouldBe List.of(2, 3)
    }

    // Exercise 3.2
    test("should replace head of list") {
        List.setHead(Nil, 1) shouldBe List.of(1)
        List.setHead(List.of(1), 0) shouldBe List.of(0)
        List.setHead(List.of(1, 2, 3), 0) shouldBe List.of(0, 2, 3)
    }

    // Exercise 3.3
    test("should drop n elements of list") {
        List.drop(Nil, 0) shouldBe Nil
        List.drop(Nil, 1) shouldBe Nil
        List.drop(List.of(1), 0) shouldBe List.of(1)
        List.drop(List.of(1), 1) shouldBe Nil
        List.drop(List.of(1, 2, 3), 1) shouldBe List.of(2, 3)
        List.drop(List.of(1, 2, 3), 2) shouldBe List.of(3)
        List.drop(List.of(1, 2, 3), 3) shouldBe Nil
    }
})
