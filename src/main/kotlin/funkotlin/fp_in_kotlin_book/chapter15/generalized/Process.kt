package funkotlin.fp_in_kotlin_book.chapter15.generalized

import arrow.Kind
import arrow.Kind2
import arrow.core.andThen
import funkotlin.fp_in_kotlin_book.chapter04.Either

class ForProcess private constructor() { companion object }
typealias ProcessOf<F, O> = Kind2<ForProcess, F, O>
typealias ProcessPartialOf<I> = Kind<ForProcess, I>
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I, O> ProcessOf<I, O>.fix(): Process<I, O> = this as Process<I, O>

sealed class Process<F, O> : ProcessOf<F, O> {
    companion object {
        data class Await<F, A, O>(
            val req: Kind<F, A>,
            val recv: (Either<Throwable, A>) -> Process<F, O>
        ) : Process<F, A>()

        data class Emit<F, O>(
            val head: O,
            val tail: Process<F, O>
        ) : Process<F, O>()

        data class Halt<F, O>(val err: Throwable) : Process<F, O>()

        /** Normal termination due to exhausted input */
        object End : Exception()

        /** Forcible termination or error */
        object Kill : Exception()

        fun <F, O> tryP(p: () -> Process<F, O>): Process<F, O> =
            try {
                p()
            } catch (e: Throwable) {
                Halt(e)
            }

        fun <F, A, O> awaitAndThen(
            req: Kind<Any?, Any?>,
            recv: (Either<Throwable, Nothing>) -> Process<out Any?, out Any?>,
            fn: (Process<F, A>) -> Process<F, O>
        ): Process<F, O> =
            Await(
                req as Kind<F, Nothing>,
                recv as (Either<Throwable, A>) -> Process<F, A> andThen fn
            ).fix()
    }

    fun <O2> flatMap(f: (O) -> Process<F, O2>): Process<F, O2> =
        when (this) {
            is Halt -> Halt(err)
            is Emit -> tryP { f(head)}.append { tail.flatMap(f) }
            is Await<*, *, *> ->
                awaitAndThen(req, recv) { p: Process<F, O> ->
                    p.flatMap(f)
                }
        }

    fun <O2> map(f: (O) -> O2): Process<F, O2> =
        when (this) {
            is Halt -> Halt(err)
            is Emit -> tryP { Emit(f(head), tail.map(f)) }
            is Await<*, *, *> ->
                awaitAndThen(req, recv) { p: Process<F, O> ->
                    p.map(f)
                }
        }

    fun append(p: () -> Process<F, O>): Process<F, O> =
        this.onHalt { ex: Throwable ->
            when (ex) {
                is End -> p()
                else -> Halt(ex)
            }
        }

    fun onHalt(f: (Throwable) -> Process<F, O>): Process<F, O> =
        when (this) {
            is Halt -> tryP { f(this.err) }
            is Emit -> Emit(this.head, tail.onHalt(f))
            is Await<*, *, *> ->
                awaitAndThen(req, recv) { p: Process<F, O> ->
                    p.onHalt(f)
                }
        }
}
