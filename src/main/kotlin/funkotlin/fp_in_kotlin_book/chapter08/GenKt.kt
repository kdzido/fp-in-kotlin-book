package funkotlin.fp_in_kotlin_book.chapter08

import arrow.Kind

class ForGen private constructor() { companion object }

typealias GenOf<T> = Kind<ForGen, T>

fun <A> GenOf<A>.fix() = this as Gen<A>
