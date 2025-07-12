package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import arrow.core.ForListK
import arrow.core.ForSequenceK
import arrow.core.ListK
import arrow.core.SequenceK
import arrow.core.extensions.listk.monad.flatMap
import arrow.core.extensions.sequencek.monad.flatMap
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.ForList
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter04.flatMap
import funkotlin.fp_in_kotlin_book.chapter07.ForPar
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter07.fix
import funkotlin.fp_in_kotlin_book.chapter08.ForGen
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix

interface Monad<F> : Functor<F> {
    fun <A> unit(a: A): Kind<F, A>

    fun <A, B> flatMap(fa: Kind<F, A>, f: (A) -> Kind<F, B>): Kind<F, B>

    override fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B> =
        flatMap(fa) { a -> unit(f(a)) }

    fun <A, B, C> map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C,
    ): Kind<F, C> =
        flatMap(fa) { a -> map(fb) { b -> f(a, b) } }
}

object Monads {
    val genMonad = object : Monad<ForGen> {
        override fun <A> unit(a: A): Kind<ForGen, A> = Gen.unit(a)

        override fun <A, B> flatMap(
            fa: Kind<ForGen, A>,
            f: (A) -> Kind<ForGen, B>,
        ): Kind<ForGen, B> =
            fa.fix().flatMap { a -> f(a).fix() }
    }

    fun parMonad(): Monad<ForPar> = object : Monad<ForPar> {
        override fun <A> unit(a: A): Kind<ForPar, A> =
            Pars.unit(a)

        override fun <A, B> flatMap(
            fa: Kind<ForPar, A>,
            f: (A) -> Kind<ForPar, B>,
        ): Kind<ForPar, B> =
            Pars.flatMap(fa.fix()) { b -> f(b).fix() }
    }

    fun optionMonad(): Monad<ForOption> = object : Monad<ForOption> {
        override fun <A> unit(a: A): Kind<ForOption, A> = Some(a)

        override fun <A, B> flatMap(
            fa: Kind<ForOption, A>,
            f: (A) -> Kind<ForOption, B>,
        ): Kind<ForOption, B> =
            fa.fix().flatMap { a -> f(a).fix() }
    }

    fun listMonad(): Monad<ForList> = object : Monad<ForList> {
        override fun <A> unit(a: A): Kind<ForList, A> = List.of<A>()

        override fun <A, B> flatMap(
            fa: Kind<ForList, A>,
            f: (A) -> Kind<ForList, B>,
        ): Kind<ForList, B> =
            List.flatMap(fa.fix()) { a -> f(a).fix() }
    }

    fun listKMonad(): Monad<ForListK> = object : Monad<ForListK> {
        override fun <A> unit(a: A): Kind<ForListK, A> = ListK.empty()

        override fun <A, B> flatMap(
            fa: Kind<ForListK, A>,
            f: (A) -> Kind<ForListK, B>,
        ): Kind<ForListK, B> =
            fa.flatMap(f)
    }

    fun sequenceKMonad(): Monad<ForSequenceK> = object : Monad<ForSequenceK> {
        override fun <A> unit(a: A): Kind<ForSequenceK, A> = SequenceK.empty()

        override fun <A, B> flatMap(
            fa: Kind<ForSequenceK, A>,
            f: (A) -> Kind<ForSequenceK, B>,
        ): Kind<ForSequenceK, B> =
            fa.flatMap(f)
    }
}
