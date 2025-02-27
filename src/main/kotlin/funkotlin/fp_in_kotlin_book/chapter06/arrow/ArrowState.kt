package funkotlin.fp_in_kotlin_book.chapter06.arrow

import arrow.core.Id
import arrow.core.Tuple2
import arrow.core.extensions.id.monad.monad
import arrow.mtl.State
import arrow.mtl.StateApi
import arrow.mtl.extensions.fx
import arrow.mtl.run
import funkotlin.fp_in_kotlin_book.chapter06.RNG
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG

val int: State<RNG, Int> = State { rng -> rng.nextIntTup() }

fun ints(n: Int): State<RNG, List<Int>> =
    TODO()

fun <A, B> flatMap(
    s: State<RNG, A>,
    f: (A) -> State<RNG, B>,
): State<RNG, B> = State { rng ->
    val (r2: RNG, a1: A) = s.run(rng)
    f(a1).run(r2)
}

fun <A, B> map(
    s: State<RNG, A>,
    f: (A) -> B,
): State<RNG, B> = flatMap(s) { a -> StateApi.just(f(a)) }

val ns: State<RNG, List<Int>> =
    flatMap(int) { x ->
        flatMap(int) { y ->
            map(ints(x)) { xs ->
                xs.map { it % y }
            }
        }
    }

val ns2: State<RNG, List<Int>> =
    State.fx(Id.monad()) {
        val x: Int = int.bind()
        val y: Int = int.bind()
        val xs: List<Int> = ints(x).bind()
        xs.map { it % y }
    }

fun main() {
    println("=== Arrow's State ===")

    val rng = SimpleRNG(4)
    println("ns: " + ns.run(rng))
    println("ns2: " + ns2.run(rng))
}
