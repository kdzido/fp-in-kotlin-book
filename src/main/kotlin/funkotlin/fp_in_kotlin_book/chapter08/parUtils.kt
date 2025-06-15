package funkotlin.fp_in_kotlin_book.chapter08

import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter08.Prop.Companion.forAll
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun <A> equal(p1: Par<A>, p2: Par<A>): Par<Boolean> =
    Pars.map2(p1, p2, { a, b -> a == b })




val ges: Gen<ExecutorService> = weighted(
    Gen.choose(1, 4).map {
        Executors.newFixedThreadPool(it)
    } to .75,
    Gen.unit(
        Executors.newCachedThreadPool()
    ) to .25)

fun checkPar(p: Par<Boolean>): Prop =
    forAllPar(Gen.unit(Unit)) { p }

fun <A> forAllPar(ga: Gen<A>, f: (A) -> Par<Boolean>): Prop =
    forAll(
        map2(ges, ga) { es, a -> es to a }
    ) { (es, a) -> f(a)(es).get() }

fun <A> forAllPar2(ga: Gen<A>, f: (A) -> Par<Boolean>): Prop =
    forAll(
        combine(ges, ga)
    ) { (es, a) -> f(a)(es).get() }

fun <A> forAllPar3(ga: Gen<A>, f: (A) -> Par<Boolean>): Prop =
    forAll(ges combine2 ga) {
        (es, a) -> f(a)(es).get()
    }

fun <A, B> combine(ga: Gen<A>, gb: Gen<B>): Gen<Pair<A, B>> =
    map2(ga, gb) { a, b -> a to b }

infix fun <A, B>  Gen<A>. combine2(gb: Gen<B>): Gen<Pair<A, B>> =
    map2(this, gb) { a, b -> a to b }

fun weighted(
    pa: Pair<Gen<ExecutorService>, Double>,
    pb: Pair<Gen<ExecutorService>, Double>,
): Gen<ExecutorService> =
    TODO()

fun <A, B, C> map2(ga: Gen<A>, gb: Gen<B>, f: (A, B) -> C): Gen<C> =
    TODO()
