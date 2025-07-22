package funkotlin.fp_in_kotlin_book.chapter12

import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix
import funkotlin.fp_in_kotlin_book.chapter12.Applicatives.genApplicative
import funkotlin.fp_in_kotlin_book.chapter12.Applicatives.optionApplicative
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ApplicativeTest : StringSpec({

    "replicateM for genApplicative" {
        val rng = SimpleRNG(1)

        val a = genApplicative
        val (l1, rng2) = a.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng)
        val (l2, rng3) = a.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng2)
        val (l3, rng4) = a.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng3)
        val (l4, rng5) = a.replicateM(5, Gen.choose(1, 10)).fix().sample.run(rng4)

        l1 shouldBe ListCh3.of(8, 5, 6, 7, 2)
        l2 shouldBe ListCh3.of(3, 8, 7, 9, 1)
        l3 shouldBe ListCh3.of(8, 5, 9, 5, 2)
        l4 shouldBe ListCh3.of(9, 4, 1, 1, 5)
    }

    "optionApplicative should sequence" {
        val a = optionApplicative()

        a.sequence<Int>(ListCh3.of()).fix() shouldBe Some(Nil)
        a.sequence<Int>(ListCh3.of(None)).fix() shouldBe None
        a.sequence(ListCh3.of(None, Some(1))).fix() shouldBe None
        a.sequence(ListCh3.of(Some(1), None)).fix() shouldBe None
        a.sequence(ListCh3.of(Some(1), Some(2))).fix() shouldBe Some(ListCh3.of(1, 2))
    }

    "optionApplicative should product" {
        val a = optionApplicative()

        a.product(Some(1), Some(2)).fix() shouldBe Some(Pair(1, 2))
        a.product(None, Some(2)).fix() shouldBe None
        a.product(Some(1), None).fix() shouldBe None
        a.product(None, None).fix() shouldBe None
    }

    "optionApplicative should map" {
        val a = optionApplicative()

        a.map(Some(1)) { a -> (a + 1).toString() }.fix() shouldBe Some("2")
    }

    "optionApplicative should product" {
        val a = optionApplicative()

        a.apply(Some({ a -> (a + 1).toString() }), Some(1)).fix() shouldBe Some("2")
    }

})
