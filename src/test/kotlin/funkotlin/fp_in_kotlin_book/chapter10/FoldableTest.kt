package funkotlin.fp_in_kotlin_book.chapter10

import funkotlin.fp_in_kotlin_book.chapter03.Branch
import funkotlin.fp_in_kotlin_book.chapter03.Leaf
import io.kotest.core.spec.style.StringSpec
import funkotlin.fp_in_kotlin_book.chapter03.List
import io.kotest.matchers.shouldBe

class FoldableTest: StringSpec({
    "should foldRight List" {
        val l = List.of(1, 2, 3)

        ListFoldable.foldRight(l, "", { a, acc -> a.toString() + acc }) shouldBe "123"
    }

    "should foldLeft List" {
        val l = List.of(1, 2, 3)

        ListFoldable.foldLeft(l, "", { acc, b ->  b.toString() + acc }) shouldBe "321"
    }

    "should foldMap List" {
        val l = List.of(1, 2, 3)

        ListFoldable.foldMap(l, stringMonoid, { it.toString() }) shouldBe "123"
    }

    "should foldRight Tree" {
        val f = { x: Int, acc: String -> x.toString() + acc }
        TreeFoldable.foldRight(
            Leaf(1),
            "",
            f
        ) shouldBe "1"
        // and
        TreeFoldable.foldRight(
            Branch(Leaf(1), Leaf(2)),
            "",
            f
        ) shouldBe "21"
        // and
        TreeFoldable.foldRight(
            Branch(Leaf(1), Branch(Leaf(5), Leaf(3))),
            "",
            f
        ) shouldBe "351"
    }

    "should foldLeft Tree" {
        val f = { acc: String, x: Int -> x.toString() + acc }
        TreeFoldable.foldLeft(
            Leaf(1),
            "",
            f
        ) shouldBe "1"
        // and
        TreeFoldable.foldLeft(
            Branch(Leaf(1), Leaf(2)),
            "",
            f
        ) shouldBe "12"
        // and
        TreeFoldable.foldLeft(
            Branch(Leaf(1), Branch(Leaf(5), Leaf(3))),
            "",
            f
        ) shouldBe "153"
    }
})
