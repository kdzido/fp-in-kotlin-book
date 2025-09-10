package funkotlin.fp_in_kotlin_book.chapter13

import arrow.Kind
import arrow.Kind2

sealed class ForFree { companion object }
typealias FreeOf<F, A> = Kind2<ForFree, F, A>
typealias FreePartialOf<F> = Kind<ForFree, F>
inline fun <F, A> FreeOf<F, A>.fix(): Free<F, A> = this as Free<F, A>

sealed class Free<F, A> : FreeOf<F, A>
data class Return<F, A>(val a: A) : Free<F, A>()
data class Suspend<F, A>(val s: Kind<F, A>) : Free<F, A>()
data class FlatMap<F, A, B>(
    val s: Kind<F, A>,
    val f: (A) -> Free<F, B>
): Free<F, B>()
