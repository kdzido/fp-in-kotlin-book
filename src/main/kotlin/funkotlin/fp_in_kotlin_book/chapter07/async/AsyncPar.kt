package funkotlin.fp_in_kotlin_book.chapter07.async

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.Option
import arrow.core.None
import arrow.core.Some
import arrow.core.extensions.list.foldable.firstOption
import arrow.core.getOrElse
import funkotlin.fp_in_kotlin_book.chapter07.Actor
import funkotlin.fp_in_kotlin_book.chapter07.Strategy
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference

abstract class Future<A> {
    internal abstract fun invoke(ch: (A) -> Unit)
}
typealias Par<A> = (ExecutorService) -> Future<A>


object AsyncPars {
    fun <A> run(es: ExecutorService, pa: Par<A>): A {
        val ref = AtomicReference<A>()
        val latch = CountDownLatch(1)
        pa(es).invoke { a: A ->
            ref.set(a)
            latch.countDown()
        }
        latch.await()
        return ref.get()
    }

    fun <A> run2(es: ExecutorService, pa: Par<A>): A {
        val ref = CompletableFuture<A>()
        pa(es).invoke { a: A ->
            ref.complete(a)
        }
        return ref.get()
    }


    fun <A> unit(a: A): Par<A> = { es: ExecutorService ->
        object : Future<A>() {
            override fun invoke(cb: (A) -> Unit) = cb(a)
        }
    }
    fun <A> lazyUnit(a: () -> A): Par<A> = AsyncPars.fork { AsyncPars.unit(a()) }
    fun <A, B> asyncF(f: (A) -> B): (A) -> Par<B> =  { a: A -> lazyUnit{ f(a) } }
    fun sortPar(parList: Par<List<Int>>): Par<List<Int>> = map(parList) { a -> a.sorted() }

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
                AsyncPars.fork { parFilter(l, f) },
                AsyncPars.fork { parFilter(r, f) }
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

    fun <A, B> map(par: Par<A>, f: (A) -> B): Par<B> = map2(par, unit(Unit)) { a, _ -> f(a) }

    fun <A, B, C> map2(
        pa: Par<A>,
        pb: Par<B>,
        f: (A, B) -> C
    ): Par<C> = { es: ExecutorService ->
        object : Future<C>() {
            override fun invoke(cb: (C) -> Unit) {
                val ar = AtomicReference<Option<A>>(None)
                val br = AtomicReference<Option<B>>(None)
                val combiner = Actor<Either<A, B>>(Strategy.from(es)) { eab ->
                    when (eab) {
                        is Either.Left<A> -> br.get().fold(
                            { ar.set(Some(eab.a)) },
                            { b -> eval(es) { cb(f(eab.a, b)) } }
                        )
                        is Either.Right<B> -> ar.get().fold(
                            { br.set(Some(eab.b)) },
                            { a -> eval(es) { cb(f(a, eab.b)) } }
                        )
                    }
                }
                pa(es).invoke { a: A -> combiner.send(Left(a)) }
                pb(es).invoke { b: B -> combiner.send(Right(b)) }
            }
        }
    }

    fun <A, B> parMap(ps: List<A>, f: (A) -> B): Par<List<B>> = fork {
        val fbs: List<Par<B>> = ps.map(asyncF(f))
        sequence(fbs)
    }

    // TODO fix never ending call
    // TODO fix never ending call
    // TODO fix never ending call
    fun <A> sequence(ps: List<Par<A>>): Par<List<A>> = { es ->
        tailrec fun go(left: List<Par<A>>, acc: List<A>): List<A> {
            return when {
                left.isEmpty() -> acc
                else -> {
                    val h: Par<A> = left.first()
                    val ts: List<Par<A>> = left.drop(1)
                    go(ts, listOf(run(es, h)) + acc)
                }
            }
        }
//        es.submit(Callable { go(ps, listOf()).reversed() })
        object : Future<List<A>>() {
            override fun invoke(cb: (List<A>) -> Unit) {
                val resultList = AtomicReference<List<Pair<Int, A>>>()
//                val ar = AtomicReference<Option<A>>(None)
//                val br = AtomicReference<Option<B>>(None)
//                val combiner = Actor<Either<A, B>>(Strategy.from(es)) { eab ->
//                    when (eab) {
//                        is Either.Left<A> -> br.get().fold(
//                            { ar.set(Some(eab.a)) },
//                            { b -> eval(es) { cb(f(eab.a, b)) } }
//                        )
//                        is Either.Right<B> -> ar.get().fold(
//                            { br.set(Some(eab.b)) },
//                            { a -> eval(es) { cb(f(a, eab.b)) } }
//                        )
//                    }
//                }
//                pa(es).invoke { a: A -> combiner.send(Left(a)) }
//                pb(es).invoke { b: B -> combiner.send(Right(b)) }

//                pb(es).invoke { b: B -> combiner.send(Right(b)) }

//                ps.forEachIndexed{ idx, par: Par<A> ->
//                    par(es).invoke { a: A -> combiner.send(Pair(idx, a)) }
//                }
                //                go(ps, listOf()).reversed()

                resultList.get() // needed?
            }
        }
    }

    fun <A> fork(a: () -> Par<A>): Par<A> = { es: ExecutorService ->
        object : Future<A>() {
            override fun invoke(cb: (A) -> Unit) {
                eval(es) { a()(es).invoke(cb) }
            }
        }
    }

    fun eval(es: ExecutorService, rs: () -> Unit) {
        es.submit(Callable { rs() })
    }

    fun <A> delay(a: () -> Par<A>): Par<A> = { es: ExecutorService ->
        a()(es)
    }

    infix fun <A> Par<A>.shouldBePar(other: Par<A>) = { es: ExecutorService ->
        if (run(es, this) != run(es, other))
            throw AssertionError("Par instance not equal")
    }
}


fun sum2(ints: List<Int>): Par<Int> =
    if (ints.size <= 1)
        AsyncPars.lazyUnit { ints.firstOption().getOrElse { 0 } }
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        AsyncPars.map2(sum2(l), sum2(r)) { lx: Int, rx: Int -> lx + rx }
    }

fun sum3(ints: List<Int>): Par<Int> =
    if (ints.size <= 1)
        AsyncPars.lazyUnit { ints.firstOption().getOrElse { 0 } }
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        AsyncPars.map2(
            AsyncPars.fork { sum3(l) },
            AsyncPars.fork { sum3(r) }
        ) { lx: Int, rx: Int -> lx + rx }
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
