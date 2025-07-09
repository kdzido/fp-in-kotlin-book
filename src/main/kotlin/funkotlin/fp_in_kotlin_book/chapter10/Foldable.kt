package funkotlin.fp_in_kotlin_book.chapter10

import arrow.Kind

interface Foldable<F> {
    fun <A, B> foldRight(fa: Kind<F, A>, z: B, f: (A, B) -> B): B =
        foldMap(fa, endoMonoidAndThen()) { a: A -> { b: B -> f(a, b)}}(z)

    fun <A, B> foldLeft(fa: Kind<F, A>, z: B, f: (B, A) -> B): B =
        foldMap(fa, dual(endoMonoidAndThen())) { a: A -> { b: B -> f(b, a)}}(z)

    fun <A, B> foldMap(fa: Kind<F, A>, m: Monoid<B>, f: (A) -> B): B =
        foldRight(fa, m.nil) { a: A, b: B -> m.combine(f(a), b) }

    fun <A> concatenate(fa: Kind<F, A>, m: Monoid<A>): A =
        foldLeft(fa, m.nil, m::combine)
}
