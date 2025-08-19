package funkotlin.fp_in_kotlin_book.chapter13

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter05.Cons
import funkotlin.fp_in_kotlin_book.chapter05.Empty
import funkotlin.fp_in_kotlin_book.chapter05.Stream
import funkotlin.fp_in_kotlin_book.chapter11.Monad

interface IOMonad : Monad<ForIO> {
    override fun <A> unit(a: A): Kind<ForIO, A> =
        IO { a }.fix()

    override fun <A, B> flatMap(
        fa: Kind<ForIO, A>,
        f: (A) -> Kind<ForIO, B>,
    ): Kind<ForIO, B> =
        fa.fix().flatMap { a -> f(a).fix() }

    override fun <A, B> map(
        fa: Kind<ForIO, A>,
        f: (A) -> B,
    ): Kind<ForIO, B> =
        fa.fix().flatMap { a -> unit(f(a)).fix() }

    fun <A> doWhile(
        fa: IOOf<A>,
        cond: (A) -> IOOf<Boolean>
    ): IOOf<Unit> =
        fa.fix().flatMap { a: A ->
            cond(a).fix().flatMap<Unit> { ok: Boolean ->
                if (ok) doWhile(fa, cond).fix() else unit(Unit).fix()
            }
        }

    fun <A, B> forever(fa: IOOf<A>): IOOf<B> {
        val t: IOOf<B> by lazy { forever<A, B>(fa) }
        return fa.fix().flatMap { t.fix() }
    }

    fun <A, B> foldM(
        sa: Stream<A>,
        z: B,
        f: (B, A) -> IOOf<B>,
    ): IOOf<B> =
        when (sa) {
            is Cons -> f(z, sa.head()).fix().flatMap { b ->
                foldM(sa.tail(), z, f).fix()
            }
            is Empty -> unit(z)
        }

    fun <A, B> foldDiscardM(
        sa: Stream<A>,
        z: B,
        f: (B, A) -> Kind<ForIO, B>,
    ): Kind<ForIO, Unit> =
        foldM(sa, z, f).fix().map { Unit }

    fun <A> foreachM(
        sa: Stream<A>,
        f: (A) -> IOOf<Unit>,
    ): IOOf<Unit> =
        foldDiscardM(sa, Unit) { _, a -> f(a)}

    fun <A> skip(fa: Kind<ForIO, A>): Kind<ForIO, Unit> = map(fa) { Unit }

    fun <A> sequenceDiscard(sa: Stream<Kind<ForIO, A>>): Kind<ForIO, Unit> =
        foreachM(sa) { a -> skip(a) }

    fun <A> sequenceDiscard(vararg fa: Kind<ForIO, A>): Kind<ForIO, Unit> =
        sequenceDiscard(Stream.of(*fa))

    fun <A> whenM(
        ok: Boolean,
        f: () -> Kind<ForIO, A>,
    ): Kind<ForIO, Boolean> =
        if (ok) f().fix().map { true } else unit(false)
}

fun IntRange.toStream(): Stream<Int> {
    fun stream(from: Int, to: Int): Stream<Int> =
        when (from) {
            this.last + 1 ->
                Stream.empty()
            else ->
                Stream.cons({ from }, { stream(from + 1, to) })
        }
    return stream(this.first, this.last)
}
