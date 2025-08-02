package funkotlin.fp_in_kotlin_book.chapter12

import funkotlin.fp_in_kotlin_book.chapter10.Monoid


// book's example: Foldable that is not Functor
data class Iter<A>(val a: A, val f: (A) -> A, val n: Int) {
    fun <B> foldMap(fn: (A) -> B, m: Monoid<B>): B {
        tailrec fun iterate(len: Int, nil: B, aa: A): B =
            if (len <= 0) nil else iterate(len - 1, fn(aa), f(a))
        return iterate(n, m.nil, a)
    }
}
