package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter11.Functor
import funkotlin.fp_in_kotlin_book.chapter11.fix
import funkotlin.fp_in_kotlin_book.chapter11.idApplicative

interface Traversable<F> : Functor<F> {
    override fun <A, B> map(
        fa: Kind<F, A>,
        f: (A) -> B
    ): Kind<F, B> {
        val IA = idApplicative()
        return traverse(fa, IA) { a: A ->
            IA.unit<B>(f(a))
        }.fix().a
    }

    fun <G, A, B> traverse(
        fa: Kind<F, A>,
        AG: Applicative<G>,
        f: (A) -> Kind<G, B>
    ): Kind<G, Kind<F, B>> =
        sequence(map(fa, f), AG)

    fun <G, A> sequence(
        fga: Kind<F, Kind<G, A>>,
        AG: Applicative<G>
    ): Kind<G, Kind<F, A>> =
        traverse(fga, AG) { it }
}
