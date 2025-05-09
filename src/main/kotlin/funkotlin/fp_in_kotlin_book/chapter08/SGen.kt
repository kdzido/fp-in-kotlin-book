package funkotlin.fp_in_kotlin_book.chapter08

import funkotlin.fp_in_kotlin_book.chapter08.Gen.Companion.listOfSpecifiedN

data class SGen<A>(val forSize: (Int) -> Gen<A>) {
    operator fun invoke(i: Int): Gen<A> = forSize(i)

    fun <B> map(f: (A) -> B): SGen<B> = SGen({ n ->
        forSize(n).map(f)
    })

    fun <B> flatMap(f: (A) -> Gen<B>): SGen<B> = SGen({ n ->
        forSize(n).flatMap(f)
    })

    companion object {
        fun <A> listOf(ga: Gen<A>): SGen<List<A>> = SGen({ n ->
            listOfSpecifiedN(n, ga)
        })


        fun <A> nonEmptyListOf(ga: Gen<A>): SGen<List<A>> = SGen({ n ->
            listOfSpecifiedN(n + 1, ga)
        })
    }
}
