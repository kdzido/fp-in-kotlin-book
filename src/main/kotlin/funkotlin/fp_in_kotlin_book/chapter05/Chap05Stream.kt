package funkotlin.fp_in_kotlin_book.chapter05

import funkotlin.fp_in_kotlin_book.chapter03.Nil as Nil3
import funkotlin.fp_in_kotlin_book.chapter03.Cons as Cons3
import funkotlin.fp_in_kotlin_book.chapter03.List as List3
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.Option

sealed class Stream<out A> {
    companion object {
        fun <A> empty() = Empty
        fun <A> cons(hd: () -> A, tl: () -> Stream<A>): Stream<A> {
            val head: A by lazy(hd)
            val tail: Stream<A> by lazy(tl)
            return Cons({ head }, { tail })
        }

        fun <A> of(vararg xs: A): Stream<A> =
            if (xs.isEmpty()) empty<A>()
            else cons({ xs[0] }, { of(*xs.sliceArray(1 until xs.size)) })

        // EXER 5.1
        fun <A> Stream<A>.toList(): List3<A> {
            tailrec fun go(left: Stream<A>, acc: List3<A>): List3<A> {
                return when (left) {
                    is Empty -> acc
                    is Cons -> go(left.tail(), Cons3(left.head(), acc))
                }
            }
            tailrec fun reverse(left: List3<A>, acc: List3<A>): List3<A> {
                return when (left) {
                    is Nil3 -> acc
                    is Cons3 -> reverse(left.tail, Cons3(left.head, acc))
                }
            }
            val rev = go(this, Nil3)
            return reverse(rev, Nil3)
        }

        // Exer 5.2
        fun <A> Stream<A>.take(n: Int): Stream<A> {
            fun go(rem: Stream<A>, j: Int): Stream<A> =
                if (j <= 0) Empty else
                    when (rem) {
                        is Empty -> Empty
                        is Cons -> Stream.cons(rem.head, { go(rem.tail(), j - 1) })
                    }
            return go(this, n)
        }

        // Exer 5.3
        fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> {
            fun go(rem: Stream<A>): Stream<A> =
                when (rem) {
                    is Empty -> Empty
                    is Cons -> {
                        if (p(rem.head()))
                            Stream.cons(rem.head, { go(rem.tail()) })
                        else Empty
                    }
                }
            return go(this)
        }


            // Exer 5.2
        fun <A> Stream<A>.drop(n: Int): Stream<A> {
            tailrec fun go(rem: Stream<A>, j: Int): Stream<A> =
                if (j <= 0) rem
                else when (rem) {
                    is Empty -> Empty
                    is Cons -> go(rem.tail(), j - 1)
                }
            return go(this, n)
        }
    }
}

object Empty : Stream<Nothing>()
data class Cons<out A>(
    val head: () -> A,
    val tail: () -> Stream<A>,
) : Stream<A>()


fun <A> Stream<A>.headOption(): Option<A> =
    when (this) {
        is Empty -> None
        is Cons -> Some(head())
    }

fun main() {
    val s = Empty
}
