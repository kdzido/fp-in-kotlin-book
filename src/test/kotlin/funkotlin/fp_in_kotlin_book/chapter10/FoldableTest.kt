package funkotlin.fp_in_kotlin_book.chapter10

import io.kotest.core.spec.style.StringSpec
import funkotlin.fp_in_kotlin_book.chapter03.List
import io.kotest.matchers.shouldBe

class FoldableTest: StringSpec({
    "should foldRight List" {
        val l = List.of(1, 2, 3)

        ListFoldable.foldRight(l, "", { a, b -> a.toString() + b }) shouldBe "123"
    }

    "should foldLeft List" {
        val l = List.of(1, 2, 3)

        ListFoldable.foldLeft(l, "", { a, b -> a + b.toString() }) shouldBe "123"
    }

    "should foldMap List" {
        val l = List.of(1, 2, 3)

        ListFoldable.foldMap(l, stringMonoid, { it.toString() }) shouldBe "123"
    }
})
