package funkotlin.fp_in_kotlin_book.chapter03

import arrow.Kind

class ForList private constructor() {
    companion object
}

typealias ListOf<A> = Kind<ForList, A>

fun <A> ListOf<A>.fix() = this as List<A>
