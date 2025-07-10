package funkotlin.fp_in_kotlin_book.chapter04

import arrow.Kind


class ForOption private constructor() { companion object }

typealias OptionOf<T> = Kind<ForOption, T>

fun <A> OptionOf<A>.fix() = this as Option<A>
