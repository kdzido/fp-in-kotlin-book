package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.getOrElse
import arrow.core.toOption
import funkotlin.fp_in_kotlin_book.chapter06.RNG

sealed class Result {
    abstract fun isFalsified(): Boolean
}

object Passed : Result() {
    override fun isFalsified(): Boolean = false
}

data class Falsified(
    val failure: FailedCase,
    val successes: SuccessCount,
) : Result() {
    override fun isFalsified(): Boolean = true
}

typealias SuccessCount = Int
typealias FailedCase = String
typealias TestCases = Int

data class Prop(val check: (TestCases, RNG) -> Result) {
    fun and(other: Prop): Prop = Prop { n: TestCases, rng: RNG ->
        when (val p1 = check(n, rng)) {
            is Falsified -> p1
            Passed -> other.check(n, rng)
        }
    }

    fun or(other: Prop): Prop = Prop { n: TestCases, rng: RNG ->
        when (val p1 = check(n, rng)) {
            is Passed -> p1
            is Falsified -> other.check(n, rng)
        }
    }
}

fun <A> forAll(ga: Gen<A>, f: (A) -> Boolean): Prop =
    Prop { n: TestCases, rng: RNG ->
        randomSequence(ga, rng).mapIndexed { i, a ->
            try {
                if (f(a)) Passed
                else Falsified(a.toString(), i)
            } catch (e: Exception) {
                Falsified(buildMessage(a, e), i)
            }
        }.take(n)
            .find { it.isFalsified() }
            .toOption()
            .getOrElse { Passed }
    }

private fun <A> randomSequence(ga: Gen<A>, rng: RNG): Sequence<A> = sequence {
    val (a: A, rng2: RNG) = ga.sample.run(rng)
    yield(a)
    yieldAll(randomSequence(ga, rng2))
}

private fun <A> buildMessage(a: A, e: Exception) =
    """
    |test case: $a
    |generated and exception: ${e.message}
    |stacktrace: 
    |${e.stackTrace.joinToString("\n")}    
    """.trimMargin()
