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

// listing 2.2
object Example {
    private fun abs(n: Int): Int =
        if (n < 0) -n else n

    private fun factorial(n: Int): Int {
        fun go(n: Int, acc: Int): Int =
            if (n <= 0) acc else go(n-1, n*acc)
        return go(n, 1)
    }

    fun formatAbs(x: Int): String {
        val msg = "The absolute value of %d is %d"
        return msg.format(x, abs(x))
    }

    fun formatFactorial(x: Int): String {
        val msg = "The factorial of %d is %d"
        return msg.format(x, factorial(x))
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
}
