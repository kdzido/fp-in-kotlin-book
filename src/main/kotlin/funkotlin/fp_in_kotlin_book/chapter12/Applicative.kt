package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter11.Functor

interface Applicative<F> : Functor<F> {
    fun <A, B, C> map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C
    ): Kind<F, C>

    fun <A> unit(a: A): Kind<F, A>

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        map2(fa, unit(Unit)) { a, _ -> f(a) }

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
