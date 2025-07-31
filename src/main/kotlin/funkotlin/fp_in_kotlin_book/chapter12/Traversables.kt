package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.ForList
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.ListOf
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.OptionOf
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter04.map

class ForTree private constructor() { companion object }
typealias TreeOf<A> = Kind<ForTree, A>
fun <A> TreeOf<A>.fix() = this as Tree<A>

data class Tree<out A>(val head: A, val tail: List<Tree<A>>) : TreeOf<A>

object Traversables {
    fun <A> optionTraversable(): Traversable<ForOption> = object : Traversable<ForOption> {
        override fun <A, B> map(
            fa: OptionOf<A>,
            f: (A) -> B,
        ): OptionOf<B> =
            fa.fix().map(f)

        override fun <G, A, B> traverse(
            fa: OptionOf<A>,
            AG: Applicative<G>,
            f: (A) -> Kind<G, B>,
        ): Kind<G, OptionOf<B>> = when (val o = fa.fix()) {
            None -> AG.unit(None)
            is Some -> AG.map(f(o.value)) { Some(it) }
        }
    }

    fun <A> listTraversable(): Traversable<ForList> = object : Traversable<ForList> {
        override fun <A, B> map(
            fa: ListOf<A>,
            f: (A) -> B,
        ): ListOf<B> =
            List.map(fa.fix(), f)

        override fun <G, A, B> traverse(
            fa: ListOf<A>,
            AG: Applicative<G>,
            f: (A) -> Kind<G, B>,
        ): Kind<G, ListOf<B>> =
            List.foldRight(fa.fix(), AG.unit(List.of<B>())) {e: A,  acc: Kind<G, List<B>>  ->
                AG.map2(f(e), acc) { h: B, ts: List<B> -> Cons(h, ts) }
            }
    }

    fun <A> treeTraversable(): Traversable<ForTree> = object : Traversable<ForTree> {
        override fun <A, B> map(
            fa: TreeOf<A>,
            f: (A) -> B,
        ): TreeOf<B> = TODO()

        override fun <G, A, B> traverse(
            fa: TreeOf<A>,
            AG: Applicative<G>,
            f: (A) -> Kind<G, B>,
        ): Kind<G, Kind<ForTree, B>> {
            val v = fa.fix()

            return AG.map2(
                f(v.head),
                listTraversable<A>().traverse(v.tail, AG) { ta: Tree<A> ->
                    traverse(ta, AG, f)
                }
            ) { h: B, t: Kind<ForList,TreeOf<B>> ->
                Tree(h, List.map(t.fix()) { it.fix() })
            }
        }
    }
}
