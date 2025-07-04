package funkotlin.fp_in_kotlin_book.chapter10

import arrow.core.andThen
import arrow.core.compose
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.orElse
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.Prop
import funkotlin.fp_in_kotlin_book.chapter08.Prop.Companion.forAll

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
