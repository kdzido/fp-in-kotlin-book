package funkotlin.fp_in_kotlin_book.chapter04

import funkotlin.fp_in_kotlin_book.chapter03.List

// listing 4.5
sealed class Either<out E, out A>
data class Left<out E>(val value: E) : Either<E, Nothing>()
data class Right<out A>(val value: A) : Either<Nothing, A>()

// Exercise 4.6
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> = when (this) {
    is Left -> this
    is Right -> Right(f(this.value))
}

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> = when (this) {
    is Left -> this
    is Right -> f(this.value)
}

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> = when (this) {
    is Left -> f()
    is Right -> this
}

fun <E, A, B, C> map2E(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C,
): Either<E, C> =
    ae.flatMap { a ->
        be.map { b ->
            f(a, b)
        }
    }

fun <A> catchesE(a: () -> A): Either<Exception, A> =
    try {
        Right(a())
    } catch (e: Exception) {
        Left(e)
    }

fun meanE(xs: List<Double>): Either<String, Double> =
    if (List.isEmpty(xs)) Left("Mean of empty list!") else Right(List.sum(xs) / List.size(xs))

fun safeDiv(x: Int, y: Int): Either<Exception, Int> =
    try {
        Right(x / y)
    } catch (e: Exception) {
        Left(e)
    }

fun safeDivC(x: Int, y: Int): Either<Exception, Int> = catchesE { x / y }


fun main() {
    print("Either")
}
