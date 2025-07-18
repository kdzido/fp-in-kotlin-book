package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter04.ForOption

interface Monad<F> : Functor<F> {
    fun <A> unit(a: A): Kind<F, A>

    fun <A, B> _flatMap(
        fa: Kind<F, A>,
        f: (A) -> Kind<F, B>
    ): Kind<F, B> {
        val f1: (Kind<F, A>) -> Kind<F, A> = { k -> k }
        return compose(f1, f)(fa)
    }

    fun <A, B> __flatMap(
        fa: Kind<F, A>,
        f: (A) -> Kind<F, B>
    ): Kind<F, B> =
        join(map(fa) { a: A -> f(a) })

    fun <A, B, C> __compose(
        f: (A) -> Kind<F, B>,
        g: (B) -> Kind<F, C>,
    ): (A) -> Kind<F, C> = { a: A ->
        join(map(f(a)) { b: B -> g(b) })
    }

    fun <A, B> flatMap(
        fa: Kind<F, A>,
        f: (A) -> Kind<F, B>
    ): Kind<F, B> =
        compose<Unit, A, B>({ _ -> fa }, f)(Unit)

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        flatMap(fa) { a -> unit(f(a)) }

    fun <A> join(ffa: Kind<F, Kind<F, A>>): Kind<F, A> =
        flatMap(ffa) { fa: Kind<F, A> -> fa }

    fun <A, B, C> map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C,
    ): Kind<F, C> =
        flatMap(fa) { a -> map(fb) { b -> f(a, b) } }

    fun <A> sequence(lfa: List<Kind<F, A>>): Kind<F, List<A>> =
        List.foldRight2(
            lfa,
            unit(List.of()),
            { fa: Kind<F, A>, fla: Kind<F, List<A>> ->
                map2(fa, fla) { a: A, la: List<A> -> Cons(a, la) }
            }
        )

    fun <A, B> traverse(
        la: List<A>,
        f: (A) -> Kind<F, B>,
    ): Kind<F, List<B>> =
        sequence(List.map(la, f))

    fun <A> replicateM(n: Int, ma: Kind<F, A>): Kind<F, List<A>> =
        sequence(List.fill(n, ma))

    fun <A> _replicateM(n: Int, ma: Kind<F, A>): Kind<F, List<A>> =
        if (n >= 1) {
            map2(ma, _replicateM(n - 1, ma)) { a: A, la: List<A> -> Cons(a, la) }
        } else {
            unit(List.of())
        }

    fun <A, B> product(
        ma: Kind<F, A>,
        mb: Kind<F, B>
    ): Kind<F, Pair<A, B>> =
        map2(ma, mb) { a, b -> Pair(a, b) }

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
    ): (A) -> Kind<F, C>
}

fun <A, B> Kind<ForOption, A>.flatMap(
    f: (A) -> Kind<ForOption, B>
): Kind<ForOption, B> =
    Monads.optionMonad().flatMap(this, f)
