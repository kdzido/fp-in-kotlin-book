package funkotlin.fp_in_kotlin_book.chapter06

import arrow.Kind
import arrow.Kind2

class ForState private constructor() { companion object }

typealias StateOf<S, T> = Kind2<ForState, S, T>
typealias StatePartialOf<S> = Kind<ForState, S>
inline fun <S, T> StateOf<S, T>.fix() = this as State<S, T>


