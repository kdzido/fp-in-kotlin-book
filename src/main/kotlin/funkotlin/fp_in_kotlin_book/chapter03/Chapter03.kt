package funkotlin.fp_in_kotlin_book.chapter03

sealed class List<out A> : ListOf<A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        fun <A> empty(): List<A> = Nil

        fun sum(ints: List<Int>): Int =
            foldLeft(ints, 0, { x, y -> x + y })

        fun sum(ds: List<Double>): Double =
            foldLeft(ds, 0.0, { x, y -> x + y })

        fun product(doubles: List<Double>): Double =
            foldLeft(doubles, 1.0, { x, y -> x * y })

        fun sum2(ints: List<Int>): Int =
           foldRight(ints, 0, { x, y -> x + y })

        fun product2(doubles: List<Double>): Double =
            foldRight(doubles, 1.0, {x, y -> x * y})

        // Listing 3.11
        fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }

        // Listing 3.12
        tailrec fun <A, B> foldRight2(xs: List<A>, z: B, f: (A, B) -> B): B  {
           val reversed = foldLeft(xs, Nil as List<A>, { x, y -> Cons(y, x)})
            return foldLeft(reversed, z, { b, a -> f(a, b)})
        }

        // Exercise 3.9
        tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
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

        // Listing 3.13
        tailrec fun <A> append2(l1: List<A>, l2: List<A>): List<A> =
            foldRight2(l1, l2, { x, y -> Cons(x, y)})

        // Listing 3.14
        tailrec fun <Int> concat(ls: List<List<Int>>): List<Int> =
            foldRight2(ls, Nil as List<Int>, { xs, x -> append(xs, x)})

        // Exercise 3.15, 3.16
        tailrec fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
            List.foldRight2(xs, Nil as List<B>, { x, y -> Cons(f(x), y) })

        // Exercise 3.19
        tailrec fun <A, B> flatMap(xs: List<A>, f: (A) -> List<B>): List<B> =
            foldRight2(xs, Nil as List<B>, { a, bs -> append(f(a), bs)})

        // Exercise 3.18
        fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
            foldRight2(xs, Nil as List<A>, { x, y -> if (f(x)) Cons(x, y) else y })

        // Exercise 3.20
        fun <A> filter2(xs: List<A>, f: (A) -> Boolean): List<A> =
            flatMap(xs, {a -> if (f(a)) List.of(a) else Nil })

        // Exercise 3.21
        fun sumLists(xs: List<Int>, ys: List<Int>): List<Int> {
            fun go(l1: List<Int>, l2: List<Int>, acc: List<Int>): List<Int> = when(l1) {
                is Nil -> acc
                is Cons ->
                    when (l2) {
                    is Nil -> acc
                    is Cons -> go(l1.tail, l2.tail, Cons(l1.head + l2.head, acc))
                }
            }
            val reversed = go(xs, ys, Nil)
            return foldLeft(reversed, Nil as List<Int>, { ls, l -> Cons(l, ls)})
        }

        // Exercise 3.22
        fun <A, B> zipWith(xs: List<A>, ys: List<A>, f: (A, A) -> B): List<B> {
            fun go(l1: List<A>, l2: List<A>, acc: List<B>): List<B> = when(l1) {
                is Nil -> acc
                is Cons ->
                    when (l2) {
                        is Nil -> acc
                        is Cons -> go(l1.tail, l2.tail, Cons(f(l1.head, l2.head), acc))
                    }
            }
            val reversed = go(xs, ys, Nil)
            return foldLeft(reversed, Nil as List<B>, { ls, l -> Cons(l, ls)})
        }

        // Exercise 3.23
        tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean {
            tailrec fun goMatchAgainstBeginningOf(ls: List<A>, pat: List<A>, acc: Boolean): Boolean = when(pat) {
                is Nil -> acc
                is Cons ->
                    when (ls) {
                        is Nil -> false
                        is Cons -> goMatchAgainstBeginningOf(ls.tail, pat.tail, ls.head == pat.head && acc)
                    }
            }
            tailrec fun go(ls: List<A>, foundAcc: Boolean): Boolean {
                return when(ls) {
                    is Nil -> foundAcc
                    is Cons -> go(ls.tail,foundAcc || goMatchAgainstBeginningOf(ls, sub, true))
                }
            }
            return go(xs, false)
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

        // Exercise 3.8
        fun <A> length(xs: List<A>): Int =
            foldLeft(xs, 0, { acc, _ -> acc + 1 })

        fun <A> size(xs: List<A>): Int = length(xs)

        fun <A> isEmpty(xs: List<A>): Boolean = when(xs) {
            is Nil -> true
            is Cons -> false
        }
    }
}

object Nil : List<Nothing>() {
    override fun toString(): String {
        return "Nil"
    }
}
data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()

// Listing 3.12
sealed class Tree<out A> {
    companion object {
        // exercise 3.24
        fun <A> size(t: Tree<A>): Int = when (t) {
            is Leaf -> 1
            is Branch -> 1 + size(t.left) + size(t.right)
        }

        // exercise 3.25
        fun maximum(t: Tree<Int>): Int = when (t) {
            is Leaf -> t.value
            is Branch -> maxOf(maximum(t.left), maximum(t.right))
        }

        // exercise 3.26
        fun <A> depth(t: Tree<A>): Int = when (t) {
            is Leaf -> 1
            is Branch -> 1 + maxOf(depth(t.left), depth(t.right))
        }

        // exercise 3.27
        fun <A, B> map(t: Tree<A>, f: (A) -> B): Tree<B> = when (t) {
            is Leaf -> Leaf(f(t.value))
            is Branch -> Branch(map(t.left, f), map(t.right, f))
        }

        // exercise 3.28
        fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B = when(ta) {
            is Leaf<A> -> l(ta.value)
            is Branch<A> -> b(fold(ta.left, l, b), fold(ta.right, l, b))
        }

        // exercise 3.28
        fun <A> sizeF(ta: Tree<A>): Int =
            fold(ta, { _ -> 1 }, { x, y -> 1 + x + y })

        // exercise 3.28
        fun maximumF(ta: Tree<Int>): Int =
            fold(ta, { x -> x }, { x, y -> maxOf(x, y) })

        // exercise 3.29
        fun <A> depthF(ta: Tree<A>): Int =
            fold(ta, { x -> 1 }, { x, y -> 1 + maxOf(x, y) })

        // exercise 3.29
        fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
            fold(ta, { x -> (Leaf(f(x)) as Tree<B>) }, { x, y -> Branch<B>(x, y) })
    }
}

data class Leaf<A>(val value: A): Tree<A>()
data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

fun main2() {
    println("Ch03 - Functional data structures")

    val ex1: List<Double> = Nil
    val ex2: List<Int> = Cons(1, Nil)
    val ex3: List<String> = Cons("a", Cons("b", Nil))

    println("l1: " + ex1)
    println("l2: " + ex2)
    println("l3: " + ex3)
    println("of: " + List.of(1,2,3))
}
