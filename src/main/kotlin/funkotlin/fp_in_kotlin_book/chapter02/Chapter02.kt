package funkotlin.fp_in_kotlin_book.chapter02

// listing 2.1
fun factorial(i: Int): Int {
    fun go(n: Int, acc: Int): Int {
        if (n <= 0) return acc
        else return go(n - 1, n * acc)
    }
    return go(i, 1)
}

fun main() {
    println("Chapter 02")
    println("===")

    println("factorial(1)= " + factorial(1))
    println("factorial(3)= " + factorial(3))
}
