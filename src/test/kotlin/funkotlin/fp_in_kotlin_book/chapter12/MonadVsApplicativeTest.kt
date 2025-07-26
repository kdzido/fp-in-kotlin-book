package funkotlin.fp_in_kotlin_book.chapter12

import java.util.Date
import arrow.Kind
import arrow.core.Option
import arrow.core.toOption
import arrow.core.Some
import arrow.core.ForOption
import arrow.core.fix
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter09.ForParser
import funkotlin.fp_in_kotlin_book.chapter09.Parser
import funkotlin.fp_in_kotlin_book.chapter09.fix
import funkotlin.fp_in_kotlin_book.chapter05.Stream
import funkotlin.fp_in_kotlin_book.chapter05.ForStream
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.take
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.zipWith
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.toList
import funkotlin.fp_in_kotlin_book.chapter05.StreamOf
import funkotlin.fp_in_kotlin_book.chapter05.fix
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

import funkotlin.fp_in_kotlin_book.chapter11.Monad

class MonadVsApplicativeTest : StringSpec({
    "optionApplicative " should {
        // given: "applicative instance"
        val FA = object : Applicative<ForOption> {
            override fun <A> unit(a: A): Kind<ForOption, A> = Some(a)

            override fun <A, B, C> map2(
                fa: Kind<ForOption, A>,
                fb: Kind<ForOption, B>,
                f: (A, B) -> C,
            ): Kind<ForOption, C> =
                fa.fix().flatMap { a -> fb.fix().flatMap { b -> unit(f(a, b)) } }
        }
        // given: "monad instance"
        val FM = object : Monad<ForOption> {
            override fun <A> unit(a: A): Kind<ForOption, A> = Some(a)

            override fun <A, B> flatMap(
                fa: Kind<ForOption, A>,
                f: (A) -> Kind<ForOption, B>,
            ): Kind<ForOption, B> =
                fa.fix().flatMap { a -> f(a).fix() }
        }

        "independent lookups using optionApplicative" {
            val employee = "Alice"
            val departments: Map<String, String> = mapOf("Alice" to "Tech")
            val salaries: Map<String, Double> = mapOf("Alice" to 100_000.0)

            val o: Option<String> = FA.map2(
                departments[employee].toOption(),
                salaries[employee].toOption(),
            ) { dept: String, salary: Double ->
                "$employee in $dept makes $salary per year."
            }.fix()

            o shouldBe Some("Alice in Tech makes 100000.0 per year.")
        }

        "dependent lookups using optionMonad" {
            val employee = "Bob"
            val idByName: Map<String, Int> = mapOf("Bob" to 101)
            val departments: Map<Int, String> = mapOf(101 to "Sales")
            val salaries: Map<Int, Double> = mapOf(101 to 100_000.0)

            val o: Option<String> = idByName[employee].toOption().flatMap { id ->
                FM.map2(
                    departments[id].toOption(),
                    salaries[id].toOption(),
                ) { dept: String, salary: Double ->
                    "$employee in $dept makes $salary per year."
                }
            }.fix()

            o shouldBe Some("Bob in Sales makes 100000.0 per year.")
        }
    }

    "parser applicative vs monad" should {
        data class Row(val date: Date, val temp: Double)
        fun <A> Parser<A>.sep(s: String): Parser<List<Row>> = TODO()

        "parserApplicative" {
            // given: "applicative instance"
            val F:  Applicative<ForParser> = TODO()

            val date: Parser<Date> = TODO()
            val temp: Parser<Double> = TODO()

            val row: Parser<Row> = F.map2(date, temp) { d, t -> Row(d, t) }.fix()
            val rows: Parser<List<Row>> = row.sep("\n")
        }

        "parserMonad" {
            // given: "monad instance"
            val F:  Monad<ForParser> = TODO()

            val header: Parser<Parser<Row>> = TODO()
            val rows: Parser<List<Row>> = F.flatMap(header) { row: Parser<Row> ->
                row.sep("\n")
            }.fix()
        }
    }

    "streamApplicative" should {
        // given: "applicative instance"
        val F:  Applicative<ForStream> = object : Applicative<ForStream> {
            override fun <A> unit(a: A): Kind<ForStream, A> =
                Stream.constant(a)

            override fun <A, B, C> map2(
                fa: StreamOf<A>,
                fb: StreamOf<B>,
                f: (A, B) -> C,
            ): StreamOf<C> =
                fa.fix().zipWith(fb.fix()) { a, b -> f(a, b) }
        }

        "streamApplicative sequence" {
            val s1 = Stream.constant(1)
            val s2 = Stream.constant(2)
            val s3 = Stream.constant(3)
            val s4 = Stream.constant(4)
            val zs = F.sequence(ListL.of(
                s1,
                s2,
                s3,
                s4,
            )).fix()

            zs.take(4).toList() shouldBe ListL.of(
                ListL.of(1, 2, 3, 4),
                ListL.of(1, 2, 3, 4),
                ListL.of(1, 2, 3, 4),
                ListL.of(1, 2, 3, 4),
            )
        }
    }
})
