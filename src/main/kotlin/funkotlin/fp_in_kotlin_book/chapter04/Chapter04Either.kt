package funkotlin.fp_in_kotlin_book.chapter04

import arrow.core.raise.either
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Nil

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

// Exercise 4.8
fun <E, A, B, C> map2E_2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C,
): Either<List<E>, C> = when {
    ae is Left<E> && be is Left<E> -> Left(List.of(ae.value, be.value))
    ae is Right<A> && be is Left<E> -> Left(List.of(be.value))
    ae is Left<E> && be is Right<B> -> Left(List.of(ae.value))
    ae is Right<A> && be is Right<B> -> Right(f(ae.value, be.value))
    else -> Left(List.of())
}

// Exercise 4.7
fun <E, A> sequenceE(xs: List<Either<E, A>>): Either<E, List<A>> =
    traverseE(xs, { e -> e })

// Exercise 4.7
fun <E, A, B> traverseE(xs: List<A>, f: (A) -> Either<E, B>): Either<E, List<B>> {
    tailrec fun go(ls: List<Either<E, B>>, acc: Either<E, List<B>>): Either<E, List<B>> = when (ls) {
        is Nil -> acc
        is Cons -> {
            when (ls.head) {
                is Left -> Left(ls.head.value)
                is Right -> go(ls.tail, acc.map { l -> Cons<B>(ls.head.value, l) })
            }
        }
    }

    tailrec fun reverseLoop(ls: List<B>, acc: List<B>): List<B> = when (ls) {
        is Nil -> acc
        is Cons -> reverseLoop(ls.tail, Cons(ls.head, acc))
    }

    val les: List<Either<E, B>> = List.map(xs, f)
    val transformed: Either<E, List<B>> = go(les, Right(List.of()))
    return transformed.map { l -> reverseLoop(l, List.of()) }
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

// Listing
suspend fun parseInsuranceRateQuoteE(
    age: String,
    numberOfSpeedingTickets: String
): arrow.core.Either<Throwable, Double> {
    val ae = age.parseToInt()
    val te = numberOfSpeedingTickets.parseToInt()
    return either {
        val a = ae.bind()
        val t = te.bind()
        insuranceRateQuote(a, t)
    }
}

suspend fun String.parseToInt(): arrow.core.Either<Throwable, Int> =
    arrow.core.Either.catch { this.toInt() }


data class Name(val value: String)
data class Age(val value: Int)
data class Person(val name: Name, val age: Age)

fun mkName(name: String): Either<String, Name> =
    if (name.isBlank()) Left("Name is empty")
    else Right(Name(name))

fun mkAge(age: Int): Either<String, Age> =
    if (age < 0) Left("Age is out of range")
    else Right(Age(age))

fun mkPerson(name: String, age: Int): Either<String, Person> =
    map2E(mkName(name), mkAge(age), {n, a -> Person(n, a) })

fun main() {
    println("Either - main")

    println(mkPerson("John", 10))
    println(mkPerson("John", -1))
    println(mkPerson("", 10))
}
