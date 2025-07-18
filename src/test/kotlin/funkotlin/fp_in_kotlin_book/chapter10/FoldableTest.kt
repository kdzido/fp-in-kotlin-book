package funkotlin.fp_in_kotlin_book.chapter10

import funkotlin.fp_in_kotlin_book.chapter03.Branch
import funkotlin.fp_in_kotlin_book.chapter03.Leaf
import io.kotest.core.spec.style.StringSpec
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
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

    "should foldRight Option" {
        OptionFoldable.foldRight(None, "", { a, acc -> a.toString() + acc }) shouldBe ""
        OptionFoldable.foldRight(Some(1), "", { a, acc -> a.toString() + acc }) shouldBe "1"
    }

    "should foldLeft Option" {
        OptionFoldable.foldLeft(None, "", { acc, b ->  b.toString() + acc }) shouldBe ""
        OptionFoldable.foldLeft(Some(1), "", { acc, b ->  b.toString() + acc }) shouldBe "1"
    }

    "should toList any foldable" {
        ListFoldable.toList(List.of<Int>()) shouldBe List.of()
        ListFoldable.toList(List.of<Int>(1, 2, 3)) shouldBe List.of(3, 2, 1)
        // and
        TreeFoldable.toList(Leaf(5)) shouldBe List.of(5)
        TreeFoldable.toList(Branch(Leaf(1), Branch(Leaf(5), Leaf(3)))) shouldBe
                List.of(1, 5, 3)
        // and
        OptionFoldable.toList(None) shouldBe List.of()
        OptionFoldable.toList(Some(1)) shouldBe List.of(1)
    }
})
