package funkotlin.fp_in_kotlin_book.chapter10

import arrow.core.andThen
import arrow.core.compose
import arrow.core.extensions.list.foldable.foldLeft
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.orElse
import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.Prop
import funkotlin.fp_in_kotlin_book.chapter08.Prop.Companion.forAll
import kotlin.Int

interface Monoid<A> {
    fun combine(a1: A, a2: A): A
    val nil: A
}

val stringMonoid = object : Monoid<String> {
    override fun combine(a1: String, a2: String): String = a1 + a2
    override val nil: String = ""
}

fun <A> listMonoid(): Monoid<List<A>> = object : Monoid<List<A>> {
    override fun combine(a1: List<A>, a2: List<A>): List<A> = a1 + a2
    override val nil: List<A> = emptyList()
}

fun intAddition(): Monoid<Int> = object : Monoid<Int> {
    override fun combine(a1: Int, a2: Int): Int = a1 + a2
    override val nil: Int = 0
}

fun intMultiplication(): Monoid<Int> = object : Monoid<Int> {
    override fun combine(a1: Int, a2: Int): Int = a1 * a2
    override val nil: Int = 1
}
fun booleanOr(): Monoid<Boolean> = object : Monoid<Boolean> {
    override fun combine(a1: Boolean, a2: Boolean): Boolean = a1 || a2
    override val nil: Boolean = false
}

fun booleanAnd(): Monoid<Boolean> = object : Monoid<Boolean> {
    override fun combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2
    override val nil: Boolean = true
}

fun <A> optionMonoid(): Monoid<Option<A>> = object : Monoid<Option<A>> {
    override fun combine(a1: Option<A>, a2: Option<A>): Option<A> = a1.orElse { a2 }
    override val nil: Option<A> = None
}

fun <A> dual(m: Monoid<A>): Monoid<A> = object : Monoid<A> {
    override fun combine(a1: A, a2: A): A = m.combine(a2, a1)
    override val nil: A = m.nil
}

fun <A> firstOptionMonoid(): Monoid<Option<A>> = optionMonoid()
fun <A> lastOptionMonoid(): Monoid<Option<A>> = dual(optionMonoid())

fun <A> endoMonoidAndThen(): Monoid<(A) -> A> = object : Monoid<(A) -> A> {
    override fun combine(a1: (A) -> A, a2: (A) -> A): (A) -> A = a1 andThen a2
    override val nil: (A) -> A get() = { a -> a }
}
fun <A> endoMonoidComposed(): Monoid<(A) -> A> = object : Monoid<(A) -> A> {
    override fun combine(a1: (A) -> A, a2: (A) -> A): (A) -> A = a1 compose a2
    override val nil: (A) -> A get() = { a -> a }
}

fun <A> monoidLaws(m: Monoid<A>, g: Gen<A>): Prop =
    forAll(
        g.flatMap { a ->
            g.flatMap { b ->
                g.map { c -> Triple(a, b, c) }
            }
        }
    ) { (a: A, b: A, c: A) ->
        // associativity
        m.combine(a, m.combine(b, c)) == m.combine(m.combine(a, b), c) &&
                // identity
                m.combine(a, m.nil) == a &&
                m.combine(m.nil, a) == a &&
                m.combine(m.nil, m.nil) == m.nil
    }

fun <A> concatenate(ls: List<A>, m: Monoid<A>): A =
    ls.foldLeft(m.nil, m::combine)

fun <A, B> foldMap(ls: List<A>, m: Monoid<B>, f: (A) -> B): B =
    ls.map(f)
        .foldLeft(m.nil, m::combine)

fun <A, B> balFoldMap(ls: List<A>, m: Monoid<B>, f: (A) -> B): B =
    when {
        ls.isEmpty() -> m.nil
        ls.size == 1 -> f(ls.first())
        else -> {
            val index: Int = ls.size / 2
            val l1 = ls.take(index)
            val l2 = ls.drop(index)
            m.combine(balFoldMap(l1, m, f), balFoldMap(l2, m, f))
        }
    }

fun <A, B> parFoldMap(ls: List<A>, m: Monoid<Par<B>>, f: (A) -> B): Par<B> =
    when {
        ls.isEmpty() -> m.nil
        ls.size == 1 -> Pars.lazyUnit { f(ls.first()) }
        else -> {
            val index: Int = ls.size / 2
            val l1 = ls.take(index)
            val l2 = ls.drop(index)
            m.combine(parFoldMap(l1, m, f), parFoldMap(l2, m, f))
        }
    }

fun <A> monoidPar(m: Monoid<A>): Monoid<Par<A>> =
    object : Monoid<Par<A>> {
        override fun combine(a1: Par<A>, a2: Par<A>, ): Par<A> = Pars.map2(a1, a2, m::combine)
        override val nil: Par<A> get() = Pars.unit(m.nil)
    }

fun <B> foldMonoid(): Monoid<(B) -> B> = object : Monoid<(B) -> B> {
    override fun combine(a1: (B) -> B, a2: (B) -> B): (B) -> B = a1 andThen a2
    override val nil: (B) -> B get() = { b -> b }
}

fun <A, B> foldRight(ls: Sequence<A>, z: B, f: (A, B) -> B): B {
    val fc: (A) -> (B) -> B = { a -> { b -> f(a, b) }}

    return foldMap(ls.toList(), foldMonoid(), fc)(z)
}

fun <A, B> foldLeft(ls: Sequence<A>, z: B, f: (B, A) -> B): B {
    val fc: (A) -> (B) -> B = { a -> { b -> f(b, a) }}

    return foldMap(ls.toList(), dual(foldMonoid()), fc)(z)
}

data class ListInterval(
    val first: Int,
    val last: Int,
    val isInOrder: Boolean
)

fun orderedIntervalsMonoid(): Monoid<ListInterval> = object : Monoid<ListInterval> {
    // a1.last < a2.first, remember combine is not commutative in monoids
    override fun combine(a1: ListInterval, a2: ListInterval): ListInterval =
        when {
            a1.isInOrder && a2.isInOrder && a1.last < a2.first -> ListInterval(a1.first, a2.last, true)
            else -> ListInterval(a1.last, a2.last, false)
        }
    override val nil: ListInterval get() = ListInterval(0, 0, true)
}

fun ordered(ints: Sequence<Int>): Boolean =
    balFoldMap(ints.toList(), orderedIntervalsMonoid(), { a: Int ->
        ListInterval(a, a, true)
    }
    ).isInOrder
