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
    fun sortPar(parList: Par<List<Int>>): Par<List<Int>> = map(parList) { a -> a.sorted() }

    fun <A, B> parMap(ps: List<A>, f: (A) -> B): Par<List<B>> = fork {
        val fbs: List<Par<B>> = ps.map(asyncF(f))
        sequence(fbs)
    }

    fun <A> parFilter(ps: List<A>, f: (A) -> Boolean): Par<List<A>> =
        if (ps.size <= 1)
            lazyUnit {
                ps.firstOption()
                    .filter(f)
                    .map { listOf(it) }
                    .getOrElse { emptyList() }
            }
        else {
            val (l, r) = ps.splitAt(ps.size / 2)
            map2(
                Pars.fork { parFilter(l, f) },
                Pars.fork { parFilter(r, f) }
            ) { lx: List<A>, rx: List<A> -> lx + rx}
        }

    fun <A, B> parFoldLeft(xs: List<A>, z: B, f: (B, A) -> B): Par<B> =
        if (xs.isEmpty())
            lazyUnit { z }
        else if (xs.size == 1)
            lazyUnit { f(z, xs.get(0)) }
        else {
            val h = xs.first()
            val ts = xs.drop(1)
            parFoldLeft(ts, f(z, h), f)
        }

    fun <K, V> choiceMap(key: Par<K>, choices: Map<K, Par<V>>): Par<V> = flatMap(key) {
        choices.getValue(it)
    }

    fun <A> choiceN(n: Par<Int>, choices: List<Par<A>>): Par<A> = flatMap(n) {
        choices.get(it)
    }

    fun <A> choice(cond: Par<Boolean>, t: Par<A>, f: Par<A>): Par<A> = flatMap(cond) {
        when (it) {
            true -> t
            false -> f
        }
    }

    fun <A> choice2(cond: Par<Boolean>, t: Par<A>, f: Par<A>): Par<A> =
        choiceN(
            Pars.map(cond) { it -> if (it) 0 else 1},
            listOf(t, f)
        )

    fun <A, B> chooser(pa: Par<A>, choices: (A) -> Par<B>): Par<B> =
        flatMap(pa, choices)

    fun <A, B> flatMap(pa: Par<A>, f: (A) -> Par<B>): Par<B> = { es ->
        val k = pa(es).get()
        f(k)(es)
    }

    fun <A, B> map(par: Par<A>, f: (A) -> B): Par<B> = map2(par, unit(Unit)) { a, _ -> f(a) }
    fun <A, B, C> map2(
        a: Par<A>,
        b: Par<B>,
        f: (A, B) -> C
    ): Par<C> = { es: ExecutorService ->
        val af: Future<A> = a(es)
        val bf: Future<B> = b(es)
        TimeoutableUnitFuture(af, bf, f)
    }

    fun <A, B, C, D> map3(
        ap: Par<A>,
        bp: Par<B>,
        cp: Par<C>,
        f: (A, B, C) -> D
    ): Par<D> = map2(
        map2(ap, bp) { a, b ->
            Pair(a, b)
        },
        cp
    ) { ab: Pair<A, B>, c ->
        val (a, b) = ab
        f(a, b, c)
    }

    fun <A, B, C, D, E> map4(
        ap: Par<A>,
        bp: Par<B>,
        cp: Par<C>,
        dp: Par<D>,
        f: (A, B, C, D) -> E
    ): Par<E> = map2(
        map2(ap, bp) { a, b ->
            Pair(a, b)
        },
        map2(cp, dp) { c, d ->
            Pair(c, d)
        }
    ) { ab: Pair<A, B>, cd: Pair<C, D> ->
        val (a, b) = ab
        val (c, d) = cd
        f(a, b, c, d)
    }

    fun <A, B, C, D, E, F> map5(
        ap: Par<A>,
        bp: Par<B>,
        cp: Par<C>,
        dp: Par<D>,
        ep: Par<E>,
        g: (A, B, C, D, E) -> F,
    ): Par<F> = map2(
        map2(
            map2(ap, bp) { a, b ->
                Pair(a, b)
            },
            cp) { ab, c ->
            Triple(ab.first, ab.second, c)
        },
        map2(dp, ep) { d, e ->
            Pair(d, e)
        }
    ) { abc: Triple<A, B, C>, de: Pair<D, E> ->
        val (a, b, c) = abc
        val (d, e) = de
        g(a, b, c, d, e)
    }

    fun <A> sequence(ps: List<Par<A>>): Par<List<A>> = { es ->
        tailrec fun go(left: List<Par<A>>, acc: List<A>): List<A> {
            return when {
                left.isEmpty() -> acc
                else -> {
                    val h: Par<A> = left.first()
                    val ts: List<Par<A>> = left.drop(1)
                    go(ts, listOf(h(es).get()) + acc)
                }
            }
        }
        es.submit(Callable { go(ps, listOf()).reversed() })
    }

    fun <A> fork(a: () -> Par<A>): Par<A> = { es: ExecutorService ->
        es.submit(Callable<A> { a()(es).get() })
    }

    fun <A> delay(a: () -> Par<A>): Par<A> = { es: ExecutorService ->
        a()(es)
    }

    infix fun <A> Par<A>.shouldBePar(other: Par<A>) = { es: ExecutorService ->
        if (this(es).get() != other(es).get())
            throw AssertionError("Par instance not equal")
    }

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
}

fun sum2(ints: List<Int>): Par<Int> =
    if (ints.size <= 1)
        Pars.lazyUnit { ints.firstOption().getOrElse { 0 } }
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        Pars.map2(
            sum2(l),
            sum2(r)
        ) { lx: Int, rx: Int -> lx + rx}
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
