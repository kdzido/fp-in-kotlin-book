package funkotlin.fp_in_kotlin_book.chapter15.generalized

import arrow.Kind
import arrow.Kind2
import arrow.Kind3
import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter15.generalized.Process.Companion.tryP

typealias Tee<I1, I2, O> = Process<ForT, O>

fun <I1, I2, O> awaitL(
    fallback: Tee<I1, I2, O> = haltT<I1, I2, O>(),
    recv: (I1) -> Tee<I1, I2, O>
): Tee<I1, I2, O> =
    await<ForT, I1, O>(
        T.left<I1, I2>()
    ) { e: Either<Throwable, I1> ->
        when (e) {
            is Left -> when (val err = e.value) {
                is Process.Companion.End -> fallback
                else -> Process.Companion.Halt(err)
            }
            is Right -> tryP { recv(e.value) }
        }
    }

fun <I1, I2, O> awaitR(
    fallback: Tee<I1, I2, O> = haltT<I1, I2, O>(),
    recv: (I2) -> Tee<I1, I2, O>
): Tee<I1, I2, O> =
    await<ForT, I1, O>(
        T.right<I1, I2>()
    ) { e: Either<Throwable, I2> ->
        when (e) {
            is Left -> when (val err = e.value) {
                is Process.Companion.End -> fallback
                else -> Process.Companion.Halt(err)
            }
            is Right -> tryP { recv(e.value) }
        }
    }

fun <I1, I2, O> emitT(
    h: O,
    tl: Tee<I1, I2, O> = haltT<I1, I2, O>(),
): Tee<I1, I2, O> =
    Process.Companion.Emit(h, tl)

fun <I1, I2, O> haltT(): Tee<I1, I2, O> =
    Process.Companion.Halt(Process.Companion.End)


class ForT private constructor() {
    companion object
}

typealias TOf<I1, I2, X> = Kind3<ForT, I1, I2, X>
typealias TPartialOf<I1, I2> = Kind2<ForT, I1, I2>

inline fun <I1, I2, X> TOf<I1, I2, X>.fix(): T<I1, I2, X> = this as T<I1, I2, X>

sealed class T<I1, I2, X> : TOf<I1, I2, X> {
    companion object {
        fun <I1, I2> left() = L<I1, I2>()
        fun <I1, I2> right() = R<I1, I2>()
    }

    abstract fun get(): Either<(I1) -> X, (I2) -> X>

    class L<I1, I2> : T<I1, I2, I1>() {
        override fun get(): Either<(I1) -> I1, (I2) -> I1> = Left { l: I1 -> l }
    }

    class R<I1, I2> : T<I1, I2, I2>() {
        override fun get(): Either<(I1) -> I2, (I2) -> I2> = Right { r: I2 -> r }
    }
}

fun <I1, I2, O> zipWith(f: (I1, I2) -> O): Tee<I1, I2, O> =
    awaitL<I1, I2, O> { i1: I1 ->
        awaitR<I1, I2, O> { i2: I2 ->
            emitT<I1, I2, O>(f(i1, i2))
        }
    }.repeat()

fun <I1, I2> zip(): Tee<I1, I2, Pair<I1, I2>> =
    zipWith { i1: I1, i2: I2 -> i1 to i2 }

fun main() {
    println("test")
}
