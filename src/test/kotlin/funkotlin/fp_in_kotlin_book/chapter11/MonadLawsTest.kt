package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.OptionOf
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter04.flatMap
import funkotlin.fp_in_kotlin_book.chapter11.Monads.optionMonad
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MonadLawsTest : StringSpec({
    "associativity law holds for Option None" {
        val m = optionMonad()

        val x = None
        val f: (Int) -> Option<Int> = { x -> None }
        val g: (Int) -> Option<Int> = { x -> None }

        x.flatMap(f).flatMap(g) shouldBe
                x.flatMap { a -> f(a).flatMap(g) }
    }

    "associativity law holds for Option Some" {
        val m = optionMonad()

        val x = Some(1)
        val f: (Int) -> Option<Int> = { x -> Some(x + 1) }
        val g: (Int) -> Option<Int> = { x -> Some(x * 2) }

        x.flatMap(f).flatMap(g) shouldBe
                x.flatMap { a -> f(a).flatMap(g) }

        // given: "proving associativity for Some, Listing 11.11"
        // expect: "original associativity law"
        x.flatMap(f).flatMap(g) shouldBe
                x.flatMap { a -> f(a).flatMap(g) }
        // and: "replace x with Some(v) on both sides
        val v = 2
        Some(v).flatMap(f).flatMap(g) shouldBe
                Some(v).flatMap { a -> f(a).flatMap(g) }
        // and: "collapse Some(v).flatMap on both sides by applying v to f directly"
        f(v).flatMap(g) shouldBe
                { a: Int -> f(a).flatMap(g) }(v)
        // and: "apply v to g directly on the right side, proving equality
        f(v).flatMap(g) shouldBe
                f(v).flatMap(g)
    }

    "law of associativity in terms of compose" {
        val m = optionMonad()

        val v = 1
        val f: (Int) -> OptionOf<Int> = { x -> Some(x + 1) }
        val g: (Int) -> OptionOf<Int> = { x -> Some(x * 2) }
        val h: (Int) -> OptionOf<Int> = { x -> Some(x * x) }

        m.compose(m.compose(f, g), h)(v).fix() shouldBe
                m.compose(f, m.compose(g, h))(v).fix()
    }

    "verify associativity in terms of flatMap and compose" {
        val m = optionMonad()

        val v = 1
        val f: (Int) -> OptionOf<Int> = { x -> Some(x + 1) }
        val g: (Int) -> OptionOf<Int> = { x -> Some(x * 2) }
        val h: (Int) -> OptionOf<Int> = { x -> Some(x * x) }

        val left = m.compose(m.compose(f, g), h)(v).fix()
        val right = m.compose(f, m.compose(g, h))(v).fix()

        // expect: "left side reduction"
        m.compose(m.compose(f, g), h)(v).fix() shouldBe right
        { a: Int -> m.flatMap(m.compose(f, g)(a), h) }(v).fix() shouldBe right
        { a: Int -> m.flatMap({ b: Int -> m.flatMap(f(b), g) }(a), h) }(v).fix() shouldBe right
        { a: Int -> m.flatMap(m.flatMap(f(a), g), h) }(v).fix() shouldBe right
        { a: Int ->
            val x = f(a)
            m.flatMap(m.flatMap(x, g), h)
        }(v).fix() shouldBe right
        // and
        { a: Int ->
            val x = f(a)
            m.flatMap(m.flatMap(x, g), h)
        }(v).fix() shouldBe right

        // expect: "right side reduction"
        left shouldBe m.compose(f, m.compose(g, h))(v).fix()
        left shouldBe { a: Int -> m.flatMap(f(a), m.compose(g, h)) }(v).fix()
        left shouldBe { a: Int -> m.flatMap(f(a)) { b: Int -> m.flatMap(g(b), h) } }(v).fix()
        // and
        left shouldBe { a: Int ->
            val x = f(a)
            m.flatMap(x) { b: Int -> m.flatMap(g(b), h) }
        }(v).fix()

        // expect: "the final law"
        val leftSide = { a: Int ->
            val x = f(a)
            m.flatMap(m.flatMap(x, g), h)
        }(v).fix()
        val rightSide = { a: Int ->
            val x = f(a)
            m.flatMap(x) { b: Int -> m.flatMap(g(b), h) }
        }(v).fix()
        leftSide shouldBe rightSide

        // and: "more expressive with ext fun"
        { a: Int ->
            val x = f(a)
            x.flatMap(g).flatMap(h)
        }(v).fix() shouldBe
                { a: Int ->
                    val x = f(a)
                    x.flatMap { b: Int -> g(b).flatMap(h) }
                }(v).fix()
    }
})

fun <A, B> Kind<ForOption, A>.flatMap(
    f: (A) -> Kind<ForOption, B>
): Kind<ForOption, B> =
    Monads.optionMonad().flatMap(this, f)



