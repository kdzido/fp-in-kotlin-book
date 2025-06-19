package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter09.Parsers.Parser

interface Parsers<PE> {
    interface Parser<T>

    fun char(c: Char): Parser<Char>
    fun string(s: String): Parser<String>

    fun char2(c: Char): Parser<Char> = string(c.toString()).map { it[0] }
    fun <A> succeed(a: A): Parser<A> = string("").map { a }

    fun charZeroOrMore(c: Char): Parser<Int>


    infix fun String.or(other: String): Parser<String>
    fun <A> or(pa: Parser<A>, pb: Parser<A>): Parser<A>
    fun <A> listOfN(n: Int, p: Parser<A>): Parser<List<A>>

    fun <A> run(p: Parser<A>, input: String): Either<PE, A>
}

fun <A> Parser<A>.many(): Parser<List<A>> = TODO()
fun <A> Parser<A>.many1(): Parser<List<A>> = TODO()
infix fun <A, B> Parser<A>.product(pb: Parser<B>): Parser<Pair<A, B>> = TODO()

fun <A, B> Parser<A>.map(f: (A) -> B): Parser<B> = TODO()
fun <A> Parser<A>.slice(): Parser<String> = TODO()

data class ParseError(val msg: String)

object ParsersInterpreter : Parsers<ParseError> {
    override fun char(c: Char): Parsers.Parser<Char> {
        TODO("Not yet implemented")
    }
    override fun string(s: String): Parsers.Parser<String> {
        TODO("Not yet implemented")
    }
    override fun charZeroOrMore(c: Char): Parsers.Parser<Int> =
        TODO("Not yet implemented")

    override infix fun String.or(other: String): Parsers.Parser<String> {
        TODO("Not yet implemented")
    }
    override fun <A> or(
        pa: Parsers.Parser<A>,
        pb: Parsers.Parser<A>,
    ): Parsers.Parser<A> {
        TODO("Not yet implemented")
    }

    override fun <A> listOfN(
        n: Int,
        p: Parsers.Parser<A>,
    ): Parsers.Parser<List<A>> {
        TODO("Not yet implemented")
    }

    override fun <A> run(
        p: Parsers.Parser<A>,
        input: String,
    ): Either<ParseError, A> {
        TODO("Not yet implemented")
    }
}
