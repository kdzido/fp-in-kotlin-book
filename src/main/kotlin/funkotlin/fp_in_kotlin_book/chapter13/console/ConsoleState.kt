package funkotlin.fp_in_kotlin_book.chapter13.console

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter11.Monad

data class Buffers(
    val input: List<String>,
    val output: List<String>,
)

sealed class ForConsoleState { companion object }
typealias ConsoleStateOf<A> = Kind<ForConsoleState, A>
inline fun <A> ConsoleStateOf<A>.fix(): ConsoleState<A> = this as ConsoleState<A>

data class ConsoleState<A>(
    val run: (Buffers) -> Pair<A, Buffers>
) : ConsoleStateOf<A> {
    companion object {
        fun <A> unit(a: A) = ConsoleState({ b -> a to b })
    }

    fun <B> flatMap(f: (A) -> ConsoleState<B>): ConsoleState<B> = ConsoleState { b: Buffers ->
        val (a1, s2) = this.run(b)
        f(a1).run(s2)
    }

    fun <B> map(f: (A) -> B): ConsoleState<B> =
        this.flatMap { a -> unit(f(a)) }
}

fun ConsoleState.Companion.monad() = object : ConsoleStateMonad {}

interface ConsoleStateMonad : Monad<ForConsoleState> {
    override fun <A> unit(a: A): Kind<ForConsoleState, A> =
        ConsoleState.unit(a)

    override fun <A, B> flatMap(
        fa: ConsoleStateOf<A>,
        f: (A) -> ConsoleStateOf<B>,
    ): ConsoleStateOf<B> =
        fa.fix().flatMap { a -> f(a).fix() }

    override fun <A, B> map(
        fa: ConsoleStateOf<A>,
        f: (A) -> B,
    ): ConsoleStateOf<B> = fa.fix().map(f)
}



fun main() {

}
