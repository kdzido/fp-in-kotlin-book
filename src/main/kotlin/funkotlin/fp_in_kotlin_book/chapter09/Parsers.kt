package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter04.Either

interface Parsers<PE> {
    interface Parser<T>

    fun char(c: Char): Parser<Char>

    fun <A> run(p: Parser<A>, input: String): Either<PE, A>
}
