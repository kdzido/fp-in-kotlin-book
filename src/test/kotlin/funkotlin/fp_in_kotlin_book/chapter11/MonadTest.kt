package funkotlin.fp_in_kotlin_book.chapter11

import arrow.core.ListK
import arrow.core.ListKOf
import arrow.core.SequenceK
import arrow.core.SequenceKOf
import arrow.core.fix
import funkotlin.fp_in_kotlin_book.chapter03.ListOf
import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.catches
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter06.RNG
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import funkotlin.fp_in_kotlin_book.chapter06.State
import funkotlin.fp_in_kotlin_book.chapter06.fix
import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.Pars.shouldBePar
import funkotlin.fp_in_kotlin_book.chapter07.fix
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monads.genMonad
import funkotlin.fp_in_kotlin_book.chapter11.Monads.listKMonad
import funkotlin.fp_in_kotlin_book.chapter11.Monads.listMonad
import funkotlin.fp_in_kotlin_book.chapter11.Monads.optionMonad
import funkotlin.fp_in_kotlin_book.chapter11.Monads.parMonad
import funkotlin.fp_in_kotlin_book.chapter11.Monads.sequenceKMonad
import funkotlin.fp_in_kotlin_book.chapter11.Monads.stateMonad
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors

class MonadTest : StringSpec({
    "genMonad should flatMap" {
        val rng = SimpleRNG(1)
        val gen = Gen.choose(1, 6)
        fun genTransform(a: Int): Gen<List<Int>> = Gen.listOfSpecifiedN(a, Gen.choose(1, 10))

        val m = genMonad
        m.flatMap(m.unit(1)) { a: Int -> m.unit(a + 1) }
            .fix().sample.run(rng).first shouldBe 2

        val (l1, rng2) = m.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng)
        val (l2, rng3) = m.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng2)
        val (l3, rng4) = m.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng3)
        val (l4, rng5) = m.flatMap(gen) { a -> genTransform(a) }
            .fix().sample.run(rng4)

        l1 shouldBe listOf(5, 6, 7, 2)
        l2 shouldBe listOf(8, 7, 9, 1, 8)
        l3 shouldBe listOf(9, 5, 2, 9, 4)
        l4 shouldBe listOf(1)
    }

    "parMonad should flatMap" {
        val es = Executors.newFixedThreadPool(1)

        val m = parMonad()
        // TODO no fixing
        val a: Par<Int> = m.unit(42).fix()
        val b: Par<Int> = m.unit(8).fix()
        val c: Par<Int> = m.unit(3).fix()

        val dict = mapOf(EnumKey.ONE to a, EnumKey.TWO to b, EnumKey.THREE to c)
        val choices: (EnumKey) -> Par<Int> = { k -> dict.getValue(k) }

        (m.flatMap(m.unit(EnumKey.ONE), choices) shouldBePar m.unit(42))(es)
        (m.flatMap(m.unit(EnumKey.TWO), choices) shouldBePar m.unit(8))(es)
        (m.flatMap(m.unit(EnumKey.THREE), choices) shouldBePar m.unit(3))(es)
    }

    "optionMonad should flatMap" {
        val m = optionMonad()

        m.flatMap(None) { a -> Some(123) }.fix() shouldBe None
        m.flatMap(Some(1)) { a -> Some(a + 1) }.fix() shouldBe Some(2)
        m.flatMap(Some(1)) { a -> None }.fix() shouldBe None
    }

    "listMonad should flatMap" {
        val m = listMonad()

        val f: (Double) -> ListOf<String> = { x -> ListCh3.of(x.toString(), x.toString()) }
        m.flatMap(Nil as ListCh3<Double>, f).fix() shouldBe Nil
        m.flatMap(ListCh3.of(3.14), f).fix() shouldBe ListCh3.of("3.14", "3.14")
        m.flatMap(ListCh3.of(3.14, 2.18, 1.23), f).fix() shouldBe ListCh3.of(
            "3.14", "3.14",
            "2.18", "2.18",
            "1.23", "1.23"
        )
    }

    "listKMonad should flatMap" {
        val m = listKMonad()

        val f: (Double) -> ListKOf<String> = { x -> ListK(listOf(x.toString(), x.toString())) }
        m.flatMap(ListK.empty(), f).fix() shouldBe ListK.empty()
        m.flatMap(ListK(listOf(3.14)), f).fix() shouldBe ListK(listOf("3.14", "3.14"))
        m.flatMap(ListK(listOf(3.14, 2.18, 1.23)), f).fix() shouldBe ListK(
            listOf(
                "3.14", "3.14",
                "2.18", "2.18",
                "1.23", "1.23"
            )
        )
    }

    "sequenceKMonad should flatMap" {
        val m = sequenceKMonad()

        val f: (Double) -> SequenceKOf<String> = { x -> SequenceK(sequenceOf(x.toString(), x.toString())) }
        m.flatMap(SequenceK.empty(), f).fix().toList() shouldBe ListK.empty()
        m.flatMap(SequenceK(sequenceOf(3.14)), f).fix().toList() shouldBe ListK(listOf("3.14", "3.14"))
        m.flatMap(SequenceK(sequenceOf(3.14, 2.18, 1.23)), f).fix().toList() shouldBe ListK(
            listOf(
                "3.14", "3.14",
                "2.18", "2.18",
                "1.23", "1.23"
            )
        )
    }

    "stateMonad should flatMap" {
        val rng = SimpleRNG(42)
        val f: State<RNG, Int> = State { r -> RNG.nonNegativeInt().run(r) }

        val m = stateMonad<RNG>()

        val incr1 = m.flatMap(f) { i -> m.unit(i + 1) }.fix()
        val (n2, rng2) = incr1.run(rng)
        val (n3, rng3) = incr1.run(rng2)
        n2 shouldBe 16159454
        n3 shouldBe 1281479698
    }

    "optionMonad should sequence" {
        val m = optionMonad()

        m.sequence<Int>(ListCh3.of()).fix() shouldBe Some(Nil)
        m.sequence<Int>(ListCh3.of(None)).fix() shouldBe None
        m.sequence(ListCh3.of(None, Some(1))).fix() shouldBe None
        m.sequence(ListCh3.of(Some(1), None)).fix() shouldBe None
        m.sequence(ListCh3.of(Some(1), Some(2))).fix() shouldBe Some(ListCh3.of(1, 2))
    }

    "optionMonad should traverse list of ints" {
        val m = optionMonad()

        val toIntO: (String) -> Option<Int> = { a -> catches { a.toInt() } }
        m.traverse(ListCh3.of(), toIntO).fix() shouldBe Some(Nil)
        m.traverse(ListCh3.of("1", "2"), toIntO).fix() shouldBe Some(ListCh3.of(1, 2))
        m.traverse(ListCh3.of("One", "2"), toIntO).fix() shouldBe None
        m.traverse(ListCh3.of("1", "Two"), toIntO).fix() shouldBe None
    }
})

enum class EnumKey { ONE, TWO, THREE }
