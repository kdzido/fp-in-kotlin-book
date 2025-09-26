package funkotlin.fp_in_kotlin_book.chapter15

import funkotlin.fp_in_kotlin_book.chapter11.Monad

interface ProcessMonad<I, O> : Monad<ProcessPartialOf<I>> {
    override fun <A> unit(a: A): ProcessOf<I, A> = Emit(a)

    override fun <A, B> flatMap(
        fa: ProcessOf<I, A>,
        f: (A) -> ProcessOf<I, B>,
    ): ProcessOf<I, B> =
        fa.fix().flatMap { a -> f(a).fix() }

    override fun <A, B> map(
        fa: ProcessOf<I, A>,
        f: (A) -> B,
    ): ProcessOf<I, B> =
        fa.fix().map(f)
}
