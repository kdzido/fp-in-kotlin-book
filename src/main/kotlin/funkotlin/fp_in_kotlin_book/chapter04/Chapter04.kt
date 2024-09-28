package funkotlin.fp_in_kotlin_book.chapter04

import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import kotlin.math.pow

sealed class Option<out A>
data class Some<A>(val value: A) : Option<A>()
data object None : Option<Nothing>()


// Exercise 4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is None -> None
    is Some<A> -> Some(f(value))
}

// Exercise 4.1
fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
    is None -> None
    is Some<A> -> f(value)
}

// Exercise 4.1
fun <A> Option<A>.getOrElse(default: () -> A): A = when (this) {
    is None -> default()
    is Some<A> -> value
}

// Exercise 4.1
fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> = when (this) {
    is None -> ob()
    is Some<A> -> this
}

// Exercise 4.1
fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> = when(this) {
    is None -> None
    is Some<A> -> if (f(value)) this else None
}


// Listing 4.1 - y is non-RT
fun failingFn(i: Int): Int {
    val y: Int = throw Exception("boom")
    return try {
        val x = 42 + 5
        x + y
    } catch (e: Exception) {
        42
    }
}
// Listing 4.2
fun failingFn2(i: Int): Int {
    return try {
        val x = 42 + 5
        x + (throw Exception("boom")) as Int
    } catch (e: Exception) {
        42
    }
}

// partial function
fun mean(xs: List<Double>): Double =
    if (List.isEmpty(xs))
        throw ArithmeticException("mean of empty list")
    else List.sum(xs) / List.size(xs)

// listing 4.2
fun meanO(xs: List<Double>): Option<Double> =
    if (List.isEmpty(xs))
       None
    else Some(List.sum(xs) / List.size(xs))

fun mean(xs: List<Double>, onEmpty: Double): Double =
    if (List.isEmpty(xs)) onEmpty else List.sum(xs) / List.size(xs)


// Exercise 4.2
fun variance(xs: List<Double>): Option<Double> = Some(xs).flatMap { os: List<Double> ->
    when (os) {
        is Nil -> None
        is Cons -> Some(os)
    }
}
    .flatMap { ls: Cons<Double> -> meanO(ls).flatMap { listMean -> Some(Pair(ls, listMean)) } }
    .map { listAndMean: Pair<Cons<Double>, Double> ->
        mean(List.map(listAndMean.first, { x -> (x - listAndMean.second).pow(2) }))
    }

// Listing 4.4
fun <A, B> lift(f: (A) -> B): (Option<A>) -> Option<B> =
    { op -> op.map(f) }

val absO = lift<Double, Double> {kotlin.math.abs(it)}

fun main() {
    println(failingFn2(1))
    mean(Nil)
}
