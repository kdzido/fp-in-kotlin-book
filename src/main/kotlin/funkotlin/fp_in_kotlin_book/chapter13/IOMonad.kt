package funkotlin.fp_in_kotlin_book.chapter13

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter05.Cons
import funkotlin.fp_in_kotlin_book.chapter05.Empty
import funkotlin.fp_in_kotlin_book.chapter05.Stream
import funkotlin.fp_in_kotlin_book.chapter11.Monad
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.ForTailrec
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.Tailrec
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.TailrecOf
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.fix

interface IOMonad : Monad<ForTailrec> {
    override fun <A> unit(a: A): Kind<ForTailrec, A> =
        Tailrec { a }.fix()

    override fun <A, B> flatMap(
        fa: Kind<ForTailrec, A>,
        f: (A) -> Kind<ForTailrec, B>,
    ): Kind<ForTailrec, B> =
        fa.fix().flatMap { a -> f(a).fix() }

    override fun <A, B> map(
        fa: Kind<ForTailrec, A>,
        f: (A) -> B,
    ): Kind<ForTailrec, B> =
        fa.fix().flatMap { a -> unit(f(a)).fix() }

    fun <A> doWhile(
        fa: TailrecOf<A>,
        cond: (A) -> TailrecOf<Boolean>
    ): TailrecOf<Unit> =
        fa.fix().flatMap { a: A ->
            cond(a).fix().flatMap<Unit> { ok: Boolean ->
                if (ok) doWhile(fa, cond).fix() else unit(Unit).fix()
            }
        }

    fun <A, B> forever(fa: TailrecOf<A>): TailrecOf<B> {
        val t: TailrecOf<B> by lazy { forever<A, B>(fa) }
        return fa.fix().flatMap { t.fix() }
    }

    fun <A, B> foldM(
        sa: Stream<A>,
        z: B,
        f: (B, A) -> TailrecOf<B>,
    ): TailrecOf<B> =
        when (sa) {
            is Cons -> f(z, sa.head()).fix().flatMap { b ->
                foldM(sa.tail(), z, f).fix()
            }
            is Empty -> unit(z)
        }

    fun <A, B> foldDiscardM(
        sa: Stream<A>,
        z: B,
        f: (B, A) -> Kind<ForTailrec, B>,
    ): Kind<ForTailrec, Unit> =
        foldM(sa, z, f).fix().map { Unit }

    fun <A> foreachM(
        sa: Stream<A>,
        f: (A) -> TailrecOf<Unit>,
    ): TailrecOf<Unit> =
        foldDiscardM(sa, Unit) { _, a -> f(a)}

    fun <A> skip(fa: Kind<ForTailrec, A>): Kind<ForTailrec, Unit> = map(fa) { Unit }

    fun <A> sequenceDiscard(sa: Stream<Kind<ForTailrec, A>>): Kind<ForTailrec, Unit> =
        foreachM(sa) { a -> skip(a) }

    fun <A> sequenceDiscard(vararg fa: Kind<ForTailrec, A>): Kind<ForTailrec, Unit> =
        sequenceDiscard(Stream.of(*fa))

    fun <A> whenM(
        ok: Boolean,
        f: () -> Kind<ForTailrec, A>,
    ): Kind<ForTailrec, Boolean> =
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
