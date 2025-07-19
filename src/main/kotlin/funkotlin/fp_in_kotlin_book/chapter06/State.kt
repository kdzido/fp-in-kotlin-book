package funkotlin.fp_in_kotlin_book.chapter06

import funkotlin.fp_in_kotlin_book.chapter03.Cons
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL

// fns of this type are called state actions of state transitions

//data class State<S, out A>(val run: (S) -> Pair<A, S>) {
//data class State<S, out A>(val run: (S) -> Pair<A, S>) : IntStateOf<A> {
data class State<S, out A>(val run: (S) -> Pair<A, S>) : StateOf<S, A> {
    // EXER 6.8, 6.10
    fun < B> flatMap(f: (A) -> State<S, B>): State<S, B> = State { rng ->
        val (a1, s2) = this.run(rng)
        f(a1).run(s2)
    }

    // EXER 6.5, 6.9, 6.10
    fun <B> map(f: (A) -> B): State<S, B> =
        this.flatMap { a -> unit(f(a)) }

    fun <S> getState(): State<S, S> = State { s -> s to s }

    fun <S> setState(s: S): State<S, Unit> = State { Unit to s }

    companion object {
        // EXER 6.10
        fun <S, A> unit(a: A): State<S, A> = State { s -> a to s }

        fun <S> getState(): State<S, S> =
            State { s -> s to s }

        fun <S> setState(s: S): State<S, Unit> =
            State { Unit to s }

        // EXER 6.6, 6.9, 6.10
        fun <S, A, B, C> map2(ra : State<S, A>, rb: State<S, B>, f: (A, B) -> C): State<S, C> =
            ra.flatMap { a ->
                rb.flatMap { b ->
                    unit(f(a, b))
                }
            }

        fun <S, A, B> both(ra: State<S, A>, rb: State<S, B>): State<S, Pair<A, B>> =
            map2(ra, rb) { a, b -> a to b}

        // EXER 6.10
        fun <A> sequence(fs: ListL<Rand<A>>): Rand<ListL<A>> =
            ListL.foldRight2<Rand<A>, Rand<ListL<A>>>(fs, Rand.unit(ListL.of())) { ra: Rand<A>, rla: Rand<ListL<A>> ->
                map2(ra, rla) { a, la -> Cons(a, la) }
            }
    }
}
