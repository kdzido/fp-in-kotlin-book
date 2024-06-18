package funkotlin.fp_in_kotlin_book.chapter02

// listing 2.1
fun factorial(i: Int): Int {
    tailrec fun go(n: Int, acc: Int): Int {
        return if (n <= 0) acc
        else go(n - 1, n * acc)
    }
    return go(i, 1)
}

// Exercise 2.1
fun fib(n: Int): Int {
    fun go(acc1: Int, acc2: Int, nn: Int): Int {
       return if (nn >= n) acc1
        else go(acc2, acc1 +  acc2, nn + 1)
    }
   return go(0,1, 1)
}

fun main() {
    println("Chapter 02 ===")

    println("factorial(3)= " + factorial(3))

    // 0, 1, 1, 2, 3, 5, 8, 13, 21
    println("fib(0)= " + fib(0))        // 0
    println("fib(1)= " + fib(1))        // 0
    println("fib(2)= " + fib(2))        // 1
    println("fib(3)= " + fib(3))        // 1
    println("fib(4)= " + fib(4))        // 2
    println("fib(5)= " + fib(5))        // 3
    println("fib(6)= " + fib(6))        // 5
}
