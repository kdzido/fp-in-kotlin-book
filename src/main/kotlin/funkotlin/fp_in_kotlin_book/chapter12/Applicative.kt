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
}
