package funkotlin.fp_in_kotlin_book.chapter06

import funkotlin.fp_in_kotlin_book.chapter02.Example.abs
import funkotlin.fp_in_kotlin_book.chapter06.RNG.Companion.nonNegativeInt
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter03.Nil as NilL
import funkotlin.fp_in_kotlin_book.chapter03.Cons as ConsL

// fns of this type are called state actions of state transitions
typealias Rand<A> = (RNG) -> Pair<A, RNG>
typealias State<S, A> = (S) -> Pair<A, S>

val intR: Rand<Int> = { rng -> rng.nextInt() }
fun nonNegativeEven(): Rand<Int> = RNG.map(::nonNegativeInt) { it - (it % 2)}
val doubleR: Rand<Double> = RNG.map(::nonNegativeInt) { i ->
    i / (Int.MAX_VALUE.toDouble() + 1)
}
val intDoubleR: Rand<Pair<Int, Double>> = RNG.both(intR, doubleR)
val doubleIntR: Rand<Pair<Double, Int>> = RNG.both(doubleR, intR)

//fun rollDie(): Rand<Int> = nonNegativeInt()

sealed interface RNG {
    fun nextInt(): Pair<Int, RNG>

    companion object {
        fun <S, A> unit(a: A): State<S, A> = { rng -> a to rng }

        // EXER 6.8
        fun <S, A, B> flatMap(s: State<S, A>, f: (A) -> State<S, B>): State<S, B> = { rng ->
            val (s1, r2) = s(rng)
            f(s1)(r2)
        }

        // EXER 6.5, 6.9
        fun <S, A, B> map(sa : State<S, A>, f: (A) -> B): State<S, B> =
            flatMap(sa) { a -> RNG.unit(f(a)) }

        // EXER 6.6, 6.9
        fun <S, A, B, C> map2(ra : State<S, A>, rb: State<S, B>, f: (A, B) -> C): State<S, C> =
            flatMap(ra) { a ->
                flatMap(rb) { b ->
                    RNG.unit(f(a, b))
                }
            }

        fun <S, A, B> both(ra: State<S, A>, rb: State<S, B>): State<S, Pair<A, B>> =
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

        // EXER 6.7
        fun <A> sequence(fs: ListL<Rand<A>>): Rand<ListL<A>> = { rng ->
            fun go(l: ListL<Rand<A>>, acc: ListL<A>, r: RNG): Pair<ListL<A>, RNG> {
                return when (l) {
                    is NilL -> Pair(acc, r)
                    is ConsL -> {
                        val (h2: A, r2: RNG) = l.head(r)
                        go(l.tail, ConsL(h2, acc), r2)
                    }
                }
            }

            val (l2: ListL<A>, rng2: RNG) = go(fs, NilL, rng)
            val inOrder = ListL.foldLeft(l2, NilL as ListL<A>, { a, b -> ConsL(b, a)})

            Pair(inOrder, rng2)
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
