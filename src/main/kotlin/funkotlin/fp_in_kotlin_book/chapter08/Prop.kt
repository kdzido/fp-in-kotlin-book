package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.Either

interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

data class State<S, out A>(val run: (S) -> Pair<A, S>)

data class Gen<A>(val sample: State<RNG, A>)

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
