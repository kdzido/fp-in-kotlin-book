package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.extensions.list.foldable.firstOption
import arrow.core.lastOrNone
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropTest : StringSpec({
    "List reverse" {
        val rng = SimpleRNG(1L)
        val intList = Gen.listOfN(Gen.choose(1, 10), Gen.choose(0, 100))

        forAll(intList) { list ->
            (list.firstOption() == list.reversed().lastOrNone()) and
                    (list.reversed().reversed() == list)
        }.check(100, rng) shouldBe Passed
    }

    "Sum of list of same value" {
        val rng = SimpleRNG(1L)
        val sameValueList = Gen.listOfN(Gen.choose(1, 100), Gen.choose(50, 51))

        forAll(sameValueList) { ls ->
            (ls.sum() == ls.size * 50) and
                    (ls.reversed().sum() == ls.sum())
        }.check(100, rng) shouldBe Passed
    }

    "Sum of list" {
        val rng = SimpleRNG(1L)
        val intList = Gen.listOfN(Gen.choose(1, 10), Gen.choose(0, 100))

        forAll(intList) { ls ->
            (ls.reversed().sum() == ls.sum())
        }.check(100, rng) shouldBe Passed
    }

    "Max of list of 50s" {
        val rng = SimpleRNG(1L)
        val sameValueList = Gen.listOfN(Gen.choose(1, 100), Gen.choose(50, 51))

        forAll(sameValueList) { ls ->
            ls.max() == 50
        }.check(100, rng) shouldBe Passed
    }

    "Max of list" {
        val rng = SimpleRNG(1L)
        val intList = Gen.listOfN(Gen.choose(1, 10), Gen.choose(0, 100))

        forAll(intList) { ls ->
            ls.reversed().max() == ls.max()
        }.check(100, rng) shouldBe Passed
    }

    "should and Props" {
        val rng = SimpleRNG(1L)
        val intList = Gen.listOfN(Gen.choose(1, 10), Gen.choose(0, 100))

        forAll(intList) { list ->
            (list.firstOption() == list.reversed().lastOrNone())
        }.and(
            forAll(intList) { list ->
                (list.reversed().reversed() == list)
            }
        ).check(100, rng) shouldBe Passed
    }

    "should or props" {
        val rng = SimpleRNG(1L)
        val intList = Gen.listOfN(Gen.choose(1, 10), Gen.choose(0, 100))

        forAll(intList) { list ->
            (list.firstOption() == list.lastOrNone())
        }.or(
            forAll(intList) { list ->
                (list.reversed().reversed() == list)
            }
        ).check(100, rng) shouldBe Passed
    }
})

