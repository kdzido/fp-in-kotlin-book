package funkotlin.fp_in_kotlin_book.chapter06

import arrow.Kind

class ForIntState private constructor() { companion object }
typealias IntStateOf<T> = Kind<ForIntState, T>
inline fun <T> IntStateOf<T>.fix() = this as State<Int, T>

