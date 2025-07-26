package funkotlin.fp_in_kotlin_book.chapter12

import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL

interface ValidationApplicative<E> : Applicative<ValidationPartialOf<E>> {
    override fun <A> unit(a: A): ValidationOf<E, A>

    override fun <A, B, C> map2(
        fa: ValidationOf<E, A>,
        fb: ValidationOf<E, B>,
        f: (A, B) -> C,
    ): ValidationOf<E, C>
}

fun <E> validation(): ValidationApplicative<E> = object : ValidationApplicative<E> {
    override fun <A> unit(a: A): ValidationOf<E, A> = Success(a)

    override fun <A, B, C> map2(
        fa: ValidationOf<E, A>,
        fb: ValidationOf<E, B>,
        f: (A, B) -> C,
    ): ValidationOf<E, C> {
        val va: Validation<E, A> = fa.fix()
        val vb: Validation<E, B> = fb.fix()

        return when {
            va is Failure<E> && vb is Failure<E> -> Failure(va.head, Cons(vb.head, ListL.append(va.tail, vb.tail)))
            va is Success<A> && vb is Failure<E> -> vb
            va is Failure<E> && vb is Success<B> -> va
            va is Success<A> && vb is Success<B> -> Success(f(va.a, vb.a))
            else -> throw IllegalStateException("Unsupported Validation map2 combination")
        }
    }
}
