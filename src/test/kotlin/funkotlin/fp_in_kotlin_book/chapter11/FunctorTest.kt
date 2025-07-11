package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter03.List

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FunctorTest : StringSpec({
    "should map over list with ListFunctor" {
        listFunctor.map(List.of(1, 2, 3)) { it + 1 } shouldBe
                List.of(2, 3, 4)
    }

    "should unzip over ListFunctor" {
        val unzipped = listFunctor.distribute(
            List.of(
                Pair(1, "one"),
                Pair(2, "two"),
                Pair(3, "three")
            )
        )

        unzipped shouldBe (List.of(1, 2, 3) to List.of("one", "two", "three"))
    }
})
