package funkotlin.fp_in_kotlin_book.chapter11

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

    "functor law holds for Option None" {
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
})




