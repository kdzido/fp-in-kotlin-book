package funkotlin.fp_in_kotlin_book.chapter13.console

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter11.Monad

sealed class ForConsoleReader { companion object }
typealias ConsoleReaderOf<A> = Kind<ForConsoleReader, A>
inline fun <A> ConsoleReaderOf<A>.fix(): ConsoleReader<A> = this as ConsoleReader<A>

data class ConsoleReader<A>(val run: (String) -> A): ConsoleReaderOf<A> {
    companion object {}

    fun <B> flatMap(f: (A) -> ConsoleReader<B>): ConsoleReader<B> =
        ConsoleReader { r -> f(run(r)).run(r) } // <.>

    fun <B> map(f: (A) -> B): ConsoleReader<B> =
        ConsoleReader { r -> f(run(r)) } // <.>
}

fun ConsoleReader.Companion.monad() = object : ConsoleReaderMonad {}

//@extension
interface ConsoleReaderMonad : Monad<ForConsoleReader> {
    override fun <A> unit(a: A): Kind<ForConsoleReader, A> =
        ConsoleReader { a }

    override fun <A, B> flatMap(
        fa: ConsoleReaderOf<A>,
        f: (A) -> ConsoleReaderOf<B>,
    ): ConsoleReaderOf<B> =
        fa.fix().flatMap { a -> f(a).fix() }

    override fun <A, B> map(
        fa: ConsoleReaderOf<A>,
        f: (A) -> B,
    ): ConsoleReaderOf<B> = fa.fix().map(f)
}
