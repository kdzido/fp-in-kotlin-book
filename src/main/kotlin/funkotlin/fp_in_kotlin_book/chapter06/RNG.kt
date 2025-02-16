package funkotlin.fp_in_kotlin_book.chapter06

import funkotlin.fp_in_kotlin_book.chapter02.Example.abs

sealed interface RNG {
    fun nextInt(): Pair<Int, RNG>
    fun nonNegativeInt(rng: RNG): Pair<Int, RNG>
}

// LST 6.4
data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return n to nextRNG
    }

    // EXER 6.1
    override fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
        val (n1, rng2) = rng.nextInt()
        return if (n1 == Int.MIN_VALUE) nonNegativeInt(rng2) else Pair(abs(n1), rng2)
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
