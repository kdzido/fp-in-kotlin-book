package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter04.EitherOf
import funkotlin.fp_in_kotlin_book.chapter04.EitherPartialOf
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter04.flatMap
import funkotlin.fp_in_kotlin_book.chapter11.Monad


interface EitherMonad<E> : Monad<EitherPartialOf<E>> {
    override fun <A> unit(a: A): EitherOf<E, A>

    override fun <A, B> flatMap(
        fa: EitherOf<E, A>,
        f: (A) -> EitherOf<E, B>,
    ): EitherOf<E, B>
}

fun <E> eitherMonad(): EitherMonad<E> = object : EitherMonad<E> {
    override fun <A> unit(a: A): EitherOf<E, A> = Right(a)

    override fun <A, B> flatMap(
        fa: EitherOf<E, A>,
        f: (A) -> EitherOf<E, B>,
    ): EitherOf<E, B> = when(val faf: Either<E, A> = fa.fix()) {
        is Left -> faf
        is Right -> faf.flatMap { a -> f(a).fix() }
    }
}
