package funkotlin.fp_in_kotlin_book.chapter04

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

import funkotlin.fp_in_kotlin_book.chapter03.List

class Chapter04KtTest : FunSpec({
    test("should throw for mean of empty list") {
        val e = shouldThrow<ArithmeticException> {
            mean(List.of())
        }
        e.message shouldBe "mean of empty list"
    }

    test("should get default value for mean on emtpy list") {
        mean(List.of(), 0.0) shouldBe 0.0
    }

    test("should calc mean of list") {
        mean(List.of(1.0 , 2.0)) shouldBe 1.5
    }
    // listing 4.2
    test("should calc meanO of list") {
        meanO(List.of()).shouldBeInstanceOf<None>()
        meanO(List.of(1.0 , 2.0)) shouldBe Some(1.5)
    }

    test("should construct Option") {
        Some(1).value shouldBe 1
        None.shouldBeInstanceOf<Option<Nothing>>()
    }

    // Exercise 4.1
    test("should map Option") {
        None.map { a -> 123 } shouldBe None
        Some(1).map { a -> a + 1 } shouldBe Some(2)
    }

    // Exercise 4.1
    test("should flatMap Option") {
        None.flatMap { a -> Some(123) } shouldBe None
        Some(1).flatMap { a -> Some(a + 1) } shouldBe Some(2)
        Some(1).flatMap { a -> None } shouldBe None
    }

    // Exercise 4.1
    test("should getOrElse Option") {
        None.getOrElse { 123 } shouldBe 123
        Some(1).getOrElse { 123 } shouldBe 1
    }

    // Exercise 4.1
    test("should orElse Option") {
        None.orElse { Some(123) } shouldBe Some(123)
        Some(1).orElse { Some(123) } shouldBe Some(1)
    }

    // Exercise 4.1
    test("should filter Option") {
        None.filter { true } shouldBe None
        None.filter { false } shouldBe None
        Some(1).filter { true } shouldBe Some(1)
        Some(1).filter { false } shouldBe None
    }

    // Exercise 4.2
    test("should get variance of list") {
        variance(List.of()) shouldBe None
        variance(List.of(1.0)) shouldBe Some(0.0)
        variance(List.of(1.0, 2.0)) shouldBe Some(0.25)
        variance(List.of(1.0, 2.0, 3.0, 4.0)) shouldBe Some(1.25)
    }
})
