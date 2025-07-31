package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter12.Applicative

data class Id<out A>(val a: A) : IdOf<A> {
    companion object {
        fun <A> unit(a: A): Id<A> = Id(a)
    }

    fun <B> flatMap(f: (A) -> Id<B>): Id<B> = f(a)

    fun <B> map(f: (A) -> B): Id<B> = unit(f(a))
}

fun idMonad(): Monad<ForId> = object : Monad<ForId> {
    override fun <A> unit(a: A): Kind<ForId, A> = Id(a)

    override fun <A, B, C> compose(
        f: (A) -> Kind<ForId, B>,
        g: (B) -> Kind<ForId, C>,
    ): (A) -> Kind<ForId, C> = { a: A ->
        f(a).fix().flatMap{ b -> g(b).fix() }
    }
}

// Kotlin workaround for HOTs
class ForId private constructor() { companion object }
typealias IdOf<A> = Kind<ForId, A>
inline fun <A> IdOf<A>.fix() = this as Id<A>

fun idApplicative(): Applicative<ForId> = object : Applicative<ForId> {
    override fun <A> unit(a: A): Kind<ForId, A> = Id(a)

    override fun <A, B, C> map2(
        fa: IdOf<A>,
        fb: IdOf<B>,
        f: (A, B) -> C,
    ): IdOf<C> =
        fa.fix().flatMap { a -> fb.fix().map { b -> f(a, b) } }
}
