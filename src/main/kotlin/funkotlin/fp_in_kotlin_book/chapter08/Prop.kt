package funkotlin.fp_in_kotlin_book.chapter08

interface Gen<A>

interface Prop {
    fun check(): Boolean

    fun add(p: Prop): Prop {
        val outer = this
        return object : Prop {
            override fun check(): Boolean {
                return p.check() && outer.check()
            }
        }
    }
}

fun <A> listOf(a: Gen<A>): List<Gen<A>> = TODO()

fun <A> listOfN(n: Int, a: Gen<A>): List<Gen<A>> = TODO()

fun <A> forAll(a: Gen<A>, f: (A) -> Boolean): Prop = TODO()
