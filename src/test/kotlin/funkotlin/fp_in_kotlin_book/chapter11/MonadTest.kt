package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import arrow.core.ListK
import arrow.core.ListKOf
import arrow.core.SequenceK
import arrow.core.SequenceKOf
import arrow.core.fix
import funkotlin.fp_in_kotlin_book.chapter03.ListOf
import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.OptionOf
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

    "_replicateM for genMonad" {
        val rng = SimpleRNG(1)

        val m = genMonad
        val (l1, rng2) = m._replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng)
        val (l2, rng3) = m._replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng2)
        val (l3, rng4) = m._replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng3)
        val (l4, rng5) = m._replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng4)

        l1 shouldBe ListCh3.of(8, 5, 6, 7, 2)
        l2 shouldBe ListCh3.of(3, 8, 7, 9, 1)
        l3 shouldBe ListCh3.of(8, 5, 9, 5, 2)
        l4 shouldBe ListCh3.of(9, 4, 1, 1, 5)
    }

    "replicateM for genMonad" {
        val rng = SimpleRNG(1)

        val m = genMonad
        val (l1, rng2) = m.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng)
        val (l2, rng3) = m.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng2)
        val (l3, rng4) = m.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng3)
        val (l4, rng5) = m.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng4)

        l1 shouldBe ListCh3.of(8, 5, 6, 7, 2)
        l2 shouldBe ListCh3.of(3, 8, 7, 9, 1)
        l3 shouldBe ListCh3.of(8, 5, 9, 5, 2)
        l4 shouldBe ListCh3.of(9, 4, 1, 1, 5)
    }

    "describe replicateM on string" {
        val lm = listMonad()

        lm.replicateM(0, ListCh3.of(1, 2, 3)).fix() shouldBe
                ListCh3.of(ListCh3.of())

        lm.replicateM(1, ListCh3.of(1, 2, 3)).fix() shouldBe
                ListCh3.of(
                    ListCh3.of(1),
                    ListCh3.of(2),
                    ListCh3.of(3)
                )

        lm.replicateM(2, ListCh3.of(1, 2, 3)).fix() shouldBe
                ListCh3.of(
                    ListCh3.of(1, 1),
                    ListCh3.of(1, 2),
                    ListCh3.of(1, 3),
                    ListCh3.of(2, 1),
                    ListCh3.of(2, 2),
                    ListCh3.of(2, 3),
                    ListCh3.of(3, 1),
                    ListCh3.of(3, 2),
                    ListCh3.of(3, 3),
                )

    }

    "describe replicateM for optionMonad" {
        val om = optionMonad()

        om.replicateM(0, Some(1)).fix() shouldBe Some(ListCh3.of())
        om.replicateM(1, Some(1)).fix() shouldBe Some(ListCh3.of(1))
        om.replicateM(3, Some(1)).fix() shouldBe Some(ListCh3.of(1, 1, 1))

        om.replicateM(0, None).fix() shouldBe Some(ListCh3.of())
        om.replicateM(1, None).fix() shouldBe None
        om.replicateM(3, None).fix() shouldBe None
    }

    "filterM for optionMonad" {
        val m = optionMonad()

        val fS: (Int) -> Kind<ForOption, Boolean> = { a -> Some(a % 2 == 0) }
        val fN: (Int) -> Kind<ForOption, Boolean> = { a -> None }

        m.filterM<Int>(Nil, { _ -> Some(true) }).fix() shouldBe Some(Nil)
        m.filterM<Int>(Nil, { _ -> None }).fix() shouldBe Some(Nil)

        m.filterM(ListCh3.of(2)) { a -> Some(true) }.fix() shouldBe Some(ListCh3.of(2))
        m.filterM(ListCh3.of(2)) { a -> Some(false) }.fix() shouldBe Some(ListCh3.of())
        m.filterM(ListCh3.of(2)) { a -> Some(a % 2 == 0) }.fix() shouldBe Some(ListCh3.of(2))
        m.filterM(ListCh3.of(2)) { a -> None }.fix() shouldBe None
        // and
        m.filterM(ListCh3.of(1)) { a -> Some(true) }.fix() shouldBe Some(ListCh3.of(1))
        m.filterM(ListCh3.of(1)) { a -> Some(false) }.fix() shouldBe Some(ListCh3.of())
        m.filterM(ListCh3.of(1)) { a -> Some(a % 2 == 0) }.fix() shouldBe Some(ListCh3.of())
        m.filterM(ListCh3.of(1)) { a -> None }.fix() shouldBe None

        // and
        val l = ListCh3.of(1, 2, 3, 4)
        m.filterM(l) { a -> None }.fix() shouldBe None
        m.filterM(l) { a -> Some(a % 2 == 0) }.fix() shouldBe Some(ListCh3.of(2, 4))
    }

    "optionMonad should compose" {
        val m = optionMonad()

        val k1: (Int) -> OptionOf<String> = { i: Int ->
            when (i) {
                0 -> Some("zero")
                1 -> Some("one")
                2 -> Some("two")
                else -> None
            }
        }
        val k2: (String) -> OptionOf<String> = { s: String -> Some("__" + s ) }
        val k3: (Int) -> OptionOf<String> = m.compose(k1, k2)

        k3(-1) shouldBe None
        k3(0) shouldBe Some("__zero")
        k3(1) shouldBe Some("__one")
        k3(2) shouldBe Some("__two")
        k3(3) shouldBe None
    }

    "prove identity laws for option monad" {
        val m = optionMonad()
        val v = 123

        // expect: "right identity"
        m.compose<Int, Int, Int>({ a -> None} , { a -> m.unit(a) })(v).fix() shouldBe None
        m.compose<Int, Int, Int>({ a -> Some(a)} , { a -> m.unit(a) })(v).fix() shouldBe Some(v)
        // and:
        m.flatMap(None) { a -> m.unit(a) }.fix() shouldBe None
        m.flatMap(Some(v)) { a -> m.unit(a) }.fix() shouldBe Some(v)

        // expect: "left identity"
        m.compose<Int, Int, Int>({ a -> m.unit(a) }, { a -> None} )(v).fix() shouldBe None
        m.compose<Int, Int, Int>({ a -> m.unit(a) }, { a -> Some(a)} , )(v).fix() shouldBe Some(v)
        // and:
        m.flatMap(Some(None)) { a -> m.unit(a) }.fix() shouldBe Some(None)
        m.flatMap(Some(Some(v))) { a -> m.unit(a) }.fix() shouldBe Some(Some(v))
    }
})

enum class EnumKey { ONE, TWO, THREE }
