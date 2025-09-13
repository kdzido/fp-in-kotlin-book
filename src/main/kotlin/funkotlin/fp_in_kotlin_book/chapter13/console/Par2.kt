package funkotlin.fp_in_kotlin_book.chapter13.console

import java.util.concurrent.ExecutorService
import arrow.Kind

class ForPar2 private constructor() { companion object }
typealias Par2Of<T> = Kind<ForPar2, T>
fun <A> Par2Of<A>.fix() = this as Par2<A>

class Par2<A>(val run: (ExecutorService) -> Future<A>): Par2Of<A> {
    companion object
}

abstract class Future<A> {
    internal abstract fun invoke(cb: (A) -> Unit)
}

fun <A> Par2.Companion.async(run: ((A) -> Unit) -> Unit): Par2<A> =
    Par2 { es ->
        object : Future<A>() {
            override fun invoke(cb: (A) -> Unit) = run(cb)
        }
    }

