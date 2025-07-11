package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.ForList
import funkotlin.fp_in_kotlin_book.chapter03.fix

interface Functor<F> {
    fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B>

    fun <A, B> distribute(
        fab: Kind<F, Pair<A, B>>
    ): Pair<Kind<F, A>, Kind<F, B>>

}

val listFunctor = object : Functor<ForList> {
    override fun <A, B> map(
        fa: Kind<ForList, A>,
        f: (A) -> B,
    ): Kind<ForList, B> =
        List.map(fa.fix(), f)

    override fun <A, B> distribute(
        fab: Kind<ForList, Pair<A, B>>
    ): Pair<Kind<ForList, A>, Kind<ForList, B>> =
        map(fab) { it.first} to map(fab) { it.second }
}

