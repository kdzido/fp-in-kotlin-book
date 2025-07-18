package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter06.StateOf
import funkotlin.fp_in_kotlin_book.chapter06.StatePartialOf

interface StateMonad<S> : Monad<StatePartialOf<S>> {
    override fun <A> unit(a: A): StateOf<S, A>

    override fun <A, B, C> compose(
        f: (A) -> StateOf<S, B>,
        g: (B) -> StateOf<S, C>,
    ): (A) -> StateOf<S, C>
}

