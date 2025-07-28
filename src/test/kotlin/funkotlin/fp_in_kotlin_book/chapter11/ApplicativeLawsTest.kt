package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import arrow.core.compose
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.OptionOf
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter04.flatMap
import funkotlin.fp_in_kotlin_book.chapter11.Monads.optionMonad
import funkotlin.fp_in_kotlin_book.chapter12.Applicative
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ApplicativeLawsTest : StringSpec({

    "functor law holds for Option" {
        val m: Applicative<ForOption> = optionMonad()

        val v0 = None
        val v1 = Some(3)
        val f: (Int) -> Int = { x -> x + 1 }
        val g: (Int) -> Int = { x -> x + 2 }

        // expect:
        m.map(m.map(v0, f), g).fix() shouldBe
                m.map(v0, f compose g).fix()
        // and:
        m.map(m.map(v1, f), g).fix() shouldBe
                m.map(v1, f compose g).fix()
    }

    "applicative left and right identity law holds for Option" {
        val m: Applicative<ForOption> = optionMonad()

        val v0 = None
        val v1 = Some(3)

        // expect: "left identity"
        m.map2(m.unit(Unit), v0) { _, a -> a }.fix() shouldBe v0
        m.map2(m.unit(Unit), v1) { _, a -> a }.fix() shouldBe v1
        // expect: "right identity"
        m.map2(v0, m.unit(Unit)) { a, _ -> a }.fix() shouldBe v0
        m.map2(v1, m.unit(Unit)) { a, _ -> a }.fix() shouldBe v1
    }

    "associativity law holds for Option in terms of product with assoc" {
        val m: Applicative<ForOption> = optionMonad()

        val v0 = None
        val f: Option<Int> = Some(1)
        val g:  Option<String> = Some("two")
        val h:Option<Double> = Some(3.14)

        // expect:
        m.product(m.product(f, g), h) shouldBe
                m.map(m.product(f, m.product(g, h)), ::assoc)
        // expect:
        m.product(m.product(v0, g), h) shouldBe
                m.map(m.product(v0, m.product(g, h)), ::assoc)
        // and:
        m.product(m.product(f, v0), h) shouldBe
                m.map(m.product(f, m.product(v0, h)), ::assoc)
        // and:
        m.product(m.product(f, g), v0) shouldBe
                m.map(m.product(f, m.product(g, v0)), ::assoc)
    }

})

fun <A, B, C> assoc(p: Pair<A, Pair<B, C>>): Pair<Pair<A, B>, C> =
    (p.first to p.second.first) to p.second.second




