package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.Either

typealias TestCases = Int
typealias Result = Either<Pair<FailedCase, SuccessCount>, SuccessCount>
typealias SuccessCount = Int
typealias FailedCase = String

data class Prop(val check: (TestCases) -> Result)

fun <A> forAll(a: Gen<A>, f: (A) -> Boolean): Prop = TODO()
