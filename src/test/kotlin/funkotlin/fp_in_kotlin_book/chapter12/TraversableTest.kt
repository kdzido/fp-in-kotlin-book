package funkotlin.fp_in_kotlin_book.chapter12

import funkotlin.fp_in_kotlin_book.chapter03.ForList
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.catches
import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monads.listMonad
import funkotlin.fp_in_kotlin_book.chapter11.Monads.optionMonad
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TraversableTest : StringSpec({

    "should traverse over list of numbers" {
        val A = Traversables.listTraversable<String>()
        val OA: Applicative<ForOption> = optionMonad()

        val toIntO: (String) -> Option<Int> = { a -> catches { a.toInt() } }
        A.traverse(ListCh3.of(), OA, toIntO) shouldBe Some(Nil)
        A.traverse(ListCh3.of("1", "2"), OA, toIntO) shouldBe Some(ListCh3.of(1, 2))
        A.traverse(ListCh3.of("One", "2"), OA, toIntO) shouldBe None
        A.traverse(ListCh3.of("1", "Two"), OA, toIntO) shouldBe None
    }

    "should traverse over option of number" {
        val A = Traversables.optionTraversable<String>()
        val OA: Applicative<ForList> = listMonad()

        val toIntL: (String) -> ListCh3<Int> = { a -> catchesList { a.toInt() } }

        A.traverse(None, OA, toIntL) shouldBe ListCh3.of(None)
        A.traverse(Some("1"), OA, toIntL) shouldBe ListCh3.of(Some(1))
        ListCh3.of(None)
    }

    "should traverse over tree of numbers" {
        val A = Traversables.treeTraversable<String>()
        val OA: Applicative<ForOption> = optionMonad()

        val toIntO: (String) -> Option<Int> = { a -> catches { a.toInt() } }
        A.traverse(Tree("1", ListCh3.of()), OA, toIntO) shouldBe Some(Tree(1, Nil))
        A.traverse(Tree("1", ListCh3.of(Tree("2", Nil))), OA, toIntO) shouldBe Some(Tree(1, ListCh3.of(Tree(2, Nil))))
        A.traverse(Tree("One", Nil), OA, toIntO) shouldBe None
        A.traverse(Tree("1", ListCh3.of(Tree("Two", Nil))), OA, toIntO) shouldBe None
    }

    "should zipWithIndex" {
        val A = Traversables.listTraversable<Int>()

        val expectedList = ListCh3.of(
            1 to 0,
            2 to 1,
            3 to 2,
        )

        A.zipWithIndex(ListCh3.of(1, 2, 3)).fix() shouldBe expectedList
    }
})

fun <A> catchesList(a: () -> A): ListCh3<A> =
    try {
        ListCh3.of(a())
    } catch (e: Throwable) {
        ListCh3.of()
    }
