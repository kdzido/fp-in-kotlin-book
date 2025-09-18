package funkotlin.fp_in_kotlin_book.chapter14

import arrow.Kind
import arrow.Kind2
import arrow.core.Option
import okhttp3.internal.toImmutableMap

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

abstract class STArray<S, A> @PublishedApi internal constructor() {
    companion object {
        inline operator fun <S, reified A> invoke(
            sz: Int,
            v: A
        ): ST<S, STArray<S, A>> = ST {
            object : STArray<S, A>() {
                override val value = Array(sz) { v}
            }
        }
    }

    protected abstract val value: Array<A>

    val size: ST<S, Int> = ST { value.size }
    fun write(i: Int, a: A): ST<S, Unit> = object : ST<S, Unit>() {
        override fun run(s: S): Pair<Unit, S> {
            value[i] = a
            return Unit to s
        }
    }
    fun read(i: Int): ST<S, A> = ST { value[i] }

    fun swap(i: Int, j: Int): ST<S, Unit> = read(i).flatMap { x ->
        read(j).flatMap { y ->
            write(i, y).flatMap {
                write(j, x)
            }
        }
    }

    fun freeze(): ST<S, List<A>> = ST { value.toList() }
}

fun <S, A> STArray<S, A>.fill(xs: Map<Int, A>): ST<S, Unit> =
    xs.entries.fold(ST { Unit }) { acc, e ->
        acc.flatMap { _: Unit -> this.write(e.key, e. value)  }
    }

inline fun <S, reified A> fromList(
    xs: List<A>
): ST<S, STArray<S, A>> =
    ST {
        object : STArray<S, A>() {
            override val value: Array<A> = xs.toTypedArray()
        }
    }

abstract class STMap<S, K, V> @PublishedApi internal constructor() {
    companion object {
        inline operator fun <S, reified K, reified V> invoke(): ST<S, STMap<S, K, V>> =
            ST {
                object : STMap<S, K, V>() {
                    override val map: MutableMap<K, V> = mutableMapOf()
                }
            }

        fun <S, K, V> fromMap(map: Map<K, V>): ST<S, STMap<S, K, V>> =
            ST {
                object : STMap<S, K, V>() {
                    override val map: MutableMap<K, V> = map.toMutableMap()
                }
            }
    }

    protected abstract val map: MutableMap<K, V>

    val size: ST<S, Int> = ST { map.size }

    fun gen(k: K): ST<S, V> = object : ST<S, V>() {
        override fun run(s: S): Pair<V, S> =
            map.getOrElse(k, noElementFor(k)) to s
    }

    fun genOption(k: K): ST<S, Option<V>> = object : ST<S, Option<V>>() {
        override fun run(s: S): Pair<Option<V>, S> =
            Option.fromNullable(map[k]) to s
    }

    fun put(k: K, v: V): ST<S, Unit> = object : ST<S, Unit>() {
        override fun run(s: S): Pair<Unit, S> {
            map[k] = v
            return Unit to s
        }
    }

    fun remove(k: K): ST<S, Unit> = object : ST<S, Unit>() {
        override fun run(s: S): Pair<Unit, S> {
            map.remove(k)
            return Unit to s
        }
    }

    fun clear(): ST<S, Unit> = object : ST<S, Unit>() {
        override fun run(s: S): Pair<Unit, S> {
            map.clear()
            return Unit to s
        }
    }

    fun freeze(): ST<S, Map<K, V>> =
        ST { map.toImmutableMap() }

    private fun noElementFor(k: K): () -> Nothing =
        { throw NoSuchElementException("No value for key $k") }
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


