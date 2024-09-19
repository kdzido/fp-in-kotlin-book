package funkotlin.fp_in_kotlin_book.chapter04

sealed class Option<out A>
data class Some<A>(val value: A) : Option<A>()
object None : Option<Nothing>()


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

fun mean(xs: List<Double>, onEmpty: Double): Double =
    if (xs.isEmpty()) onEmpty else xs.sum() / xs.size

fun main() {
    println(failingFn2(1))
    mean(listOf())
}
