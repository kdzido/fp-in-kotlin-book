package funkotlin.fp_in_kotlin_book.chapter08

data class SGen<A>(val forSize: (Int) -> Gen<A>) {
    operator fun invoke(i: Int): Gen<A> = forSize(i)

    fun <B> map(f: (A) -> B): SGen<B> = SGen({ n ->
        forSize(n).map(f)
    })

    fun <B> flatMap(f: (A) -> Gen<B>): SGen<B> = SGen({ n ->
        forSize(n).flatMap(f)
    })
}
