package funkotlin.fp_in_kotlin_book.chapter04

sealed class Option<out A>
data class Some<A>(val value: A) : Option<A>()
data object None : Option<Nothing>()


// Exercise 4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is None -> None
    is Some<A> -> Some<B>(f(value))
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
    if (xs.isEmpty())
        throw ArithmeticException("mean of empty list")
    else xs.sum() / xs.size

// listing 4.2
fun meanO(xs: List<Double>): Option<Double> =
    if (xs.isEmpty())
       None
    else Some(xs.sum() / xs.size)

fun mean(xs: List<Double>, onEmpty: Double): Double =
    if (xs.isEmpty()) onEmpty else xs.sum() / xs.size

fun main() {
    println(failingFn2(1))
    mean(listOf())
}
