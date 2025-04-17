package funkotlin.fp_in_kotlin_book.chapter07

import arrow.core.extensions.list.foldable.firstOption
import arrow.core.getOrElse
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

typealias Par<A> = (ExecutorService) -> Future<A>


object Pars {
    fun <A> unit(a: A): Par<A> = { es: ExecutorService -> UnitFuture(a) }
    fun <A> lazyUnit(a: () -> A): Par<A> = Pars.fork { Pars.unit(a()) }
    fun <A, B> asyncF(f: (A) -> B): (A) -> Par<B> =  { a: A -> lazyUnit{ f(a) } }

    data class UnitFuture<A>(val a: A): Future<A> {
        override fun get(): A = a
        override fun get(timeout: Long, unit: TimeUnit): A = a
        override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
        override fun isDone(): Boolean = true
        override fun isCancelled(): Boolean = false
    }

    data class TimeoutableUnitFuture<A, B, C>(
        private val af: Future<A>,
        private val bf: Future<B>,
        private val f: (A, B) -> C
    ): Future<C> {
        override fun get(): C = f(af.get(), bf.get())
        override fun get(timeout: Long, unit: TimeUnit): C {
            val aResult: A
            val aTookMillis = measureTimeMillis {
                aResult = af.get(timeout, unit)
            }
            val bResult: B = bf.get(timeout - aTookMillis, unit)
            return f(aResult, bResult)
        }
        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            val cancelA = af.cancel(mayInterruptIfRunning)
            val cancelB = bf.cancel(mayInterruptIfRunning)
            return cancelA || cancelB
        }
        override fun isDone(): Boolean = af.isDone && bf.isDone
        override fun isCancelled(): Boolean = af.isCancelled || bf.isCancelled
    }

    fun <A, B, C> map2(
        a: Par<A>,
        b: Par<B>,
        f: (A, B) -> C
    ): Par<C> = { es: ExecutorService ->
        val af: Future<A> = a(es)
        val bf: Future<B> = b(es)
        TimeoutableUnitFuture(af, bf, f)
    }

    fun <A> fork(a: () -> Par<A>): Par<A> = { es: ExecutorService ->
        es.submit(Callable<A> { a()(es).get() })
    }
}

fun sum2(ints: List<Int>): Par<Int> =
    if (ints.size <= 1)
        Pars.lazyUnit { ints.firstOption().getOrElse { 0 } }
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        Pars.map2(sum2(l), sum2(r)) { lx: Int, rx: Int -> lx + rx}
    }

fun sum3(ints: List<Int>): Par<Int> =
    if (ints.size <= 1)
        Pars.lazyUnit { ints.firstOption().getOrElse { 0 } }
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        Pars.map2(
            Pars.fork { sum3(l) },
            Pars.fork { sum3(r) }
        ) { lx: Int, rx: Int -> lx + rx}
    }

fun <T> Iterable<T>.splitAt(n: Int): Pair<List<T>, List<T>> =
    when {
        n < 0 -> throw IllegalArgumentException("Requested split at index $n is less than zero.")
        n == 0 -> emptyList<T>() to toList()
        this is Collection<T> && (n >= size) -> toList() to emptyList()
        else -> {
            val dn = if (this is Collection<T>) size - n else n
            val left = ArrayList<T>(n)
            val right = ArrayList<T>(dn)
            for ((idx, item) in this.withIndex()) {
                when (idx >= n) {
                    false -> left.add(item)
                    true -> right.add(item)
                }
            }
            left to right
        }
    }.let {
        it.first to it.second
    }
