package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter12.Applicative

interface Monad<F> : Applicative<F> {
    override fun <A> unit(a: A): Kind<F, A>

    fun <A, B> flatMap(
        fa: Kind<F, A>,
        f: (A) -> Kind<F, B>
    ): Kind<F, B> =
        join(map(fa, f))

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        flatMap(fa) { a -> unit(f(a)) }

    fun <A> join(ffa: Kind<F, Kind<F, A>>): Kind<F, A> =
        flatMap(ffa) { fa: Kind<F, A> -> fa }

    override fun <A, B, C> map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C,
    ): Kind<F, C> =
        flatMap(fa) { a -> map(fb) { b -> f(a, b) } }

    fun <A> filterM(
        ms: List<A>,
        f: (A) -> Kind<F, Boolean>
    ): Kind<F, List<A>> = when (ms) {
        is Nil -> unit(Nil)
        is Cons ->
            flatMap(f(ms.head)) { success: Boolean ->
                if (success)
                    map(filterM(ms.tail, f)) { tail ->
                        Cons(ms.head, tail)
                    }
                else
                    filterM(ms.tail, f)
            }
    }

    fun <A, B, C> compose(
        f: (A) -> Kind<F, B>,
        g: (B) -> Kind<F, C>,
    ): (A) -> Kind<F, C> = { a: A ->
        flatMap(f(a)) { a -> g(a) }
    }
}

