package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.char
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.listOfN
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.or
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.regexp
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.succeed

interface Parser<T>

object ParseError

interface Parsers<PE> {
    fun char(c: Char): Parser<Char>
    fun string(s: String): Parser<String>
    fun regexp(regexp: String): Parser<String>

    fun char2(c: Char): Parser<Char> = string(c.toString()).map { it[0] }
    fun <A> succeed(a: A): Parser<A> = string("").map { a }

    fun charZeroOrMore(c: Char): Parser<Int>

    infix fun String.or(other: String): Parser<String>
    fun <A> or(pa: Parser<A>, pb: () -> Parser<A>): Parser<A>
    fun <A> listOfN(n: Int, p: Parser<A>): Parser<List<A>>

    fun <A> run(p: Parser<A>, input: String): Either<PE, A>
}

infix fun <T> T.cons(la: List<T>) = listOf(this) + la

fun <A> Parser<A>.many(): Parser<List<A>> =
    or(map2(this, { this.many() }) { a, la ->
        a cons la
    }, succeed(emptyList<A>()).defer() )

fun <A> Parser<A>.defer(): () -> Parser<A> = TODO()

fun <A> Parser<A>.many1(): Parser<List<A>> =
    map2(this, { this.many() }) { a: A, la: List<A> -> la }

fun <A, B> Parser<A>.map(f: (A) -> B): Parser<B> = TODO()

fun <A, B> Parser<A>.flatMap(f: (A) -> Parser<B>): Parser<B> = TODO()

fun <A, B, C> map2(pa: Parser<A>, pb: () -> Parser<B>, f: (A, B) -> C): Parser<C> =
    (pa product pb).map { (a, b) -> f(a, b) }

infix fun <A, B> Parser<A>.product(pb: () -> Parser<B>): Parser<Pair<A, B>> = TODO()

fun <A> Parser<A>.slice(): Parser<String> = TODO()

fun <A> nFollowingChars(pa: Parser<A>): Parser<Int> =
    regexp("\\d")
        .flatMap { digit: String ->
            listOfN(digit.toInt(), char('a'))
                .map { it.size }
        }

object ParsersInterpreter : Parsers<ParseError> {
    override fun char(c: Char): Parser<Char> {
        TODO("Not yet implemented")
    }
    override fun string(s: String): Parser<String> {
        TODO("Not yet implemented")
    }
    override fun regexp(regexp: String): Parser<String> {
        TODO("Not yet implemented")
    }
    override fun charZeroOrMore(c: Char): Parser<Int> =
        TODO("Not yet implemented")

    override infix fun String.or(other: String): Parser<String> {
        TODO("Not yet implemented")
    }
    override fun <A> or(
        pa: Parser<A>,
        pb: () -> Parser<A>,
    ): Parser<A> {
        TODO("Not yet implemented")
    }

    override fun <A> listOfN(
        n: Int,
        p: Parser<A>,
    ): Parser<List<A>> {
        return when {
            n == 0 -> map2(p, { succeed("") }) { a, s -> emptyList() }
            n == 1 -> map2(p, { succeed("") }) { a, s -> listOf(a) }
            n >= 2 -> map2(p, { listOfN(n - 1, p) }) { a, la -> listOf(a) + la }
            else -> throw IllegalArgumentException("n must be non-negative")
        }
    }

    override fun <A> run(
        p: Parser<A>,
        input: String,
    ): Either<ParseError, A> {
        TODO("Not yet implemented")
    }
}
