package funkotlin.fp_in_kotlin_book.chapter10

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Branch
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.ForList
import funkotlin.fp_in_kotlin_book.chapter03.ForTree
import funkotlin.fp_in_kotlin_book.chapter03.Leaf
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix

interface Foldable<F> {
    fun <A, B> foldRight(fa: Kind<F, A>, z: B, f: (A, B) -> B): B =
        foldMap(fa, endoMonoidAndThen()) { a: A -> { b: B -> f(a, b)}}(z)

    fun <A, B> foldLeft(fa: Kind<F, A>, z: B, f: (B, A) -> B): B =
        foldMap(fa, dual(endoMonoidAndThen())) { a: A -> { b: B -> f(b, a)}}(z)

    fun <A, B> foldMap(fa: Kind<F, A>, m: Monoid<B>, f: (A) -> B): B =
        foldRight(fa, m.nil) { a: A, b: B -> m.combine(f(a), b) }

    fun <A> concatenate(fa: Kind<F, A>, m: Monoid<A>): A =
        foldLeft(fa, m.nil, m::combine)

    fun <A> toList(fa: Kind<F, A>): List<A> =
        foldLeft(fa, List.of()) { la, a -> Cons(a, la) }
}

object ListFoldable : Foldable<ForList> {
    override fun <A, B> foldRight(
        fa: Kind<ForList, A>,
        z: B,
        f: (A, B) -> B,
    ): B = List.foldRight(fa.fix(), z, f)

    override fun <A, B> foldLeft(
        fa: Kind<ForList, A>,
        z: B,
        f: (B, A) -> B,
    ): B = List.foldLeft(fa.fix(), z, f)
}

object TreeFoldable : Foldable<ForTree> {
    override fun <A, B> foldMap(
        fa: Kind<ForTree, A>,
        m: Monoid<B>,
        f: (A) -> B,
    ): B = when (val t = fa.fix()) {
        is Branch -> m.combine(foldMap(t.left, m, f), foldMap(t.right, m, f))
        is Leaf -> f(t.value)
    }
}

object OptionFoldable : Foldable<ForOption> {
    override fun <A, B> foldMap(
        fa: Kind<ForOption, A>,
        m: Monoid<B>,
        f: (A) -> B,
    ): B = when (val t = fa.fix()) {
        is None -> m.nil
        is Some -> f(t.value)
    }
}
