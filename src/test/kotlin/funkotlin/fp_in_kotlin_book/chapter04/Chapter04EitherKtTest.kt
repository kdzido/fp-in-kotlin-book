package funkotlin.fp_in_kotlin_book.chapter04

import funkotlin.fp_in_kotlin_book.chapter03.Cons
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Nil
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
        val er1 = map2E(left1, left2, addF).shouldBeInstanceOf<Left<ArithmeticException>>()
        er1.value.message shouldBe "error"
        // and
        val er2 = map2E(Right(1), left2, addF).shouldBeInstanceOf<Left<IllegalStateException>>()
        er2.value.message shouldBe "state"
        // and
        val er3 = map2E(left1, Right(1), addF).shouldBeInstanceOf<Left<ArithmeticException>>()
        er3.value.message shouldBe "error"
        // and
        map2E(Right(1), Right(2), addF) shouldBe Right(3)
    }
    
    // Exercise 4.8
    test("should map2 Either with both errors") {
        val ex1 = ArithmeticException("error")
        val ex2 = IllegalStateException("state")
        val left1: Either<Exception, Int> = Left(ex1)
        val left2: Either<Exception, Int> = Left(ex2)

        val addF: (Int, Int) -> Int = { a, b -> a + b }
        // expect
        val er1: Left<List<Exception>> = map2E_2(left1, left2, addF).shouldBeInstanceOf<Left<List<Exception>>>()
        val exList1 = exceptionListOf(er1)
        exList1 shouldBe List.of(ex1, ex2)
        // and
        val er2 = map2E_2(Right(1), left2, addF).shouldBeInstanceOf<Left<List<Exception>>>()
        val exList2 = exceptionListOf(er2)
        exList2 shouldBe List.of(ex2)
        // and
        val er3 = map2E_2(left1, Right(1), addF).shouldBeInstanceOf<Left<List<Exception>>>()
        val exList3 = exceptionListOf(er3)
        exList3 shouldBe List.of(ex1)
        // and
        map2E_2(Right(1), Right(2), addF) shouldBe Right(3)
    }

    // Exercise 4.7
    test("should sequence list") {
        val left1: Either<Exception, Int> = Left(ArithmeticException("error"))
        val left2: Either<Exception, Int> = Left(IllegalStateException("state"))

        sequenceE(List.of(left1, Right(1))).shouldBeInstanceOf<Left<IllegalArgumentException>>()
        sequenceE(List.of(Right(1), left2)).shouldBeInstanceOf<Left<IllegalStateException>>()
        sequenceE(List.of(left1, left2)).shouldBeInstanceOf<Left<IllegalArgumentException>>()

        sequenceE<RuntimeException, Int>(List.of()) shouldBe Right(List.of())
        sequenceE(List.of(Right(1), Right(2))) shouldBe Right(List.of(1, 2))
    }

    // Exercise 4.7
    test("should traverse list") {
        val toIntO: (String) -> Either<Exception, Int> = { a -> catchesE { a.toInt() } }
        traverseE(List.of(), toIntO) shouldBe Right(List.of())
        traverseE(List.of("1", "2"), toIntO) shouldBe Right(List.of(1, 2))
        traverseE(List.of("One", "2"), toIntO).shouldBeInstanceOf<Left<*>>()
        traverseE(List.of("1", "Two"), toIntO).shouldBeInstanceOf<Left<*>>()
    }
})

private fun exceptionListOf(er1: Left<List<Exception>>) =
    when (val h1 = er1.value) {
        is Cons -> {
            when (val h2 = h1.tail) {
                is Cons -> List.of(h1.head, h2.head)
                Nil -> List.of(h1.head)
            }
        }

        Nil -> List.of()
    }
