package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import arrow.core.ForListK
import arrow.core.ForSequenceK
import arrow.core.ListK
import arrow.core.SequenceK
import arrow.core.extensions.listk.monad.flatMap
import arrow.core.fix
import funkotlin.fp_in_kotlin_book.chapter03.ForList
import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter06.State
import funkotlin.fp_in_kotlin_book.chapter06.StateOf
import funkotlin.fp_in_kotlin_book.chapter06.fix
import funkotlin.fp_in_kotlin_book.chapter04.flatMap as flatMapOption
import funkotlin.fp_in_kotlin_book.chapter07.ForPar
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter07.fix
import funkotlin.fp_in_kotlin_book.chapter08.ForGen
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix

object Monads {
    val genMonad = object : Monad<ForGen> {
        override fun <A> unit(a: A): Kind<ForGen, A> = Gen.unit(a)

        override fun <A, B, C> compose(
            f: (A) -> Kind<ForGen, B>,
            g: (B) -> Kind<ForGen, C>,
        ): (A) -> Kind<ForGen, C>  = { a: A ->
            f(a).fix().flatMap { b: B -> g(b).fix() }
        }
    }

    fun parMonad(): Monad<ForPar> = object : Monad<ForPar> {
        override fun <A> unit(a: A): Kind<ForPar, A> =
            Pars.unit(a)

        override fun <A, B, C> compose(
            f: (A) -> Kind<ForPar, B>,
            g: (B) -> Kind<ForPar, C>,
        ): (A) -> Kind<ForPar, C> = { a: A ->
            Pars.flatMap(f(a).fix()) { b: B -> g(b).fix() }
        }
    }

    fun optionMonad(): Monad<ForOption> = object : Monad<ForOption> {
        override fun <A> unit(a: A): Kind<ForOption, A> = Some(a)

        override fun <A, B, C> compose(
            f: (A) -> Kind<ForOption, B>,
            g: (B) -> Kind<ForOption, C>,
        ): (A) -> Kind<ForOption, C> = { a: A ->
            f(a).fix().flatMapOption { b: B -> g(b).fix() }
        }
    }

    fun listMonad(): Monad<ForList> = object : Monad<ForList> {
        override fun <A> unit(a: A): Kind<ForList, A> = funkotlin.fp_in_kotlin_book.chapter03.List.of<A>(a)

        override fun <A, B, C> compose(
            f: (A) -> Kind<ForList, B>,
            g: (B) -> Kind<ForList, C>,
        ): (A) -> Kind<ForList, C> = { a: A ->
            List.flatMap(f(a).fix()) { b: B -> g(b).fix() }
        }
    }

    fun listKMonad(): Monad<ForListK> = object : Monad<ForListK> {
        override fun <A> unit(a: A): Kind<ForListK, A> = ListK.empty()

        override fun <A, B, C> compose(
            f: (A) -> Kind<ForListK, B>,
            g: (B) -> Kind<ForListK, C>,
        ): (A) -> Kind<ForListK, C> = { a: A ->
            f(a).flatMap { b: B -> g(b) }
        }
    }

    fun sequenceKMonad(): Monad<ForSequenceK> = object : Monad<ForSequenceK> {
        override fun <A> unit(a: A): Kind<ForSequenceK, A> = SequenceK.empty()

        override fun <A, B, C> compose(
            f: (A) -> Kind<ForSequenceK, B>,
            g: (B) -> Kind<ForSequenceK, C>,
        ): (A) -> Kind<ForSequenceK, C> = { a: A ->
            f(a).fix().flatMap { b: B -> g(b).fix() }
        }
    }

    fun intStateMonad() = object : StateMonad<Int> {
        override fun <A> unit(a: A): StateOf<Int, A> = State { s -> a to s}

        override fun <A, B, C> compose(
            f: (A) -> StateOf<Int, B>,
            g: (B) -> StateOf<Int, C>,
        ): (A) -> StateOf<Int, C> = { a: A ->
            f(a).fix().flatMap { b: B -> g(b).fix() }
        }
    }
}
