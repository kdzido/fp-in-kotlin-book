package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.extensions.list.foldable.exists
import arrow.core.extensions.list.foldable.firstOption
import arrow.core.extensions.list.foldable.forAll
import arrow.core.lastOrNone
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter07.Pars.map
import funkotlin.fp_in_kotlin_book.chapter08.Gen.Companion.listOfN
import funkotlin.fp_in_kotlin_book.chapter08.Prop.Companion.forAll
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.concurrent.Executors

class PropTest : StringSpec({
    "List reverse" {
        val rng = SimpleRNG(1L)
        val intList = Gen.choose(1, 10).listOf()

        forAll(intList) { list ->
            (list.firstOption() == list.reversed().lastOrNone()) and
                    (list.reversed().reversed() == list)
        }.run(100, 100, rng) shouldBe Passed
    }

    "Sum of list of same value" {
        val rng = SimpleRNG(1L)
        val sameValueList = Gen.choose(50, 51).listOf()

        forAll(sameValueList) { ls ->
            (ls.sum() == ls.size * 50)
        }.run(100, 100, rng) shouldBe Passed
    }

    "Sum of list" {
        val rng = SimpleRNG(1L)
        val intList = Gen.choose(0, 100).listOf()

        forAll(intList) { ls ->
            (ls.reversed().sum() == ls.sum())
        }.run(100, 100, rng) shouldBe Passed
    }

    "Sum of list of 50s A" {
        val rng = SimpleRNG(1L)
        val sameValueList = Gen.choose(50, 51).listOf()

        forAll(sameValueList) { ls ->
            (ls.maxOrNull()?.let { 50 == it } ?: true)
        }.run(100, 100, rng) shouldBe Passed
    }

    "Max of list" {
        val rng = SimpleRNG(1L)
        val intList = listOfN(Gen.choose(1, 10), Gen.choose(0, 100))

        forAll(intList) { ls ->
            ls.reversed().max() == ls.max()
        }.run(100, 100, rng) shouldBe Passed
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
        ).run(100, 100, rng) shouldBe Passed
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
        ).run(100, 100, rng) shouldBe Passed
    }

    "should ensure max of list is correct" {
        val smallInt = Gen.choose(-10, 10)

        val maxProp = forAll(SGen.nonEmptyListOf(smallInt)) { ns ->
            val mx = ns.max()
                ?: throw IllegalStateException("max on empty list")
            !ns.exists { it > mx }
        }
        Prop.run(maxProp).shouldBeInstanceOf<Passed>()
    }

    "should ensure max of sorted list" {
        val smallInt = Gen.choose(-10, 10)

        val maxProp = forAll(SGen.nonEmptyListOf(smallInt)) { ns: List<Int> ->
            val mx = ns.max()
                ?: throw IllegalStateException("max on empty list")
            ns.sorted().last() == mx
        }
        Prop.run(maxProp).shouldBeInstanceOf<Passed>()
    }

    "test pars" {
        val es = Executors.newCachedThreadPool()

        val p1 = forAll(Gen.unit(Pars.unit(1))) { pi: Par<Int> ->
            map(pi, { it + 1 }).run(es).get() == Pars.unit(2).run(es).get()
        }
        Prop.run(p1).shouldBeInstanceOf<Passed>()
    }

    "should run check" {
        Prop.run(Prop.check({ true })).shouldBeInstanceOf<Proved>()
        Prop.run(Prop.check({ false })).shouldBeInstanceOf<Falsified>()
    }

    "should takeWhile prop" {
        val isEven: (Int) -> Boolean = { i: Int -> i % 2 == 0 }
        val n = Gen.choose(0, 10)
        val ga = Gen.choose(0, 100)
        val takeWhileProp = forAll(listOfN(n, ga)) { ns ->
            ns.takeWhile(isEven).forAll(isEven)
        }
        Prop.run(takeWhileProp)
    }


    fun genStringIntFn(g: Gen<Int>): Gen<(String) -> Int> =
        g.map { i: Int -> { _: String -> i } }

    fun genIntBooleanInt(g: Gen<Boolean>): Gen<(Int) -> Boolean> =
        g.map { b: Boolean -> { _: Int -> b } }

    fun genIntBooleanFn(t: Int): Gen<(Int) -> Boolean> =
        Gen.unit { i: Int -> i > t }

    "should gen HOFs" {
        val gen: Gen<Boolean> =
            Gen.listOfN(Gen.unit(100), Gen.choose(1, 100)).flatMap { ls: List<Int> ->
            Gen.choose(1, ls.size / 2).flatMap { threshold: Int ->
                genIntBooleanFn(threshold).map { fn: (Int) -> Boolean ->
                    ls.takeWhile { threshold > it }.forAll(fn)
                }
            }
        }

        Prop.run(Prop.forAll(gen) { success -> success})
    }
})

