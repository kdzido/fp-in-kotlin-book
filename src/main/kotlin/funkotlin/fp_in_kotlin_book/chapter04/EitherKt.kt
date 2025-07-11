package funkotlin.fp_in_kotlin_book.chapter04

import arrow.Kind
import arrow.Kind2

class ForEither private constructor() { companion object }
typealias EitherOf<E, A> = Kind2<ForEither, E, A>

fun <E, A> EitherOf<E, A>.fix() = this as Either<E, A>
