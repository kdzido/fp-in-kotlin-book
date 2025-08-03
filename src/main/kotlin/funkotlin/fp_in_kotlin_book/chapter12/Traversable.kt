package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter03.reversed
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter06.State
import funkotlin.fp_in_kotlin_book.chapter06.StateOf
import funkotlin.fp_in_kotlin_book.chapter06.fix
import funkotlin.fp_in_kotlin_book.chapter10.Foldable
import funkotlin.fp_in_kotlin_book.chapter10.Monoid
import funkotlin.fp_in_kotlin_book.chapter11.Functor
import funkotlin.fp_in_kotlin_book.chapter11.Monads.stateMonad
import funkotlin.fp_in_kotlin_book.chapter11.fix
import funkotlin.fp_in_kotlin_book.chapter11.idApplicative

interface Traversable<F> : Functor<F>, Foldable<F> {
    override fun <A, B> map(
        fa: Kind<F, A>,
        f: (A) -> B
    ): Kind<F, B> {
        val IA = idApplicative()
        return traverse(fa, IA) { a: A ->
            IA.unit<B>(f(a))
        }.fix().a
    }

    override fun <A, M> foldMap(
        fa: Kind<F, A>,
        m: Monoid<M>,
        f: (A) -> M,
    ): M =
        traverse(fa, monoidApplicative(m)) { a ->
            Const<M, A>(f(a))
        }.fix().value

    fun <G, A, B> traverse(
        fa: Kind<F, A>,
        AG: Applicative<G>,
        f: (A) -> Kind<G, B>
    ): Kind<G, Kind<F, B>> =
        sequence(map(fa, f), AG)

    fun <S, A, B> traverseS(
        fa: Kind<F, A>,
        f: (A) -> StateOf<S, B>
    ): State<S, Kind<F, B>> =
        traverse(
            fa = fa,
            AG = stateMonadApplicative(stateMonad<S>()),
        ) { a: A -> f(a).fix() }.fix()

    fun <G, A> sequence(
        fga: Kind<F, Kind<G, A>>,
        AG: Applicative<G>
    ): Kind<G, Kind<F, A>> =
        traverse(fga, AG) { it }

    fun <A> zipWithIndex(ta: Kind<F, A>): Kind<F, Pair<A, Int>> =
        traverseS(ta) { a: A ->
            State.getState<Int>().flatMap { s: Int ->
                State.setState(s + 1).map { _ -> a to s }
            }
        }.run(0).first

    fun <A> zipWithIndex2(ta: Kind<F, A>): Kind<F, Pair<A, Int>> =
        mapAccum(ta, 0) { a: A, s: Int ->
            (a to s) to (s + 1)
        }.first

    fun <A> toList2(ta: Kind<F, A>): ListL<A> =
        traverseS(ta) { a: A ->
            State.getState<ListL<A>>().flatMap { la: ListL<A> ->
                State.setState<ListL<A>>(Cons(a, la)).map { _ -> Unit }
            }
        }.run(Nil).second.reversed()

    fun <A> toList3(ta: Kind<F, A>): ListL<A> =
        mapAccum(ta, Nil) { a: A, sl: ListL<A> ->
            (Unit) to (Cons(a, sl))
        }.second.reversed()

    fun <S, A, B> mapAccum(
        fa: Kind<F, A>,
        s: S,
        f: (A, S) -> Pair<B, S>
    ): Pair<Kind<F, B>, S> =
        traverseS(fa) { a: A ->
            State.getState<S>().flatMap { s1 ->
                val (b, s2) = f(a, s1)
                State.setState(s2).map { _ -> b }
            }
        }.run(s)
}
