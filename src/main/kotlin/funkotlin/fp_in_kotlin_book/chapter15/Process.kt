package funkotlin.fp_in_kotlin_book.chapter15

import arrow.Kind
import arrow.Kind2
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter05.Cons
import funkotlin.fp_in_kotlin_book.chapter05.Empty
import funkotlin.fp_in_kotlin_book.chapter05.Stream

class ForProcess private constructor() { companion object }
typealias ProcessOf<I, O> = Kind2<ForProcess, I, O>
typealias ProcessPartialOf<I> = Kind<ForProcess, I>
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I, O> ProcessOf<I, O>.fix(): Process<I, O> = this as Process<I, O>

sealed class Process<I, O> : ProcessOf<I, O> {
    operator fun invoke(si: Stream<I>): Stream<O> =
        when (this) {
            is Emit -> Cons({ this.head }, { this.tail(si) })
            is Await -> when (si) {
                is Cons -> this.recv(Some(si.head()))(si.tail())
                Empty -> this.recv(None)(si)
            }

            is Halt -> Stream.empty()
        }

    fun repeat(): Process<I, O> {
        fun go(p: Process<I, O>): Process<I, O> {
            return when (p) {
                is Halt -> go(this)
                is Await -> Await { i: Option<I> ->
                    when (i) {
                        is None -> p.recv(None)
                        else -> go(p.recv(i))
                    }
                }
                is Emit -> Emit(p.head, go(p.tail))
            }
        }
        return go(this)
    }


    companion object {
    }
}

data class Emit<I, O>(
    val head: O,
    val tail: Process<I, O> = Halt()
) : Process<I, O>()

data class Await<I, O>(
    val recv: (Option<I>) -> Process<I, O>
) : Process<I, O>()

class Halt<I, O> : Process<I, O>()

fun <I, O> lift(f: (I) -> O): Process<I, O> =
    liftOne(f).repeat()

fun <I, O> liftOne(f: (I) -> O): Process<I, O> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> Emit(f(i.value))
            None -> Halt()
        }
    }

fun <I> filter(p: (I) -> Boolean): Process<I, I> =
    Await<I, I> { i: Option<I> ->
        when (i) {
            is Some -> if (p(i.value)) Emit(i.value) else Halt()
            None -> Halt()
        }
    }.repeat()

fun sum(): Process<Double, Double> {
    fun go(acc: Double): Process<Double, Double> =
        Await { i: Option<Double> ->
            when (i) {
                is Some -> Emit(i.value + acc, go(i.value + acc))
                None -> Halt()
            }
        }
    return go(0.0)
}
