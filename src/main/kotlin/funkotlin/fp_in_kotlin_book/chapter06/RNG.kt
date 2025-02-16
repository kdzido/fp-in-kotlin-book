package funkotlin.fp_in_kotlin_book.chapter06

import funkotlin.fp_in_kotlin_book.chapter02.Example.abs
import funkotlin.fp_in_kotlin_book.chapter06.RNG.Companion.nonNegativeInt
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter03.Nil as NilL
import funkotlin.fp_in_kotlin_book.chapter03.Cons as ConsL

// fns of this type are called state actions of state transitions
typealias Rand<A> = (RNG) -> Pair<A, RNG>

val intR: Rand<Int> = { rng -> rng.nextInt() }
fun nonNegativeEven(): Rand<Int> = RNG.map(::nonNegativeInt) { it - (it % 2)}
val doubleR: Rand<Double> = RNG.map(::nonNegativeInt) { i ->
    i / (Int.MAX_VALUE.toDouble() + 1)
}
val intDoubleR: Rand<Pair<Int, Double>> = RNG.both(intR, doubleR)
val doubleIntR: Rand<Pair<Double, Int>> = RNG.both(doubleR, intR)

sealed interface RNG {
    fun nextInt(): Pair<Int, RNG>

    companion object {
        fun <A> unit(a: A): Rand<A> = { rng -> a to rng }

        fun <A, B> map(s : Rand<A>, f: (A) -> B): Rand<B> = { rng ->
            val (a, rng2) = s(rng)
            f(a) to rng2
        }

        // EXER 6.6
        fun <A, B, C> map2(ra : Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> = { rng ->
            val (a, rng2) = ra(rng)
            val (b, rng3) = rb(rng)
            f(a, b) to rng3
        }

        fun <A, B> both(ra: Rand<A>, rb: Rand<B>): Rand<Pair<A, B>> =
            map2(ra, rb) { a, b -> a to b}

        // EXER 6.1
        fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
            val (n1, rng2) = rng.nextInt()
            return if (n1 == Int.MIN_VALUE) nonNegativeInt(rng2) else Pair(abs(n1), rng2)
        }

        // EXER 6.2
        fun double(rng: RNG): Pair<Double, RNG> {
            val (n1, rng2) = nonNegativeInt(rng)
            return Pair(n1.toDouble() / (Int.MAX_VALUE.toLong() + 1).toDouble(), rng2)
        }

        // EXER 6.5
        fun double2(): Rand<Double> =
            RNG.map(::nonNegativeInt) { it -> it.toDouble() / (Int.MAX_VALUE.toLong() + 1).toDouble()}

        // EXER 6.3
        fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
            val (n1, rng2) = nonNegativeInt(rng)
            val (d2, rng3) = double(rng2)
            return Pair(Pair(n1, d2), rng3)
        }
        fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
            val (d1, rng2) = double(rng)
            val (n2, rng3) = nonNegativeInt(rng2)
            return Pair(Pair(d1, n2), rng3)
        }
        fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
            val (d1, rng2) = double(rng)
            val (d2, rng3) = double(rng)
            val (d3, rng4) = double(rng)
            return Pair(Triple(d1, d2, d3), rng3)
        }

        // EXER 6.4
        fun ints(n: Int, rng: RNG): Pair<ListL<Int>, RNG> {
            fun go(j: Int, r: RNG, acc: ListL<Int>): Pair<ListL<Int>, RNG> {
                return if (j == 0)
                    Pair(acc, r)
                else {
                    val (n1, r2) = r.nextInt()
                    go(j-1, r2, ConsL(n1, acc))
                }
            }
            val reversed = go(n, rng, NilL)
            val inOrder = ListL.foldLeft(reversed.first, NilL as ListL<Int>, { i, a -> ConsL(a, i)})
            return Pair(inOrder, reversed.second)
        }

    }
}

// LST 6.4
data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return n to nextRNG
    }
}

fun randomPair2(rng: RNG): Pair<Pair<Int, Int>, RNG> {
    val (n1, rng2) = rng.nextInt()
    val (n2, rng3) = rng2.nextInt()
    return Pair(Pair(n1, n2), rng3)
}

fun main() {
    val rng = SimpleRNG(1)
    println("RNG.nextInt:" + rng.nextInt())
    println("RNG.nextInt:" + rng.nextInt().second.nextInt())

}
