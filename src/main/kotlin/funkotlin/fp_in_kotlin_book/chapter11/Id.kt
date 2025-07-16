package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind

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
