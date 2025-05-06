package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.getOrElse
import arrow.core.toOption
import funkotlin.fp_in_kotlin_book.chapter06.RNG
import kotlin.math.min

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
typealias MaxSize = Int

data class Prop(val check: (MaxSize, TestCases, RNG) -> Result) {
    companion object {
        fun <A> forAll(g: SGen<A>, f: (A) -> Boolean): Prop =
            forAll({ i -> g(i) }, f)

        fun <A> forAll(g: (Int) -> Gen<A>, f: (A) -> Boolean): Prop =
            Prop { max, n, rng ->
                val casePerSize: Int = (n + (max - 1)) / max

                val props: Sequence<Prop> =
                    generateSequence(0) { it + 1 }
                        .take(min(n, max) + 1)
                        .map { i -> forAll(g(i), f) }

                val prop: Prop = props.map { p ->
                    Prop { max, _, rng ->
                        p.check(max, casePerSize, rng)
                    }
                }.reduce { p1, p2 -> p1.and(p2) }

                prop.check(max, n, rng)
            }

        fun <A> forAll(ga: Gen<A>, f: (A) -> Boolean): Prop =
            Prop { max, n, rng ->
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
    }

    fun and(other: Prop): Prop = Prop { max, n, rng ->
        when (val p1 = check(max, n, rng)) {
            is Falsified -> p1
            Passed -> other.check(max, n, rng)
        }
    }

    fun or(other: Prop): Prop = Prop { max, n, rng ->
        when (val p1 = check(max, n, rng)) {
            is Passed -> p1
            is Falsified -> other.check(max, n, rng)
        }
    }
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
