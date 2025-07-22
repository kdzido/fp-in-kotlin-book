package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import arrow.core.curry
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter11.Functor

interface Applicative<F> : Functor<F> {
    fun <A, B> apply(
        fab: Kind<F, (A) -> B>,
        fa: Kind<F, A>,
    ): Kind<F, B> =
        map2(fa, fab) { a: A, f: (A) -> B -> f(a) }

    fun <A> unit(a: A): Kind<F, A>

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        apply(unit(f), fa)
//        map2(fa, unit(Unit)) { a, _ -> f(a) }

    fun <A, B, C> map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C
    ): Kind<F, C> {
        val fCurried: (A) -> (B) -> C = { a -> { b -> f(a, b) } }
        val l1: Kind<F, (B) -> C> = apply(unit(fCurried), fa)
        return apply(l1, fb)
    }

    fun <A, B, C, D> map3(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        fc: Kind<F, C>,
        f: (A, B, C) -> D,
    ): Kind<F, D> {
        val fCurried: (A) -> (B) -> (C) -> D = { a -> { b -> { c -> f(a, b, c) } } }
        val l1: Kind<F, (B) -> (C) -> D> = apply(unit(fCurried), fa)
        val l2: Kind<F, (C) -> D> = apply(l1, fb)
        return apply(l2, fc)
    }

    fun <A, B, C, D, E> map4(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        fc: Kind<F, C>,
        fd: Kind<F, D>,
        f: (A, B, C, D) -> E,
    ): Kind<F, E> {
        val fCurried: (A) -> (B) -> (C) -> (D) -> E = { a -> { b -> { c -> { d -> f(a, b, c, d) } } } }
        val l1: Kind<F, (B) -> (C) -> (D) -> E> = apply(unit(fCurried), fa)
        val l2: Kind<F, (C) -> (D) -> E> = apply(l1, fb)
        val l3: Kind<F, (D) -> E> = apply(l2, fc)
        return apply(l3, fd)
    }

    fun <A, B> traverse(
        la: ListL<A>,
        f: (A) -> Kind<F, B>
    ): Kind<F, ListL<B>> =
        ListL.foldRight2(
            la,
            unit(ListL.of<B>()),
            { a: A, acc: Kind<F, ListL<B>> ->
                map2(f(a), acc) { b: B, lb: ListL<B> -> Cons(b, lb) }
            }
        )

    fun <A> sequence(lfa: ListL<Kind<F, A>>): Kind<F, ListL<A>> =
        ListL.foldRight2(
            lfa,
            unit(ListL.of()),
            { fa: Kind<F, A>, fla: Kind<F, ListL<A>> ->
                map2(fa, fla) { a: A, la: ListL<A> -> Cons(a, la) }
            }
        )

    fun <A> replicateM(n: Int, ma: Kind<F, A>): Kind<F, ListL<A>> =
        sequence(ListL.fill(n, ma))

    fun <A, B> product(
        ma: Kind<F, A>,
        mb: Kind<F, B>
    ): Kind<F, Pair<A, B>> =
        map2(ma, mb) { a, b -> Pair(a, b) }

}
