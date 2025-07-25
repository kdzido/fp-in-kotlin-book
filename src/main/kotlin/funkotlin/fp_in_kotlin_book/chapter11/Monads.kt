package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind
import arrow.core.ForListK
import arrow.core.ForSequenceK
import arrow.core.ListK
import arrow.core.SequenceK
import arrow.core.fix
import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.ForList
import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh3
import funkotlin.fp_in_kotlin_book.chapter03.fix
import funkotlin.fp_in_kotlin_book.chapter03.reversed
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter04.flatMap
import funkotlin.fp_in_kotlin_book.chapter06.State
import funkotlin.fp_in_kotlin_book.chapter06.StateOf
import funkotlin.fp_in_kotlin_book.chapter06.fix
import funkotlin.fp_in_kotlin_book.chapter07.ForPar
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter07.fix
import funkotlin.fp_in_kotlin_book.chapter08.ForGen
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monads.intStateMonad

object Monads {
    val genMonad = object : Monad<ForGen> {
        override fun <A> unit(a: A): Kind<ForGen, A> = Gen.unit(a)

        override fun <A, B> apply(
            fab: Kind<ForGen, (A) -> B>,
            fa: Kind<ForGen, A>,
        ): Kind<ForGen, B> =
            flatMap(fa.fix()) { a: A -> flatMap(fab.fix()) { f: (A) -> B -> unit(f(a)).fix() } }

        override fun <A, B> flatMap(
            fa: Kind<ForGen, A>,
            f: (A) -> Kind<ForGen, B>,
        ): Kind<ForGen, B> =
            fa.fix().flatMap { a -> f(a).fix() }

//        override fun <A, B, C> compose(
//            f: (A) -> Kind<ForGen, B>,
//            g: (B) -> Kind<ForGen, C>,
//        ): (A) -> Kind<ForGen, C>  = { a: A ->
//            f(a).fix().flatMap { b: B -> g(b).fix() }
//        }
    }

    fun parMonad(): Monad<ForPar> = object : Monad<ForPar> {
        override fun <A> unit(a: A): Kind<ForPar, A> = Pars.unit(a)

        override fun <A, B> flatMap(
            fa: Kind<ForPar, A>,
            f: (A) -> Kind<ForPar, B>,
        ): Kind<ForPar, B> =
            Pars.flatMap(fa.fix()) { a -> f(a).fix() }
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
        override fun <A> unit(a: A): Kind<ForList, A> = ListCh3.of(a)

        override fun <A, B> flatMap(
            fa: Kind<ForList, A>,
            f: (A) -> Kind<ForList, B>,
        ): Kind<ForList, B> =
            ListCh3.flatMap(fa.fix()) { a -> f(a).fix() }
    }

    fun listKMonad(): Monad<ForListK> = object : Monad<ForListK> {
        override fun <A> unit(a: A): Kind<ForListK, A> = ListK.empty()

        override fun <A, B> flatMap(
            fa: Kind<ForListK, A>,
            f: (A) -> Kind<ForListK, B>,
        ): Kind<ForListK, B> =
            fa.fix().flatMap { a -> f(a).fix() }
    }

    fun sequenceKMonad(): Monad<ForSequenceK> = object : Monad<ForSequenceK> {
        override fun <A> unit(a: A): Kind<ForSequenceK, A> = SequenceK.empty()

        override fun <A, B> flatMap(
            fa: Kind<ForSequenceK, A>,
            f: (A) -> Kind<ForSequenceK, B>,
        ): Kind<ForSequenceK, B> =
            fa.fix().flatMap { a -> f(a).fix() }
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

fun <A> zipWithIndex(la: ListCh3<A>): ListCh3<Pair<Int, A>> {
    val m = intStateMonad()

    return ListCh3.foldLeft<A, StateOf<Int, ListCh3<Pair<Int, A>>>>(
        la,
        m.unit<ListCh3<Pair<Int, A>>>(ListCh3.of<Pair<Int, A>>())
    ) { acc: StateOf<Int, ListCh3<Pair<Int, A>>>, a: A ->
        acc.fix<Int, ListCh3<Pair<Int, A>>>().flatMap<Cons<Pair<Int, A>>> { xs ->
            acc.fix<Int, ListCh3<Pair<Int, A>>>().getState<Int>().flatMap<Cons<Pair<Int, A>>> { n ->
                acc.fix<Int, ListCh3<Pair<Int, A>>>().setState(n + 1).fix().map<Cons<Pair<Int, A>>> { u ->
                    Cons<Pair<Int, A>>(n to a, xs)
                }
            }
        }
    }.fix<Int, ListCh3<Pair<Int, A>>>().run(0).first.reversed<Pair<Int, A>>()
}
