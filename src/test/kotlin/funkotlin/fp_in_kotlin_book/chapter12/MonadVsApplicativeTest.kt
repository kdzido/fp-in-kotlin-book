package funkotlin.fp_in_kotlin_book.chapter12

import java.util.Date
import arrow.Kind
import arrow.core.Option
import arrow.core.toOption
import arrow.core.Some
import arrow.core.ForOption
import arrow.core.fix
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter04.fix
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
import funkotlin.fp_in_kotlin_book.chapter12.Validations.validateWebForm
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

    "eitherMonad"  should {

        "eitherMonad operations" {
            val M = eitherMonad<Int>()

            val e1 = M.unit("one").fix()
            val e2 = M.unit("123").fix()

            // expect:
            e1 shouldBe Right("one")
            e2 shouldBe Right("123")
            // and:
            M.flatMap(e1) { s: String ->
                if (s.contains(Regex("[0-9]+"))) Right(s.toInt()) else Left(1)
            }.fix() shouldBe Left(1)
            // and:
            M.flatMap(e2) { s: String ->
                if (s.contains(Regex("[0-9]+"))) Right(s.toInt()) else Left(1)
            }.fix() shouldBe Right(123)
        }

        "eitherMonad WebForm successful validation" {
            val F = eitherMonad<String>()

            val name = "alice"
            val dob = "2025-01-01"
            val phone = "123-456-789"

            F.flatMap(ValidationsEither.validName(name)) { f1: String ->
                F.flatMap(ValidationsEither.validDateOfBirth(dob)) { f2: Date ->
                    F.map(ValidationsEither.validPhone(phone)) { f3: String ->
                        WebForm(f1, f2, f3)
                    }
                }
            } shouldBe
                    Right(WebForm(name, Date(1234567), phone))
        }
    }

    "eitherApplicative" should {
        "eitherApplicative operations" {
            val M = eitherApplicative<Int>()

            // expect:
            val e1 = M.unit("one").fix()
            e1 shouldBe Right("one")

            // expect:
            M.map2(Right("ab"), Right("cde")) { s1, s2 ->
                s1.length + s2.length
            }.fix() shouldBe Right(5)
            // and:
            M.map2<String, String, Int>(Left(1), Left(2)) { s1, s2 ->
                s1.length + s2.length
            }.fix() shouldBe Left(1)
            // and:
            M.map2<String, String, Int>(Left(1), Right("ab")) { s1, s2 ->
                s1.length + s2.length
            }.fix() shouldBe Left(1)
            // and:
            M.map2<String, String, Int>(Right("ab"), Left(2)) { s1, s2 ->
                s1.length + s2.length
            }.fix() shouldBe Left(2)
        }

        "eitherApplicative successful validation" {
            val M = eitherApplicative<String>()

            val name = "alice"
            val dob = "2025-01-01"
            val phone = "123-456-789"

            // expect:
            M.map3(
                ValidationsEither.validName(name),
                ValidationsEither.validDateOfBirth(dob),
                ValidationsEither.validPhone(phone),
            ) { f1, f2, f3 -> WebForm(f1, f2, f3) }.fix() shouldBe
                    Right(WebForm(name, Date(1234567), phone))
        }
    }

    "validationApplicative" should {
        "validationApplicative successful validation" {
            val M = validationApplicative<String>()

            val name = "alice"
            val dob = "2025-01-01"
            val dobExpectedMillis = 1735686000000L
            val phone = "1234567891"

            // expect:
            validateWebForm(
                name = name,
                dob = dob,
                phone = phone,
            ) shouldBe
                    Success(WebForm(name, Date(dobExpectedMillis), phone))
        }

        "validationApplicative failed validation" {
            val M = validationApplicative<String>()

            // expect:
            validateWebForm(
                name = "",
                dob = "",
                phone = "",
            ) shouldBe
                    Failure(
                        head = "<Phone number must be 10 digits>",
                        tail = ListL.of(
                            "<Date of birth must be in format yyyy-MM-dd>",
                            "<Name cannot be empty>",
                        )
                    )
            // expect:
            validateWebForm(
                name = "Alice",
                dob = "",
                phone = "",
            ) shouldBe
                    Failure(
                        head = "<Phone number must be 10 digits>",
                        tail = ListL.of(
                            "<Date of birth must be in format yyyy-MM-dd>",
                        )
                    )
            // expect:
            validateWebForm(
                name = "",
                dob = "2025-01-01",
                phone = "",
            ) shouldBe
                    Failure(
                        head = "<Phone number must be 10 digits>",
                        tail = ListL.of(
                            "<Name cannot be empty>",
                        )
                    )
            // expect:
            validateWebForm(
                name = "",
                dob = "",
                phone = "1234567890",
            ) shouldBe
                    Failure(
                        head = "<Date of birth must be in format yyyy-MM-dd>",
                        tail = ListL.of(
                            "<Name cannot be empty>",
                        )
                    )
        }
    }
})
