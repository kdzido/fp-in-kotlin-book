package funkotlin.fp_in_kotlin_book.chapter03

sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        fun sum(ints: List<Int>): Int =
            when(ints) {
                is Nil -> 0
                is Cons -> ints.head + sum(ints.tail)
            }

        fun product(doubles: List<Double>): Double =
            when(doubles) {
                is Nil -> 1.0
                is Cons ->
                    if (doubles.head == 0.0) 0.0 else doubles.head * product(doubles.tail)
            }

        // Exercise 3.1
        fun <A> tail(xs: List<A>): List<A> = when (xs) {
            is Nil -> Nil
            is Cons -> xs.tail
        }

        fun <A> setHead(xs: List<A>, h: A): List<A> = when (xs) {
            is Nil -> Cons(h, Nil)
            is Cons -> Cons(h, xs.tail)
        }

        // Exercise 3.3
        tailrec fun <A> drop(xs: List<A>, n: Int): List<A> = when (xs) {
            is Nil -> Nil
            is Cons -> if (n == 0) xs else drop(xs.tail, n-1)
        }

        // Exercise 3.4
        tailrec fun <A> dropWhile(xs: List<A>, f: (A) -> Boolean): List<A> = when (xs) {
            is Nil -> Nil
            is Cons -> if (!f(xs.head)) xs else dropWhile(xs.tail, f)
        }

        // Listing 3.9
        fun <A> append(l1: List<A>, l2: List<A>): List<A> = when(l1) {
            is Nil -> l2
            is Cons -> Cons(l1. head, append(l1.tail, l2))
        }

        // Exercise 3.5, everything except last elem
        fun <A> init(xs: List<A>): List<A> {
            tailrec fun go(xss: List<A>, acc: List<A>): List<A> =
                when (xss) {
                    is Nil -> acc
                    is Cons -> if (xss.tail == Nil) acc else go(xss.tail, Cons(xss.head, acc) )
                }

            tailrec fun reverse(xss: List<A>, acc: List<A>): List<A> =
                when (xss) {
                    is Nil -> acc
                    is Cons -> reverse(xss.tail, Cons(xss.head, acc) )
                }
            return reverse(go(xs, Nil), Nil)
        }
    }
}

object Nil : List<Nothing>() {
    override fun toString(): String {
        return "Nil"
    }
}
data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()


fun main() {
    println("Ch03 - Functional data structures")

    val ex1: List<Double> = Nil
    val ex2: List<Int> = Cons(1, Nil)
    val ex3: List<String> = Cons("a", Cons("b", Nil))

    println("l1: " + ex1)
    println("l2: " + ex2)
    println("l3: " + ex3)
    println("of: " + List.of(1,2,3))
}
