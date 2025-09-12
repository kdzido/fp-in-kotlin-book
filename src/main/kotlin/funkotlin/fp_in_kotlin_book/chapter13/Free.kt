package funkotlin.fp_in_kotlin_book.chapter13

import arrow.Kind
import arrow.Kind2
import funkotlin.fp_in_kotlin_book.chapter11.Monad

sealed class ForFree { companion object }
typealias FreeOf<F, A> = Kind2<ForFree, F, A>
typealias FreePartialOf<F> = Kind<ForFree, F>
inline fun <F, A> FreeOf<F, A>.fix(): Free<F, A> = this as Free<F, A>

sealed class Free<F, A> : FreeOf<F, A> {
    companion object {
        fun <F, A> unit(a: A): Free<F, A> = Return(a)
    }
}

data class Return<F, A>(val a: A) : Free<F, A>()
//data class Suspend<F, A>(val s: Kind<F, A>) : Free<F, A>()
data class Suspend<F, A>(val resume: Kind<F, A>) : Free<F, A>()
data class FlatMap<F, A, B>(
    val s: Free<F, A>,
    val f: (A) -> Free<F, B>
): Free<F, B>()

fun <F, A, B> Free<F, A>.flatMap(f: (A) -> Free<F, B>): Free<F, B> =
    FlatMap(this.fix(), f)

fun <F, A, B> Free<F, A>.map(f: (A) -> B): Free<F, B> =
    flatMap { a -> Return<F, B>(f(a)) }


fun <F> freeMonad() = object : Monad<FreePartialOf<F>> {
    override fun <A, B> map(
        fa: FreeOf<F, A>,
        f: (A) -> B,
    ): FreeOf<F, B> =
        flatMap(fa) { a -> unit(f(a)) }

    override fun <A, B> flatMap(
        fa: FreeOf<F, A>,
        f: (A) -> FreeOf<F, B>,
    ): FreeOf<F, B> =
        fa.fix().flatMap { a -> f(a).fix() }

    override fun <A> unit(a: A): FreeOf<F, A> = Return(a)
}


sealed class ForFunction0 { companion object }
typealias Function0Of<A> = Kind<ForFunction0, A>
inline fun <A> Function0Of<A>.fix() = this as Function0<A>

data class Function0<out A>(val f: () -> A) : Function0Of<A> {
    companion object {
        operator fun <A> invoke(f: () -> A) = Function0(f)
    }
}

tailrec fun <A> runTrampoline(ffa: Free<ForFunction0, A>): A = when (ffa) {
    is Return -> ffa.a
    is Suspend -> ffa.resume.fix().f()
    is FlatMap<*, *, *> -> {
        val sout = ffa.s as Free<ForFunction0, A>
        val fout = ffa.f as (A) -> Free<ForFunction0, A>
        when (sout) {
            is FlatMap<*, *, *> -> {
                val sin = sout.s as Free<ForFunction0, A>
                val fin = sout.f as (A) -> Free<ForFunction0, A>
                runTrampoline(sin.flatMap { a -> fin(a).flatMap(fout) })
            }
            is Return -> sout.a
            is Suspend -> sout.resume.fix().f()
        }
    }
}

@Suppress("UNCHECKED_CAST")
tailrec fun <F, A> step(free: Free<F, A>): Free<F, A> =
    when (free) {
        is FlatMap<*, *, *> -> {
            val y = free.s as Free<F, A>
            val g = free.f as (A) -> Free<F, A>
            when (y) {
                is FlatMap<*, *, *> -> {
                    val x = y.s as Free<F, A>
                    val f = y.f as (A) -> Free<F, A>
                    step(x.flatMap { a: A -> f(a).flatMap(g) })
                }
                is Return -> step(g(y.a))
                else -> free
            }
        }
        else -> free
    }

@Suppress("UNCHECKED_CAST")
fun <F, A> run(free: Free<F, A>, M: Monad<F>): Kind<F, A> =
    when (val stepped = step(free)) {
        is Return -> M.unit(stepped.a)
        is Suspend -> stepped.resume
        is FlatMap<*, *, *> -> {
            val x = stepped.s as Free<F, A>
            val f = stepped.f as (A) -> Free<F, A>
            when (x) {
                is Suspend -> M.flatMap(x.resume) { a: A -> run(f(a), M)}
                else -> throw RuntimeException("Impossible, step eliminates such cases")
            }
        }
    }

interface Translate<F, G>  {
    operator fun <A> invoke(fa: Kind<F, A>): Kind<G, A>
}

fun <F, G, A> translate(
    free: Free<F, A>,
    t: Translate<F, G>
): Free<G, A> {
    val t = object : Translate<F, FreePartialOf<G>> {
        override fun <A> invoke(fa: Kind<F, A>): Kind<FreePartialOf<G>, A> = Suspend(t(fa))
    }
    return runFree(free, t, freeMonad()).fix()
}


fun <F, G, A> runFree(
    free: Free<F, A>,
    t: Translate<F, G>,
    MG: Monad<G>
): Kind<G, A> =
    when (val stepped = step(free)) {
        is Return -> MG.unit(stepped.a)
        is Suspend -> t(stepped.resume)
        is FlatMap<*, *, *> -> {
            val x = stepped.s as Free<F, A>
            val f = stepped.f as (A) -> Free<F, A>
            when (x) {
                is Suspend -> MG.flatMap(t(x.resume)) { a: A -> runFree(f(a), t, MG)}
                else -> throw RuntimeException("Impossible, step eliminates such cases")
            }
        }
    }

fun main() {
    println("[main]")
    println("------------")
    runTrampoline(Return(Function0( { println("Function0_Return"); 123 } ))).f()
    runTrampoline(Suspend(Function0( { println("Function0_Suspend"); 123 } )))

    val r: Int = runTrampoline(FlatMap( Suspend(Function0( { println("Function0_Sub"); 123 } )), { a: Int -> Return(a * 2)  }))
    println("FlatMap result: $r ")
}
