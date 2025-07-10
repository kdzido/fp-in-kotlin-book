package funkotlin.fp_in_kotlin_book.chapter03

import arrow.Kind

class ForTree private constructor() { companion object }

typealias TreeOf<A> = Kind<ForTree, A>

fun <A> TreeOf<A>.fix() = this as Tree<A>
