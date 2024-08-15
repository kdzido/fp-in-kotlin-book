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
        else go(acc2, acc1 + acc2, nn + 1)
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

// Listing 2.3 Monomorphic function to find a string in an array
fun findFirst(ss: Array<String>, key: String): Int {
    tailrec fun loop(n: Int, ): Int =
        when {
            n >= ss.size -> -1
            ss[n] == key -> n
            else -> loop(n + 1)
        }
    return loop(0)
}

// Listing 2.4 Polymorphic function to find an element in an array
fun <A> findFirst(xs: Array<A>, p: (A) -> Boolean): Int {
    tailrec fun loop(n: Int): Int =
        when {
            n >= xs.size -> -1
            p(xs[n]) -> n
            else -> loop(n + 1)
        }
    return loop(0)
}

val <T> List<T>.head: T
    get() = first()

val <T> List<T>.tail: List<T>
    get() = drop(1)

// Exercise 2-2, implement isSorted
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(h: A, ts: List<A>, sortedAcc: Boolean): Boolean {
        return when {
            !sortedAcc -> false
            ts.isEmpty() -> sortedAcc
            else -> go(ts.head, ts.tail, sortedAcc && order(h, ts.head))
        }
    }
    return go(aa.head, aa.tail, true)
}

fun <A, B, C> partial1(a: A, f: (A, B) -> C): (B) -> C =
    { b: B -> f(a, b) }

// Exercise 2-3, implement curry
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C =
    { a -> { b -> f(a, b) } }

// Exercise 2-4, implement uncurry
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
    { a, b -> f(a)(b) }

// Exercise 2-5, implement compose
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
    { a -> f(g(a)) }

fun <A, B, C> leftAssociativeFunSignature(f: ((A) -> (B)) -> C): (A, B) -> C =
    { a, b -> f({ b}) } // TODO does it make any sense?


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

    println(Example.formatResult("absolute value", -1,
        fun(n: Int): Int = if (n < 0) -n else n))   // anonymous function
    println(Example.formatResult("absolute value", -1,
        { n -> if (n < 0) -n else n }))             // lambda
    println(Example.formatResult("absolute value", -1,
        { if (it < 0) -it else it }))               // lambda

}
