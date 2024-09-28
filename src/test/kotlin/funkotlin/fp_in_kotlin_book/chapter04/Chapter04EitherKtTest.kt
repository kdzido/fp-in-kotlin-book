package funkotlin.fp_in_kotlin_book.chapter04

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

import funkotlin.fp_in_kotlin_book.chapter03.List
import io.kotest.matchers.types.shouldBeInstanceOf

class Chapter04EitherKtTest : FunSpec({
    test("should calculate meanE") {
        meanE(List.of()) shouldBe Left("Mean of empty list!")
        meanE(List.of(1.0)) shouldBe Right(1.0)
        meanE(List.of(1.0, 2.0)) shouldBe Right(1.5)
    }

    test("should divide by zero") {
        safeDiv(6, 2) shouldBe Right(3)
        safeDiv(6, 0).shouldBeInstanceOf<Left<ArithmeticException>>()
        // and
        safeDivC(6, 2) shouldBe Right(3)
        safeDivC(6, 0).shouldBeInstanceOf<Left<ArithmeticException>>()
    }

    // Exercise 4.6
    test("should map Either") {
        val left: Either<Exception, String> = Left(ArithmeticException("error"))
        val right: Either<Exception, String> = Right("value")

        left.map { a -> "one" }.shouldBeInstanceOf<Left<ArithmeticException>>()
        right.map { a -> a.length } shouldBe Right(5)
    }

    test("should flatMap Either") {
        val left: Either<Exception, String> = Left(ArithmeticException("error"))
        val right: Either<Exception, String> = Right("value")

        left.flatMap { a -> Left(IllegalStateException("wrong!")) }.shouldBeInstanceOf<Left<ArithmeticException>>()
        left.flatMap { a -> Right("valid") }.shouldBeInstanceOf<Left<ArithmeticException>>()
        right.flatMap { a -> Right("valid") } shouldBe Right("valid")
    }

    test("should orElse Either") {
        val left: Either<Exception, String> = Left(ArithmeticException("error"))
        val right: Either<Exception, String> = Right("value")

        left.orElse { -> Right("else") } shouldBe Right("else")
        right.orElse { -> Right("else") } shouldBe Right("value")
    }

    test("should orElse Option") {
        None.orElse { Some(123) } shouldBe Some(123)
        Some(1).orElse { Some(123) } shouldBe Some(1)
    }

    test("should map2 Either") {
        val left1: Either<Exception, Int> = Left(ArithmeticException("error"))
        val left2: Either<Exception, Int> = Left(IllegalStateException("state"))

        val addF: (Int, Int) -> Int = { a, b -> a + b }
        map2E(left1, left2, addF).shouldBeInstanceOf<Left<ArithmeticException>>()
        map2E(Right(1), left2, addF).shouldBeInstanceOf<Left<IllegalStateException>>()
        map2E(left1, Right(1), addF).shouldBeInstanceOf<Left<ArithmeticException>>()
        map2E(Right(1), Right(2), addF) shouldBe Right(3)
    }
})
