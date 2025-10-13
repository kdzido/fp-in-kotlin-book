package funkotlin.fp_in_kotlin_book.chapter15.generalized

import arrow.Kind
import arrow.Kind2
import arrow.core.andThen
import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monad
import funkotlin.fp_in_kotlin_book.chapter13.io.ForIO
import funkotlin.fp_in_kotlin_book.chapter13.io.IO
import funkotlin.fp_in_kotlin_book.chapter13.io.IOOf
import funkotlin.fp_in_kotlin_book.chapter13.io.fix
import funkotlin.fp_in_kotlin_book.chapter15.Counting.FILE_10
import funkotlin.fp_in_kotlin_book.chapter15.generalized.Process.Companion.awaitAndThen
import funkotlin.fp_in_kotlin_book.chapter15.generalized.Process.Companion.tryP
import funkotlin.fp_in_kotlin_book.chapter15.generalized.drain
import funkotlin.fp_in_kotlin_book.chapter15.generalized.fix
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ForIs {
    companion object
}
typealias IsOf<I> = Kind<ForIs, I>
inline fun <I> IsOf<I>.fix(): Is<I> = this as Is<I>
inline fun <I> IsOf<I>.fix1(): Is<I> = this as Is<I>
class Is<I> : IsOf<I>

typealias Process1<I, O> = Process<ForIs, O>

class ForProcess private constructor() { companion object }
typealias ProcessOf<F, O> = Kind2<ForProcess, F, O>
typealias ProcessPartialOf<I> = Kind<ForProcess, I>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I, O> ProcessOf<I, O>.fix(): Process<I, O> = this as Process<I, O>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I, O> ProcessOf<I, O>.fix1(): Process1<I, O> =
    this as Process1<I, O>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I, O> Process1<I, O>.fix1(): Process<I, O> =
    this as Process<I, O>


sealed class Process<F, O> : ProcessOf<F, O> {
    companion object {
        data class Await<F, A, O>(
            val req: Kind<F, A>,
            val recv: (Either<Throwable, A>) -> Process<F, O>
        ) : Process<F, O>()

        data class Emit<F, O>(
            val head: O,
            val tail: Process<F, O> = Halt(End)
        ) : Process<F, O>()

        data class Halt<F, O>(val err: Throwable) : Process<F, O>()

        /** Normal termination due to exhausted input */
        object End : Exception()

        /** Forcible termination or error */
        object Kill : Exception()

        fun <F, I1, I2, O> tee(
            p1: Process<F, I1>,
            p2: Process<F, I2>,
            t: Tee<I1, I2, O>,
        ): Process<F, O> = when (t) {
            is Halt ->
                p1.kill<O>()
                    .onComplete { p2.kill() }
                    .onComplete { Halt(t.err) }

            is Emit ->
                Emit(t.head, tee(p1, p2, t.tail))

            is Await<*, *, *> -> {
                val side = t.req as T<I1, I2, O>
                val rcv = t.recv as (Either<Nothing, Any?>) -> Tee<I1, I2, O>

                when (side.get()) {
                    is Left ->
                        when (p1) {
                            is Halt -> p2.kill<O>().onComplete { Halt(p1.err) }
                            is Emit -> tee(p1.tail, p2, tryP { rcv(Right(p1.head)) })
                            is Await<*, *, *> -> awaitAndThen<F, I2, O>(p1.req, p1.recv) {
                                tee(it, p2, t)
                            }
                        }

                    is Right ->
                        when (p2) {
                            is Halt -> p1.kill<O>().onComplete { Halt(p2.err) }
                            is Emit -> tee(p1, p2.tail, tryP { rcv(Right(p2.head)) })
                            is Await<*, *, *> -> awaitAndThen<F, I2, O>(p2.req, p2.recv) {
                                tee(p1, it, t)
                            }
                        }
                }
            }
        }

        fun <I, O> await1(
            recv: (I) -> Process1<ForIs, O>,
            fallback: Process1<ForIs, O> = halt1<ForIs, O>()
        ): Process1<I, O> =
            Await(Is<I>()) { ei: Either<Throwable, I> ->
                when (ei) {
                    is Left -> when (val err = ei.value) {
                        is End -> fallback
                        else -> Halt(err)
                    }
                    is Right -> tryP { recv(ei.value) }
                }
            }

        fun <I, O> halt1(): Process1<ForIs, O> =
            Halt<ForIs, O>(End).fix1()

        fun <I, O> emit1(
            head: O,
            tail: Process1<ForIs, O> = halt1<ForIs, O>()
        ): Process<ForIs, O> =
            Emit<ForIs, O>(
                head,
                tail.fix1()
            ).fix1()

        fun <I, O> lift(f: (I) -> O): Process1<ForIs, O> =
            await1({ i: I ->
                Emit<I, O>(f(i)).fix1()
            }).repeat()

        fun <I> filter(f: (I) -> Boolean): Process1<ForIs, I> =
            await1<I, I>({ i ->
                if (f(i)) Emit<ForIs, I>(i).fix1()
                else halt1<ForIs, I>()
            }).repeat()

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

    infix fun <O2> pipe(p2: Process1<O, O2>): Process<F, O2> =
        when (p2) {
            is Halt -> this.kill<O2>().onHalt { e2 -> Halt<F, O2>(p2.err).append { Halt(e2) } }
            is Emit -> Emit(p2.head, this.pipe(p2.tail.fix1()))
            is Await<*, *, *> -> {
                val rcv = p2.recv as (Either<Throwable, O>) -> Process<F, O2>
                when (this) {
                    is Halt -> Halt<F, O2>(this.err) pipe rcv(Left(this.err)).fix1()
                    is Emit -> tail.pipe(tryP { rcv(Right(head).fix()) }.fix1())
                    is Await<*, *, *> ->
                        awaitAndThen<F, O, O2>(req, recv) { it pipe p2 }
                }
            }
        }

    fun filter(f: (O) -> Boolean): Process<F, O> =
        this pipe Process.filter(f)

    fun <O2> drain(): Process<F, O2> =
        when (this) {
            is Halt -> Halt(this.err)
            is Emit -> tail.drain()
            is Await<*, *, *> ->
                awaitAndThen<F, O2, O2>(req, recv) { it.drain() }
        }

    fun <O2> kill(): Process<F, O2> =
        when (this) {
            is Await<*, *, *> -> {
                val rcv = this.recv as (Either<Throwable, O>) -> Process<F, O2>
                rcv(Left(Kill)).drain<O2>()
                    .onHalt { e ->
                        when (e) {
                            is Kill -> Halt(End)
                            else -> Halt(e)
                        }
                    }
            }
            is Emit -> tail.kill()
            is Halt -> Halt(this.err)
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

    fun repeat(): Process<F, O> = this.append { this.repeat() }

    fun onComplete(p: () -> Process<F, O>): Process<F, O> =
        this.onHalt { e: Throwable ->
            when (e) {
                is End -> p().asFinalizer()
                else -> p().asFinalizer().append { Halt(e) }
            }
        }

    private fun asFinalizer(): Process<F, O> =
        when (this) {
            is Halt -> Halt(this.err)
            is Emit -> Emit(this.head, this.tail.asFinalizer())
            is Await<*, *, *> -> {
                await<F, O, O>(this.req) { ei: Either<Throwable, Nothing> ->
                    when (ei) {
                        is Left ->
                            when (val e = ei.value) {
                                is Kill -> this.asFinalizer()
                                else -> this.recv(Left(e))
                            }
                        is Right -> this.recv(ei)
                    }
                }
            }
        }
}

fun lines(fileName: String): Process<ForIO, String> =
    resource(
        IO { BufferedReader(FileReader(fileName)) },
        { br: BufferedReader ->

            val iter = br.lines().iterator()

            fun step() = if (iter.hasNext()) Some(iter.next()) else None

            fun lns(): Process<ForIO, String> {
                return eval(IO { step() }).flatMap { ln: Option<String> ->
                    when (ln) {
                        is Some -> Process.Companion.Emit(ln.value, lns())
                        is None -> Process.Companion.Halt<ForIO, String>(Process.Companion.End)
                    }
                }
            }

            lns()
        },
        { br: BufferedReader -> evalDrain(IO { br.close() }) }
    )

fun <R, O> resource(
    acquire: IO<R>,
    use: (R) -> Process<ForIO, O>,
    release: (R) -> Process<ForIO, O>
): Process<ForIO, O> =
    eval(acquire)
        .flatMap { use(it).onComplete { release(it) } }

fun <F, A> eval(fa: Kind<F, A>): Process<F, A> =
    await<F, A, A>(fa) { ea: Either<Throwable, Nothing> ->
        when (ea) {
            is Right<A> ->
                Process.Companion.Emit(ea.value, Process.Companion.Halt(Process.Companion.End))
            is Left ->
                Process.Companion.Halt(ea.value)
        }
    }

fun <F, A, B> evalDrain(fa: Kind<F, A>): Process<F, B> =
    eval(fa).drain()

fun <F, A, B> Process<F, A>.drain(): Process<F, B> =
    when (this) {
        is Process.Companion.Halt -> Process.Companion.Halt(this.err)
        is Process.Companion.Emit -> this.tail.drain()
        is Process.Companion.Await<*, *, *> ->
            awaitAndThen<F, A, B>(
                this.req,
                { ei: Either<Throwable, Nothing> -> this.recv(ei) },
                { it.drain() }
            )
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
        recv as (Either<Throwable, A>) -> Process<F, O>
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

fun main0() {
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

fun main() {
    val p: Process<ForIO, String> = lines(FILE_10)

    val seq: Sequence<String> = runLog(p).run()
    for (line in seq) {
        println("lines: ${line}")
    }
}
