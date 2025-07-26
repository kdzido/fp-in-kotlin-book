package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.EitherOf
import funkotlin.fp_in_kotlin_book.chapter04.EitherPartialOf
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter04.flatMap
import funkotlin.fp_in_kotlin_book.chapter04.map

interface EitherApplicative<E> : Applicative<EitherPartialOf<E>> {
    override fun <A> unit(a: A): EitherOf<E, A>

    override fun <A, B, C> map2(
        fa: Kind<EitherPartialOf<E>, A>,
        fb: Kind<EitherPartialOf<E>, B>,
        f: (A, B) -> C,
    ): Kind<EitherPartialOf<E>, C>
}

fun <E> eitherApplicative(): EitherApplicative<E> = object : EitherApplicative<E> {
    override fun <A> unit(a: A): EitherOf<E, A> = Right(a)

    override fun <A, B, C> map2(
        fa: EitherOf<E, A>,
        fb: EitherOf<E, B>,
        f: (A, B) -> C,
    ): EitherOf<E, C> =
        fa.fix().flatMap { a ->
            fb.fix().map { b -> f(a, b) }
        }
}
