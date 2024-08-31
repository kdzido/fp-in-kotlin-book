package funkotlin.fp_in_kotlin_book.chapter03

sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
    }
}

object Nil : List<Nothing>() {
    override fun toString(): String {
        return "Nil"
    }
}
data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()


fun main() {
    println("Ch03 - Functional data structures")

    val ex1: List<Double> = Nil
    val ex2: List<Int> = Cons(1, Nil)
    val ex3: List<String> = Cons("a", Cons("b", Nil))

    println("l1: " + ex1)
    println("l2: " + ex2)
    println("l3: " + ex3)
    println("of: " + List.of(1,2,3))
}
