package funkotlin.fp_in_kotlin_book.chapter02

import kotlin.math.abs

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

// listing 2.2
object Example {
    fun formatAbs(x: Int): String =
        formatResult("absolute value", x, ::abs)

    fun formatFactorial(x: Int): String =
        formatResult("factorial", x, ::factorial)

    fun formatResult(name: String, n: Int, f: (Int) -> Int): String {
        val msg = "The %s of %d is %d"
        return msg.format(name, n, f(n))
    }

    fun abs(n: Int): Int =
        if (n < 0) -n else n

    fun factorial(n: Int): Int {
        fun go(n: Int, acc: Int): Int =
            if (n <= 0) acc else go(n-1, n*acc)
        return go(n, 1)
    }
}

fun main() {
    println("Chapter 02 ===")

    println("factorial(3)= " + factorial(3))

    // 0, 1, 1, 2, 3, 5, 8, 13, 21
    println("fib(0)= " + fib(0))        // 0
    println("fib(1)= " + fib(1))        // 0
    println("fib(2)= " + fib(2))        // 1
    println("fib(6)= " + fib(6))        // 5

    println(Example.formatAbs(-1))
    println(Example.formatFactorial(3))
    println(Example.formatResult("absolute value", -1, Example::abs))
    println(Example.formatResult("factorial", 3, Example::factorial))
}
