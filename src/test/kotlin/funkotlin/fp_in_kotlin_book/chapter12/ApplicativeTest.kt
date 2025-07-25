package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import funkotlin.fp_in_kotlin_book.chapter08.ForGen
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix
import funkotlin.fp_in_kotlin_book.chapter11.flatMap
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class ApplicativeTest : StringSpec({

    "genApplicative" should {
        val A = object : Applicative<ForGen> {
            override fun <A> unit(a: A): Kind<ForGen, A> = Gen.unit(a)

            override fun <A, B, C> map2(
                fa: Kind<ForGen, A>,
                fb: Kind<ForGen, B>,
                f: (A, B) -> C,
            ): Kind<ForGen, C> =
                fa.fix().flatMap { a -> fb.fix().map { b -> f(a, b) } }
        }

        "replicateM for genApplicative" {
            val rng = SimpleRNG(1)
            val (l1, rng2) = A.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng)
            val (l2, rng3) = A.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng2)
            val (l3, rng4) = A.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng3)
            val (l4, rng5) = A.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng4)

            l1 shouldBe ListCh3.of(8, 5, 6, 7, 2)
            l2 shouldBe ListCh3.of(3, 8, 7, 9, 1)
            l3 shouldBe ListCh3.of(8, 5, 9, 5, 2)
            l4 shouldBe ListCh3.of(9, 4, 1, 1, 5)
        }
    }

    "optionApplicative " should {
        val A = object : Applicative<ForOption> {
            override fun <A> unit(a: A): Kind<ForOption, A> = Some(a)

            override fun <A, B, C> map2(
                fa: Kind<ForOption, A>,
                fb: Kind<ForOption, B>,
                f: (A, B) -> C,
            ): Kind<ForOption, C> =
                fa.flatMap { a -> fb.flatMap { b -> unit(f(a, b)) } }
        }

        "optionApplicative should sequence" {
            A.sequence<Int>(ListCh3.of()).fix() shouldBe Some(Nil)
            A.sequence<Int>(ListCh3.of(None)).fix() shouldBe None
            A.sequence(ListCh3.of(None, Some(1))).fix() shouldBe None
            A.sequence(ListCh3.of(Some(1), None)).fix() shouldBe None
            A.sequence(ListCh3.of(Some(1), Some(2))).fix() shouldBe Some(ListCh3.of(1, 2))
        }

        "optionApplicative should product" {
            A.product(Some(1), Some(2)).fix() shouldBe Some(Pair(1, 2))
            A.product(None, Some(2)).fix() shouldBe None
            A.product(Some(1), None).fix() shouldBe None
            A.product(None, None).fix() shouldBe None
        }

        "optionApplicative should map" {
            A.map(Some(1)) { a -> (a + 1).toString() }.fix() shouldBe Some("2")
        }

        "optionApplicative should apply" {
            A.apply(Some({ a -> (a + 1).toString() }), Some(1)).fix() shouldBe Some("2")
        }
    }
})
