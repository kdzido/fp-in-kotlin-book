package funkotlin.fp_in_kotlin_book.chapter08

data class SGen<A>(val forSize: (Int) -> Gen<A>)
