package funkotlin.fp_in_kotlin_book.chapter07

import arrow.Kind

class ForPar private constructor() { companion object }

typealias ParOf<T> = Kind<ForPar, T>

fun <A> ParOf<A>.fix() = this as Par<A>
