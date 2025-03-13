package funkotlin.fp_in_kotlin_book.chapter07

import arrow.core.extensions.list.foldable.firstOption
import arrow.core.getOrElse


// listing 7.2
class Par<A>(val get: A)
fun <A> unit(a: () -> A): Par<A> = Par(a())
fun <A> get(a: Par<A>): A = a.get

// listing 7.1
fun sum(ints: List<Int>): Int =
    if (ints.size <= 1)
        ints.firstOption().getOrElse { 0 }
    else {
        val (l, r) = ints.splitAt(ints.size / 2)
        sum(l) + sum(r)
    }

fun <T> Iterable<T>.splitAt(n: Int): Pair<List<T>, List<T>> =
    when {
        n < 0 -> throw IllegalArgumentException("Requested split at index $n is less than zero.")
        n == 0 -> emptyList<T>() to toList()
        this is Collection<T> && (n >= size) -> toList() to emptyList()
        else -> {
            val dn = if (this is Collection<T>) size - n else n
            val left = ArrayList<T>(n)
            val right = ArrayList<T>(dn)
            for ((idx, item) in this.withIndex()) {
                when (idx >= n) {
                    false -> left.add(item)
                    true -> right.add(item)
                }
            }
            left to right
        }
    }.let {
        it.first to it.second
    }

fun main() {

}
