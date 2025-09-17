package funkotlin.fp_in_kotlin_book.chapter14

import arrow.Kind
import arrow.Kind2

interface RunnableST<A> {
    fun <S> invoke(): ST<S, A>
}

abstract class STRef<S, A> private constructor() {
    companion object {
        operator fun <S, A> invoke(a: A): ST<S, STRef<S, A>> = ST {
            object : STRef<S, A>() {
                override var cell: A = a
            }
        }
    }

    protected abstract var cell: A

    fun read(): ST<S, A> = ST<S, A> {
        cell
    }.fix()

    fun write(a: A): ST<S, Unit> = object : ST<S, Unit>() {
        override fun run(s: S): Pair<Unit, S> {
            cell = a
            return Unit to s
        }

    }
}

class ForST private constructor() { companion object }
typealias STOf<S, A> = Kind2<ForST, S, A>
typealias STPartialOf<S> = Kind<ForST, S>
fun <S, T> STOf<S, T>.fix() = this as ST<S, T>

// ST stands for state token, state tag
abstract class ST<S, A> internal constructor() : STOf<S, A> {
    companion object {
        operator fun <S, A> invoke(a: () -> A): ST<S, A> {
            val memo by lazy(a) // <2>
            return object : ST<S, A>() {
                override fun run(s: S) = memo to s
            }
        }

        fun  <A> runST(st: RunnableST<A>): A =
            st.invoke<Unit>().run(Unit).first
    }

    protected abstract fun run(s: S): Pair<A, S>

    fun <B> map(f: (A) -> B): ST<S, B> = object : ST<S, B>() {
        override fun run(s: S): Pair<B, S> {
            val (a, s1) = this@ST.run(s)
            return f(a) to s1
        }
    }

    fun <B> flatMap(f: (A) -> ST<S, B>): ST<S, B> = object : ST<S, B>() {
        override fun run(s: S): Pair<B, S> {
            val (a, s1) = this@ST.run(s)
            return f(a).run(s1)
        }
    }
}


