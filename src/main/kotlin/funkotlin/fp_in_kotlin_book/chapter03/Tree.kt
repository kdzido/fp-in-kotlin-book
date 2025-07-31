package funkotlin.fp_in_kotlin_book.chapter03

import arrow.Kind

class ForTree private constructor() { companion object }
typealias TreeOf<A> = Kind<ForTree, A>
fun <A> TreeOf<A>.fix() = this as Tree<A>

// Listing 3.12
sealed class Tree<out A> : TreeOf<A> {
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
