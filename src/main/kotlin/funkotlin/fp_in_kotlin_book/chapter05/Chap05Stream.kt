package funkotlin.fp_in_kotlin_book.chapter05

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Nil as NilL
import funkotlin.fp_in_kotlin_book.chapter03.Cons as ConsL
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.map

class ForStream private constructor() { companion object }
typealias StreamOf<T> = Kind<ForStream, T>
fun <A> StreamOf<A>.fix() = this as Stream<A>

sealed class Stream<out A> : StreamOf<A> {
    companion object {
        fun ones(): Stream<Int> = constant(1)

        // EXER 5.8
        fun <A> constant(a: A): Stream<A> = Stream.cons({ a }, { constant(a) })

        // EXER 5.9
        fun from(n: Int): Stream<Int> = Stream.cons({ n }, { from(n + 1) })

        // EXER 5.10
        fun fibs(): Stream<Int> {
            fun go(n1: Int, n2: Int): Stream<Int> {
               return cons({ n1 }, { go(n2, n1 + n2) })
            }
            return go(0, 1)
        }

        // EXER 5.11
        fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> {
            fun go(s1: S): Stream<A> {
                val vO = f(s1);
                return when (vO) {
                    is Some -> cons({vO.value.first}, {go(vO.value.second)})
                    is None -> Empty
                }
            }
            return go(z)
        }

        fun <A> empty() = Empty as Stream<A>
        fun <A> cons(hd: () -> A, tl: () -> Stream<A>): Stream<A> {
            val head: A by lazy(hd)
            val tail: Stream<A> by lazy(tl)
            return Cons({ head }, { tail })
        }

        fun <A> of(vararg xs: A): Stream<A> =
            if (xs.isEmpty()) empty<A>()
            else cons({ xs[0] }, { of(*xs.sliceArray(1 until xs.size)) })

        // EXER 5.1
        fun <A> Stream<A>.toList(): ListL<A> {
            tailrec fun go(left: Stream<A>, acc: ListL<A>): ListL<A> {
                return when (left) {
                    is Empty -> acc
                    is Cons -> go(left.tail(), ConsL(left.head(), acc))
                }
            }
            tailrec fun reverse(left: ListL<A>, acc: ListL<A>): ListL<A> {
                return when (left) {
                    is NilL -> acc
                    is ConsL -> reverse(left.tail, ConsL(left.head, acc))
                }
            }
            val rev = go(this, NilL)
            return reverse(rev, NilL)
        }

        // Exer 5.2
        fun <A> Stream<A>.take(n: Int): Stream<A> {
            fun go(rem: Stream<A>, j: Int): Stream<A> =
                if (j <= 0) Empty else
                    when (rem) {
                        is Empty -> Empty
                        is Cons -> cons(rem.head, { go(rem.tail(), j - 1) })
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
                            cons(rem.head, { go(rem.tail()) })
                        else Empty
                    }
                }
            return go(this)
        }

        // EXER 5.5
        fun <A> Stream<A>.takeWhile2(p: (A) -> Boolean): Stream<A> {
            return foldRight({ empty() }, { a, b -> if (p(a)) cons({a},  b) else Empty })
        }

        // EXER 5.6
        fun <A> Stream<A>.headOption2(): Option<A> =
            this.foldRight({None as Option<A>}, {a, b -> Some(a)})

        // EXER 5.7
        fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
            this.foldRight({empty<B>()}, {a, b -> cons<B>({f(a)}, b)})

        // EXER 5.13
        fun <A, B> Stream<A>.map2(f: (A) -> B): Stream<B> = unfold(this, { s ->
            when (s) {
                is Cons -> Some(Pair(f(s.head()), s.tail()))
                is Empty -> None
            }
        })

        // EXER 5.13
        fun <A> Stream<A>.take2(n: Int): Stream<A> = unfold(Pair(n, this), { s ->
            val (leftN, stream) = s
            if (leftN <= 0) None
            else {
                when (stream) {
                    is Cons -> Some(Pair(stream.head(), Pair(leftN - 1, stream.tail())))
                    is Empty -> None
                }
            }
        })

        // EXER 5.13
        fun <A> Stream<A>.takeWhile_3(p: (A) -> Boolean): Stream<A> = unfold(this, { s ->
            when (s) {
                is Cons -> if (p(s.head())) Some(Pair(s.head(), s.tail())) else None
                is Empty -> None
            }
        })

        // EXER 5.13
        fun <A, B, C> Stream<A>.zipWith(that: Stream<B>, f: (A, B) -> C): Stream<C> = unfold(Pair(this, that), {
            s ->
            when {
                s.first is Empty && s.second is Empty -> None
                s.first is Empty && s.second is Cons -> None
                s.first is Cons && s.second is Empty -> None
                s.first is Cons && s.second is Cons -> Some(Pair(f((s.first as Cons).head(), (s.second as Cons).head()), Pair((s.first as Cons).tail(), (s.second as Cons).tail())))
                else -> TODO()
            }
        })

        // EXER 5.13
        fun <A, B> Stream<A>.zipAll(that: Stream<B>): Stream<Pair<Option<A>, Option<B>>> = unfold(Pair(this, that), {
            s ->
            when {
                s.first is Empty && s.second is Empty -> None
                s.first is Empty && s.second is Cons -> Some(Pair(Pair((s.first as Empty).headOption2(), (s.second as Cons).headOption2()), Pair(Empty, (s.second as Cons).tail())))
                s.first is Cons && s.second is Empty -> Some(Pair(Pair((s.first as Cons).headOption2(), (s.second as Empty).headOption2()), Pair((s.first as Cons).tail(), Empty)))
                s.first is Cons && s.second is Cons -> Some(Pair(Pair((s.first as Cons).headOption2(), (s.second as Cons).headOption2()), Pair((s.first as Cons).tail(), (s.second as Cons).tail())))
                else -> TODO()
            }
        })

        // EXER 5.14
        fun <A> Stream<A>.startsWith(that: Stream<A>): Boolean =
            when {
                this is Empty && that is Empty -> true
                this is Empty && that is Cons -> false
                this is Cons && that is Empty -> true
                this is Cons && that is Cons -> if (this.head() != that.head()) false else this.tail().startsWith(that.tail())
                else -> TODO()
            }

        // EXER 5.15
        fun <A> Stream<A>.tails(): Stream<Stream<A>> = unfold(Pair(this, true), {s ->
            val (stream, hasMore) = s
            when {
                stream is Empty && hasMore == false -> None
                stream is Empty && hasMore == true -> Some(Pair(empty(), Pair(empty(), false)))
                stream is Cons && hasMore == true -> Some(Pair(cons(stream.head, stream.tail), Pair(stream.tail(), true)))
                else -> TODO()
            }
        })

        // EXER 5.15
        fun <A, B> Stream<A>.scanRight(z: () -> B, f: (A, () -> B) -> B): Stream<B> =
            this.tails().map2 { a ->
                a.foldRight(z, { a, b -> f(a, b) })
            }

        fun <A> Stream<A>.hasSubsequence(s: Stream<A>): Boolean =
            this.tails().exists2 { it.startsWith(s) }

        // EXER 5.7
        fun <A, B> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B> =
            this.foldRight({empty()}, { a, b -> append(f(a), b())})

        // EXER 5.7
        fun <A> Stream<A>.filter(p: (A) -> Boolean): Stream<A> =
            this.foldRight({empty()}, {a, b -> if (p(a)) cons({a}, b) else b()})

        // EXER 5.7
        fun <A> append(s1: Stream<A>, s2: Stream<A>): Stream<A> =
            when (s1) {
                is Empty -> s2
                is Cons -> s1.foldRight({ s2 }, { a, b -> cons({ a }, b) })
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

        fun <A> Stream<A>.exists(p: (A) -> Boolean): Boolean =
            when (this) {
                is Cons -> p(this.head()) || this.tail().exists(p)
                else -> false
            }

        fun <A> Stream<A>.exists2(p: (A) -> Boolean): Boolean =
            foldRight({ false }, { a, b -> p(a) || b() })

        // EXER 5.4
        fun <A> Stream<A>.forAll(p: (A) -> Boolean): Boolean {
            tailrec fun go(left: Stream<A>, acc: () -> Boolean): Boolean {  // infinite loop
                return when (left) {
                    is Cons -> go(if (p(left.head())) left.tail() else Empty, { acc() && p(left.head()) })
                    is Empty -> acc()
                }
            }
            return go(this, { true })
//            return when (this) {  // stack overflow
//                is Cons -> p(this.head()) && this.tail().forAll(p)
//                else -> true
//            }
        }

        fun <A, B> Stream<A>.foldRight(
            z: () -> B,
            f: (A, () -> B) -> B
        ): B = when (this) {
            is Cons -> f(this.head()) {
                this.tail().foldRight(z, f)
            }
            is Empty -> z()
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

