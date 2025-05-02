package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.Either
import kotlin.math.absoluteValue

interface RNG {
    fun nextInt(): Pair<Int, RNG>
}
data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return n to nextRNG
    }
}

data class State<S, out A>(val run: (S) -> Pair<A, S>) {
    companion object {
        fun <S, A> unit(a: A): State<S, A> =
            State { rng -> a to rng }
    }
}

data class Gen<A>(val sample: State<RNG, A>) {

    companion object {
        fun choose(start: Int, stopExclusive: Int): Gen<Int> =
            Gen(
                State({ rng ->
                    val (n2, rng2) = rng.nextInt()
                    val delta = (stopExclusive - start)
                    val nInRange = n2.absoluteValue % delta
                    Pair(start + nInRange, rng2)
                })
            )
    }
}


typealias SuccessCount = Int
typealias FailedCase = String

interface Prop {
    fun check(): Either<Pair<FailedCase, SuccessCount>, SuccessCount>

    fun add(p: Prop): Prop {
        val outer = this
        return object : Prop {
            override fun check(): Either<Pair<FailedCase, SuccessCount>, SuccessCount> {
                val p1 = outer.check()
                val p2 = p.check()
                return when {
                    p1 is Either.Right && p2 is Either.Right -> Either.right(2)
                    p1 is Either.Left && p2 is Either.Right -> Either.left(Pair(p1.a.first, 1))
                    p1 is Either.Right && p2 is Either.Left -> Either.left(Pair(p2.a.first, 1))
                    p1 is Either.Left && p2 is Either.Left -> Either.left(Pair(p1.a.first + p1.a.first, 0))
                    else -> throw IllegalStateException("Not possible")
                }
            }
        }
    }
}

fun <A> listOf(a: Gen<A>): List<Gen<A>> = TODO()

fun <A> listOfN(n: Int, a: Gen<A>): List<Gen<A>> = TODO()

fun <A> forAll(a: Gen<A>, f: (A) -> Boolean): Prop = TODO()
