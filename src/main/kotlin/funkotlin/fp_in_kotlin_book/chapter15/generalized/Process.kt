package funkotlin.fp_in_kotlin_book.chapter15.generalized

import arrow.Kind
import arrow.Kind2
import arrow.core.andThen
import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter11.Monad
import funkotlin.fp_in_kotlin_book.chapter13.io.ForIO
import funkotlin.fp_in_kotlin_book.chapter13.io.IO
import funkotlin.fp_in_kotlin_book.chapter13.io.IOOf
import funkotlin.fp_in_kotlin_book.chapter13.io.fix
import funkotlin.fp_in_kotlin_book.chapter15.Counting.FILE_10
import funkotlin.fp_in_kotlin_book.chapter15.generalized.Process.Companion.tryP
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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

// EXER 15.10
fun <F, O> Process<F, O>.runLog2(MC: MonadCatch<F>): Kind<F, Sequence<O>> {
    tailrec fun go(cur: Process<F, O>, acc: Sequence<O>): Kind<F, Sequence<O>> =
        when (cur) {
            is Process.Companion.Emit ->
                go(cur.tail, acc + cur.head)
            is Process.Companion.Halt ->
                when (val e = cur.err) {
                    is Process.Companion.End -> MC.unit(acc)
                    else -> throw e
                }
            is Process.Companion.Await<*, *, *> -> {
                val re: Kind<F, O> = cur.req as Kind<F, O>
                val rcv: (Either<Throwable, O>) -> Process<F, O> = cur.recv as (Either<Throwable, O>) -> Process<F, O>
                MC.flatMap(MC.attempt(re)) { ei ->
                    go(tryP { rcv(ei) }, acc)
                }
            }
        }

    return go(this, emptySequence())
}

interface MonadCatch<F> : Monad<F> {
    fun <A> attempt(a: Kind<F, A>): Kind<F, Either<Throwable, A>>
    fun <A> fail(t: Throwable): Kind<F, A>
}

fun <O> runLog(src: Process<ForIO, O>): IO<Sequence<O>> = IO {
    val E = Executors.newFixedThreadPool(4)

    tailrec fun go(cur: Process<ForIO, O>, acc: Sequence<O>): Sequence<O> =
        when (cur) {
            is Process.Companion.Emit ->
                go(cur.tail, acc + cur.head)
            is Process.Companion.Halt ->
                when (val e = cur.err) {
                    is Process.Companion.End -> acc
                    else -> throw e
                }
            is Process.Companion.Await<*, *, *> -> {
                val re = cur.req as IO<O>
                val rcv = cur.recv as (Either<Throwable, O>) -> Process<ForIO, O>
                val next: Process<ForIO, O> = try {
                    rcv(Right(unsafePerformIO(re, E))).fix()
                } catch (err: Throwable) {
                    rcv(Left(err))
                }
                go(next, acc)
            }
        }

    try {
        go(src, emptySequence())
    } finally {
        E.shutdown()
    }
}

private fun <A> unsafePerformIO(
    ioa: IOOf<A>,
    pool: ExecutorService
): A = ioa.fix().run()


fun <F, A, O> await(
    req: Kind<Any?, Any?>,
    recv: (Either<Throwable, Nothing>) -> Process<out Any?, out Any?>
): Process<F, O> =
    Process.Companion.Await(
        req as Kind<F, Nothing>,
        recv as (Either<Throwable, A>) -> Process<F, A>
    ).fix()

fun processNext(
    ei1: Right<BufferedReader>
): Process<ForIO, String> =
    await<ForIO, BufferedReader, String>(
        IO { ei1.value.readLine() }
    ) { ei2: Either<Throwable, String?> ->
        when (ei2) {
            is Right ->
                if (ei2.value == null) Process.Companion.Halt(Process.Companion.End) else
                    Process.Companion.Emit(ei2.value, processNext(ei1))
            is Left ->
                await<ForIO, Nothing, Nothing>(
                    IO { ei1.value.close() }
                ) { Process.Companion.Halt(ei2.value) }
        }
    }

fun main() {
    val p: Process<ForIO, String> =
        await<ForIO, BufferedReader, String>(
            IO { BufferedReader(FileReader(FILE_10)) }
        ) { ei1: Either<Throwable, BufferedReader> ->
            when (ei1) {
                is Right -> processNext(ei1)
                is Left -> Process.Companion.Halt(ei1.value)
            }
        }

    val seq = runLog(p).run()
    for (line in seq) {
        println("runLog: ${line}")
    }
}
