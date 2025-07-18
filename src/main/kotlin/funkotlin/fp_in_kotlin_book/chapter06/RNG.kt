package funkotlin.fp_in_kotlin_book.chapter06

import arrow.core.Tuple2
import funkotlin.fp_in_kotlin_book.chapter02.Example.abs
import funkotlin.fp_in_kotlin_book.chapter06.RNG.Companion.nonNegativeInt
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter03.Nil as NilL
import funkotlin.fp_in_kotlin_book.chapter03.Cons as ConsL

typealias Rand<A> = State<RNG, A>

val intR: Rand<Int> = State { rng -> rng.nextInt() }
val nonNegativeEven: Rand<Int> = State { r: RNG -> nonNegativeInt().run(r) }.map() { it - (it % 2)}
val doubleR: Rand<Double> = State { r: RNG -> nonNegativeInt().run(r) }.map { i ->
    i / (Int.MAX_VALUE.toDouble() + 1)
}
val intDoubleR: Rand<Pair<Int, Double>> = State.both(intR, doubleR)
val doubleIntR: Rand<Pair<Double, Int>> = State.both(doubleR, intR)

//fun rollDie(): Rand<Int> = nonNegativeInt()

sealed interface RNG {
    fun nextInt(): Pair<Int, RNG>
    fun nextIntTup(): Tuple2<RNG, Int>

    companion object {
        // EXER 6.1
        fun nonNegativeInt(): Rand<Int> = State { rng ->
            val (n1, rng2) = rng.nextInt()
            if (n1 == Int.MIN_VALUE) nonNegativeInt().run(rng2) else Pair(abs(n1), rng2)
        }
        fun nonNegativeInt2(rng: RNG): Pair<Int, RNG>  {
            val (i1, rng2) = rng.nextInt()
            return (if (i1 < 0) -(i1 + 1) else i1) to rng2
        }

        // EXER 6.2
        fun double(rng: RNG): Pair<Double, RNG> {
            val (n1, rng2) = nonNegativeInt().run(rng)
            return Pair(n1.toDouble() / (Int.MAX_VALUE.toLong() + 1).toDouble(), rng2)
        }

        // EXER 6.5
        fun double2(): Rand<Double> =
            State { r: RNG -> nonNegativeInt().run(r) }.map { it -> it.toDouble() / (Int.MAX_VALUE.toLong() + 1).toDouble()}

        // EXER 6.3
        fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
            val (n1, rng2) = nonNegativeInt().run(rng)
            val (d2, rng3) = double(rng2)
            return Pair(Pair(n1, d2), rng3)
        }
        fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
            val (d1, rng2) = double(rng)
            val (n2, rng3) = nonNegativeInt().run(rng2)
            return Pair(Pair(d1, n2), rng3)
        }
        fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
            val (d1, rng2) = double(rng)
            val (d2, rng3) = double(rng2)
            val (d3, rng4) = double(rng3)
            return Pair(Triple(d1, d2, d3), rng4)
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

    override fun nextIntTup(): Tuple2<RNG, Int> {
        val (i, rng) = nextInt()
        return Tuple2(rng, i)
    }
}

fun randomPair2(rng: RNG): Pair<Pair<Int, Int>, RNG> {
    val (n1, rng2) = rng.nextInt()
    val (n2, rng3) = rng2.nextInt()
    return Pair(Pair(n1, n2), rng3)
}

