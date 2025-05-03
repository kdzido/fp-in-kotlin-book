package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.Either
import funkotlin.fp_in_kotlin_book.chapter06.RNG
import funkotlin.fp_in_kotlin_book.chapter06.State
import kotlin.math.absoluteValue

data class Gen<A>(val sample: State<RNG, A>) {

    fun <B> flatMap(f: (A) -> Gen<B>): Gen<B> = Gen(State({ rng ->
        val (a2, rng2) = sample.run(rng)
        f(a2).sample.run(rng2)
    }))

    companion object {
        fun <A> unit(a: A): Gen<A> = Gen(
            State({ rng ->
                Pair(a, rng)
            })
        )

        fun <A> union(ga: Gen<A>, gb: Gen<A>): Gen<A> = boolean().flatMap { b ->
            when (b) {
                true -> ga
                false -> gb
            }
        }

        fun <A> weighted(gap: Pair<Gen<A>, Double>, gbp: Pair<Gen<A>, Double>): Gen<A> = choose(0, 100).flatMap { prob ->
            val aProb: Int = (gap.second * 100).toInt()
            val bProb: Int = (gbp.second * 100).toInt()
            if (aProb + bProb != 100) throw IllegalArgumentException("Both probabilities (a=$aProb, b=$bProb) should add up to 100.")
            when (prob) {
                in 0..aProb -> gap.first
                else -> gbp.first
            }
        }

        fun boolean(): Gen<Boolean> = Gen(
            State({ rng ->
                val (n2, rng2) = rng.nextInt()
                val b = n2 % 2 == 0
                Pair(b, rng2)
            })
        )

        fun <A> listOfN(gn: Gen<Int>, ga: Gen<A>): Gen<List<A>> = gn.flatMap { n ->
            listOfSpecifiedN(n, ga)
        }

        fun <A> listOfSpecifiedN(n: Int, ga: Gen<A>): Gen<List<A>> = Gen(
            State({ rng ->
                val resultList = mutableListOf<A>()
                var curRng = rng
                for (i in 0 until n) {
                    val (g2, rng2) = ga.sample.run(curRng)
                    resultList.add(g2)
                    curRng = rng2
                }
                Pair(resultList, curRng)
            })
        )

        fun choose(start: Int, stopExclusive: Int): Gen<Int> =
            Gen(
                State({ rng ->
                    val (n2, rng2) = rng.nextInt()
                    val delta = (stopExclusive - start)
                    val nInRange = n2.absoluteValue % delta
                    Pair(start + nInRange, rng2)
                })
            )

        fun choosePair(start: Int, stopExclusive: Int): Gen<Pair<Int, Int>> = Gen(
            State({ rng ->
                    val (g1, rng2) = choose(start, stopExclusive).sample.run(rng)
                    val (g2, rng3) = choose(start, stopExclusive).sample.run(rng2)
                Pair(Pair(g1, g2), rng3)
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

fun <A> forAll(a: Gen<A>, f: (A) -> Boolean): Prop = TODO()
