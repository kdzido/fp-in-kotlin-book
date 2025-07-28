package funkotlin.fp_in_kotlin_book.chapter11

import arrow.core.compose
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
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

    "naturality law holds for Option " {
        val F: Applicative<ForOption> = optionMonad()

        // expect:
        fun format1(oe: Option<Employee>, ep: Option<Pay>): Option<String> =
            F.map2(oe, ep) { e, p ->
                "${e.name} makes ${p.rate * p.daysPerYear}"
            }.fix()
        val employee = Employee(1, "John Doe")
        val pay = Pay(600.0, 240)
        val message1: Option<String> = format1(Some(employee), Some(pay))
        message1 shouldBe Some("John Doe makes 144000.0")

        // expect:
        fun format2(oe: Option<String>, ep: Option<Double>): Option<String> =
            F.map2(oe, ep) { e, r ->
                "${e} makes ${r}"
            }.fix()
        val maybeEmployee = Some(employee)
        val maybePay = Some(pay)
        val message2: Option<String> = format2(
            F.map(maybeEmployee) { it.name }.fix(),
            F.map(maybePay) { it.rate * it.daysPerYear}.fix()
        )
        message2 shouldBe Some("John Doe makes 144000.0")

        // expect: "naturlality law holds"
        val fa = maybeEmployee
        val fb = maybePay
        val f: (Employee) -> String = { e -> e.name }
        val g: (Pay) -> Double = { p -> p.rate * p.daysPerYear }

        F.map2(fa, fb, productF(f, g)).fix() shouldBe
                F.product(F.map(fa, f), F.map(fb, g)).fix()
    }
})

data class Employee(val id: Int, val name: String)
data class Pay(val rate: Double, val daysPerYear: Int)

fun <A, B, C> assoc(p: Pair<A, Pair<B, C>>): Pair<Pair<A, B>, C> =
    (p.first to p.second.first) to p.second.second

fun <I1, O1, I2, O2> productF(
    f: (I1) -> O1,
    g: (I2) -> O2
): (I1, I2) -> Pair<O1, O2> =
    { i1, i2 -> f(i1) to g(i2) }



