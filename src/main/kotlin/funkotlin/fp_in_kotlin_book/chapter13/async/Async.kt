package funkotlin.fp_in_kotlin_book.chapter13.async

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter07.flatMap


sealed class ForAsync { companion object }
typealias AsyncOf<A> = Kind<ForAsync, A>
inline fun <A> AsyncOf<A>.fix(): Async<A> = this as Async<A>

sealed class Async<A> : AsyncOf<A> {
    fun <B> flatMap(f: (A) -> Async<B>): Async<B> = FlatMap(this, f)
    fun <B> map(f: (A) -> B): Async<B> = flatMap { a -> Return(f(a)) }
}

data class Return<A>(val a: A) : Async<A>()
data class Suspend<A>(val resume: Par<A>) : Async<A>()
data class FlatMap<A, B>(
    val sub: Async<A>,
    val f: (A) -> Async<B>
) : Async<B>()


@Suppress("UNCHECKED_CAST")
tailrec fun <A> step(async: Async<A>): Async<A> =
    when (async) {
        is FlatMap<*, *> -> {
            val y = async.sub as Async<A>
            val g = async.f as (A) -> Async<A>
            when (y) {
                is FlatMap<*, *> -> {
                    val x = y.sub as Async<A>
                    val f = y.f as (A) -> Async<A>
                    step(x.flatMap { a: A -> f(a).flatMap(g) })
                }
                is Return -> step(g(y.a))
                else -> async
            }
        }
        else -> async
    }

@Suppress("UNCHECKED_CAST")
fun <A> run(async: Async<A>): Par<A> =
    when (val stepped = step(async)) {
        is Return -> Pars.unit(stepped.a)
        is Suspend -> stepped.resume
        is FlatMap<*, *> -> {
            val x = stepped.sub as Async<A>
            val f = stepped.f as (A) -> Async<A>
            when (x) {
                is Suspend -> x.resume.flatMap { a -> run(f(a))}
                else -> throw RuntimeException("Impossible, step eliminates such cases")
            }
        }
    }

fun main() {

}
