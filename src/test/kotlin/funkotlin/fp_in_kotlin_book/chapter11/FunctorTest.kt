package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FunctorTest : StringSpec({
    "should map over list with ListFunctor" {
        listFunctor.map(List.of(1, 2, 3)) { it + 1 } shouldBe
                List.of(2, 3, 4)
    }

    "should distribute over ListFunctor" {
        val unzipped = listFunctor.distribute(
            List.of(
                Pair(1, "one"),
                Pair(2, "two"),
                Pair(3, "three")
            )
        )

        unzipped shouldBe (List.of(1, 2, 3) to List.of("one", "two", "three"))
    }

    "should codistribute over ListFunctor" {
        // expect
        listFunctor.codistribute<Int, String>(
            Left<List<Int>>(List.of<Int>(1, 2, 3))
        ) shouldBe
                List.of(Left(1), Left(2), Left(3))

        // expect
        listFunctor.codistribute<Int, String>(
            Right<List<String>>(List.of<String>("one", "two"))
        ) shouldBe
                List.of(Right("one"), Right("two"))
    }
})
