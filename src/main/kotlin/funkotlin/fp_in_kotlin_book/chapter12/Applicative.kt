package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import arrow.Kind2
import arrow.Kind3
import arrow.core.extensions.set.foldable.foldLeft
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter06.StateOf
import funkotlin.fp_in_kotlin_book.chapter06.StatePartialOf
import funkotlin.fp_in_kotlin_book.chapter10.Monoid
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter11.Functor
import funkotlin.fp_in_kotlin_book.chapter11.StateMonad


data class Product<F, G, A>(val value: Pair<Kind<F, A>, Kind<G, A>>) : ProductOf<F, G, A>
class ForProduct private constructor() { companion object }
typealias ProductOf<F, G, A> = Kind3<ForProduct, F, G, A>
typealias ProductPartialOf<F, G> = Kind2<ForProduct, F, G>
fun <F, G, A> ProductOf<F, G, A>.fix() = this as Product<F, G, A>

data class Composite<F, G, A>(val value: Kind<F, Kind<G, A>>) : CompositeOf<F, G, A>
class ForComposite private constructor() { companion object }
typealias CompositeOf<F, G, A> = Kind3<ForComposite, F, G, A>
typealias CompositePartialOf<F, G> = Kind2<ForComposite, F, G>
fun <F, G, A> CompositeOf<F, G, A>.fix() = this as Composite<F, G, A>

//typealias ConstInt<A> = Int
//typealias Const<M, A> = M
data class Const<M, out A>(val value: M) : ConstOf<M, A>
class ForConst private constructor() { companion object }
typealias ConstOf<M, A> = Kind2<ForConst, M, A>
typealias ConstPartialOf<E> = Kind<ForConst, E>
fun <M, A> ConstOf<M, A>.fix() = this as Const<M, A>

fun <M> monoidApplicative(m: Monoid<M>): Applicative<ConstPartialOf<M>> =
    object : Applicative<ConstPartialOf<M>> {
        override fun <A> unit(a: A): ConstOf<M, A> = Const(m.nil)

        override fun <A, B, C> map2(
            fa: ConstOf<M, A>,
            fb: ConstOf<M, B>,
            f: (A, B) -> C,
        ): ConstOf<M, C> =
            Const(m.combine(fa.fix().value, fb.fix().value))
    }

fun <S> stateMonadApplicative(m: StateMonad<S>) =
    object : Applicative<StatePartialOf<S>> {
        override fun <A> unit(a: A): StateOf<S, A> =
            m.unit(a)

        override fun <A, B, C> map2(
            fa: StateOf<S, A>,
            fb: StateOf<S, B>,
            f: (A, B) -> C,
        ): StateOf<S, C> =
            m.map2(fa, fb, f)

        override fun <A, B> map(
            fa: StateOf<S, A>,
            f: (A) -> B,
        ): StateOf<S, B> =
            m.map(fa, f)
    }


fun <F, G> product(
    AF: Applicative<F>,
    AG: Applicative<G>,
): Applicative<ProductPartialOf<F, G>> = object : Applicative<ProductPartialOf<F, G>> {
    override fun <A> unit(a: A): ProductOf<F, G, A> =
        Product(AF.unit(a) to AG.unit(a))

    override fun <A, B> apply(
        fgab: ProductOf<F, G, (A) -> B>,
        fga: ProductOf<F, G, A>,
    ): ProductOf<F, G, B> {
        val (fab: Kind<F, (A) -> B>, gab: Kind<G, (A) -> B>) = fgab.fix().value
        val (fa: Kind<F, A>, ga: Kind<G, A>) = fga.fix().value
        return Product(AF.apply(fab, fa) to AG.apply(gab, ga))
    }
}

fun <F, G> compose(
    AF: Applicative<F>,
    AG: Applicative<G>
): Applicative<CompositePartialOf<F, G>> = object : Applicative<CompositePartialOf<F, G>> {
    override fun <A> unit(a: A): CompositeOf<F, G, A> =
        Composite(AF.unit(AG.unit(a)))

    override fun <A, B, C> map2(
        fa: CompositeOf<F, G, A>,
        fb: CompositeOf<F, G, B>,
        f: (A, B) -> C,
    ): CompositeOf<F, G, C> {
        val result = AF.map2(
            fa.fix().value,
            fb.fix().value,
            ) { ga: Kind<G, A>, gb: Kind<G, B> ->
            AG.map2(ga, gb) { a: A, b: B ->
                f(a, b)
            }
        }
        return Composite(result)
    }
}

interface Applicative<F> : Functor<F> {
    fun <A, B> apply(
        fab: Kind<F, (A) -> B>,
        fa: Kind<F, A>,
    ): Kind<F, B> =
        map2(fa, fab) { a: A, f: (A) -> B -> f(a) }

    fun <A> unit(a: A): Kind<F, A>

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        apply(unit(f), fa)

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

    fun <K, V> sequence(
        mkv: Map<K, Kind<F, V>>
    ): Kind<F, Map<K, V>> {
        val zero: Kind<F, Map<K, V>> = unit(mapOf())

        return mkv.entries.foldLeft<Map.Entry<K, Kind<F, V>>, Kind<F, Map<K, V>>>(zero) { facc: Kind<F, Map<K, V>>, (fk: K, fv: Kind<F, V>) ->
            map2(facc, fv) { acc: Map<K, V>, v: V ->
                acc + (fk to v)
            }
        }
    }

    fun <A> replicateM(n: Int, ma: Kind<F, A>): Kind<F, ListL<A>> =
        sequence(ListL.fill(n, ma))

    fun <A, B> product(
        ma: Kind<F, A>,
        mb: Kind<F, B>
    ): Kind<F, Pair<A, B>> =
        map2(ma, mb) { a, b -> Pair(a, b) }

}
