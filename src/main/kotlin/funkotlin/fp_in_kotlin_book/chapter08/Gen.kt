package funkotlin.fp_in_kotlin_book.chapter08

import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter06.RNG
import funkotlin.fp_in_kotlin_book.chapter06.RNG.Companion.double2
import funkotlin.fp_in_kotlin_book.chapter06.RNG.Companion.ints
import funkotlin.fp_in_kotlin_book.chapter06.RNG.Companion.nonNegativeInt2
import funkotlin.fp_in_kotlin_book.chapter06.State

data class Gen<A>(val sample: State<RNG, A>) : GenOf<A> {
    fun unsized(): SGen<A> = SGen({ n: Int -> Gen(sample)})

    fun <B> flatMap(f: (A) -> Gen<B>): Gen<B> = Gen(State({ rng ->
        val (a2, rng2) = sample.run(rng)
        f(a2).sample.run(rng2)
    }))

    fun <B> map(f: (A) -> B): Gen<B> = flatMap { unit(f(it)) }

    fun listOf(): SGen<List<A>> = SGen({ n ->
        listOfSpecifiedN(n, Gen(sample))
    })

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
                State { rng: RNG -> nonNegativeInt2(rng) }
                    .map { start + (it % (stopExclusive - start)) }
            )

        fun double(r: IntRange): Gen<Double> =
            Gen(double2()).map { d: Double -> r.first.toDouble() + (d * r.last.toDouble()) }

        fun string(): Gen<String> = Gen(State { rng: RNG ->
            ints(10, rng)
        }).map { lis: ListL<Int> ->
            val l: ListL<Char> = ListL.map(lis) { it: Int -> it.digitToChar() }
            ListL.foldRight2(l, "") { x, acc -> x + acc }
        }

        fun choosePair(start: Int, stopExclusive: Int): Gen<Pair<Int, Int>> = Gen(
            State({ rng ->
                val (g1, rng2) = choose(start, stopExclusive).sample.run(rng)
                val (g2, rng3) = choose(start, stopExclusive).sample.run(rng2)
                Pair(Pair(g1, g2), rng3)
            })
        )
    }
}
