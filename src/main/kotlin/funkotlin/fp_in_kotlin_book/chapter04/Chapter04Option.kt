package funkotlin.fp_in_kotlin_book.chapter04

import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import kotlin.math.pow

sealed class Option<out A> : OptionOf<A>
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

// Listing
fun insuranceRateQuote(age: Int, numberOfSpeedingTickets: Int): Double = TODO()
fun parseInsuranceRateQuote(age: String, speedingTickets: String): Option<Double> {
    val optAge = catches { age.toInt() }
    val optTickets = catches { speedingTickets.toInt() }
    return map2(optAge, optTickets, { a, t ->
        insuranceRateQuote(a, t)
    })
}

fun <A> catches(a: () -> A): Option<A> =
    try {
        Some(a())
    } catch (e: Throwable) {
        None
    }

// Exercise 4.3
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
    a.flatMap { oa ->
        b.map { ob ->
            f(oa, ob)
        }
    }

fun <A, B, C, D> map3(a: Option<A>, b: Option<B>, c: Option<C>, f: (A, B, C) -> D): Option<D> =
    a.flatMap { oa ->
        b.flatMap { ob ->
            c.map { oc ->
                f(oa, ob, oc)
            }
        }
    }

// Exercise 4.4
fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    List.foldRight2(xs, Some(List.of()), { e: Option<A>, ol: Option<List<A>> ->
        map2(ol, e, { x: List<A>, y: A -> Cons(y, x) })
    })

fun <A> sequence2(xs: List<Option<A>>): Option<List<A>> =
    traverse2(xs, {oa -> oa})

// Listing
fun parseInts(xs: List<String>): Option<List<Int>> = sequence(List.map(xs, { catches { it.toInt() } }))

// Exercise 4.5
fun <A, B> traverse(xs: List<A>, f: (A) -> Option<B>): Option<List<B>> =
    sequence(List.map(xs, { f(it) }))

// Exercise 4.5 - more efficient
fun <A, B> traverse2(xs: List<A>, f: (A) -> Option<B>): Option<List<B>> =
    List.foldRight2(xs, Some(List.of()), { e: A, ol: Option<List<B>> ->
        map2(ol, f(e), { x: List<B>, y: B -> Cons(y, x) })
    })

fun main4() {
    println(failingFn2(1))
    mean(Nil)
}
