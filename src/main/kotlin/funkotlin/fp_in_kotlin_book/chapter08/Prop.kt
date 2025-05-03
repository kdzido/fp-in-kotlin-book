package funkotlin.fp_in_kotlin_book.chapter08

//typealias Result = Either<Pair<FailedCase, SuccessCount>, SuccessCount>
//typealias Result = Option<Pair<FailedCase, SuccessCount>>

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

data class Prop(val check: (TestCases) -> Result)
typealias TestCases = Int

fun <A> forAll(a: Gen<A>, f: (A) -> Boolean): Prop = TODO()
