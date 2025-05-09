package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.extensions.list.foldable.exists
import arrow.core.extensions.list.foldable.firstOption
import arrow.core.lastOrNone
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import funkotlin.fp_in_kotlin_book.chapter08.Prop.Companion.forAll
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropTest : StringSpec({
    "List reverse" {
        val rng = SimpleRNG(1L)
        val intList = Gen.choose(1, 10).listOf()

        forAll(intList) { list ->
            (list.firstOption() == list.reversed().lastOrNone()) and
                    (list.reversed().reversed() == list)
        }.check(100, 100, rng) shouldBe Passed
    }

    "Sum of list of same value" {
        val rng = SimpleRNG(1L)
        val sameValueList = Gen.choose(50, 51).listOf()

        forAll(sameValueList) { ls ->
            (ls.sum() == ls.size * 50)
        }.check(100, 100, rng) shouldBe Passed
    }

    "Sum of list" {
        val rng = SimpleRNG(1L)
        val intList = Gen.choose(0, 100).listOf()

        forAll(intList) { ls ->
            (ls.reversed().sum() == ls.sum())
        }.check(100, 100, rng) shouldBe Passed
    }

    "Sum of list of 50s A" {
        val rng = SimpleRNG(1L)
        val sameValueList = Gen.choose(50, 51).listOf()

        forAll(sameValueList) { ls ->
            (ls.maxOrNull()?.let { 50 == it } ?: true)
        }.check(100, 100, rng) shouldBe Passed
    }

    "Max of list" {
        val rng = SimpleRNG(1L)
        val intList = Gen.listOfN(Gen.choose(1, 10), Gen.choose(0, 100))

        forAll(intList) { ls ->
            ls.reversed().max() == ls.max()
        }.check(100, 100, rng) shouldBe Passed
    }

    "should and Props" {
        val rng = SimpleRNG(1L)
        val intList = Gen.choose(0, 100).listOf()

        forAll(intList) { list ->
            (list.firstOption() == list.reversed().lastOrNone())
        }.and(
            forAll(intList) { list ->
                (list.reversed().reversed() == list)
            }
        ).check(100, 100, rng) shouldBe Passed
    }

    "should or props" {
        val rng = SimpleRNG(1L)
        val intList = Gen.choose(0, 100).listOf()

        forAll(intList) { list ->
            (list.firstOption() == list.lastOrNone())
        }.or(
            forAll(intList) { list ->
                (list.reversed().reversed() == list)
            }
        ).check(100, 100, rng) shouldBe Passed
    }

    "should ensure max of list is correct" {
        val rng = SimpleRNG(1L)
        val smallInt = Gen.choose(-10, 10)

        val maxProp = forAll(SGen.listOf(smallInt)) { ns ->
            val mx = ns.max()
                ?: throw IllegalStateException("max on empty list")
            !ns.exists { it > mx }
        }
    }
})

