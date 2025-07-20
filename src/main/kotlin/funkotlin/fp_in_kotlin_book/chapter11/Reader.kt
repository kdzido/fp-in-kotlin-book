package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import arrow.Kind2

class ForReader private constructor() { companion object }
typealias ReaderOf<R, A> = Kind2<ForReader, R, A>
typealias ReaderPartialOf<R> = Kind<ForReader, R>
fun <R, A> ReaderOf<R, A>.fix() = this as Reader<R, A>
interface ReaderMonad<R> : Monad<ReaderPartialOf<R>>

data class Reader<R, A>(val run2: (R) -> A) : ReaderOf<R, A> {
    companion object {
        fun <R, A> unit(a: A): Reader<R, A> = Reader { a }
    }

    fun <B> flatMap(f: (A) -> Reader<R, B>): Reader<R, B> = Reader { r: R -> f(run2(r)).run2(r) }

    fun <B> map(f: (A) -> B): Reader<R, B> = flatMap { a -> unit(f(a)) }

    fun <A> ask(): Reader<R, R> = Reader { r -> r }
}

fun <R> readerMonad() = object : ReaderMonad<R> {
    override fun <A> unit(a: A): ReaderOf<R, A> =
        Reader { a }

    override fun <A, B, C> compose(
        f: (A) -> ReaderOf<R, B>,
        g: (B) -> ReaderOf<R, C>,
    ): (A) -> ReaderOf<R, C> = { a ->
        f(a).fix().flatMap { b -> g(b).fix() }
    }
}
