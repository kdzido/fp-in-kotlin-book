package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.Prop
import funkotlin.fp_in_kotlin_book.chapter08.Prop.Companion.forAll

object ParserError

abstract class Laws : Parsers<ParseError> {
    private fun <A> equal(
        p1: Parser<A>,
        p2: Parser<A>,
        i: Gen<String>,
    ): Prop =
        forAll(i) { s -> run(p1, s) == run(p2, s) }

    fun <A> mapLaw(p: Parser<A>, i: Gen<String>): Prop =
        equal(p, p.map { a -> a }, i)


    fun <A> succeedLaw(i: Gen<String>): Prop =
        forAll(i) { a -> run(succeed(a), a) == Right(a) }
}
