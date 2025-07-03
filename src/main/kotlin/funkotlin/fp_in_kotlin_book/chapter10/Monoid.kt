package funkotlin.fp_in_kotlin_book.chapter10

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
