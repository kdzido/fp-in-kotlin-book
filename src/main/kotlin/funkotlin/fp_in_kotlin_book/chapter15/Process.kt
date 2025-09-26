package funkotlin.fp_in_kotlin_book.chapter15

import arrow.Kind
import arrow.Kind2
import arrow.core.andThen
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter05.Cons
import funkotlin.fp_in_kotlin_book.chapter05.Empty
import funkotlin.fp_in_kotlin_book.chapter05.Stream
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.runM
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

class ForProcess private constructor() { companion object }
typealias ProcessOf<I, O> = Kind2<ForProcess, I, O>
typealias ProcessPartialOf<I> = Kind<ForProcess, I>
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <I, O> ProcessOf<I, O>.fix(): Process<I, O> = this as Process<I, O>

fun <I, O> Process.Companion.monad() = object : ProcessMonad<I, O> {}

sealed class Process<I, O> : ProcessOf<I, O> {
    operator fun invoke(si: Stream<I>): Stream<O> =
        when (this) {
            is Emit -> Cons({ this.head }, { this.tail(si) })
            is Await -> when (si) {
                is Cons -> this.recv(Some(si.head()))(si.tail())
                Empty -> this.recv(None)(si)
            }

            is Halt -> Stream.empty()
        }

    fun repeat(): Process<I, O> {
        fun go(p: Process<I, O>): Process<I, O> {
            return when (p) {
                is Halt -> go(this)
                is Await -> Await { i: Option<I> ->
                    when (i) {
                        is None -> p.recv(None)
                        else -> go(p.recv(i))
                    }
                }
                is Emit -> Emit(p.head, go(p.tail))
            }
        }
        return go(this)
    }

    fun <O2> map(f: (O) -> O2): Process<I, O2> = this pipe lift(f)

    infix fun append(p2: Process<I, O>): Process<I, O> =
        when (this) {
            is Halt -> p2
            is Emit -> Emit(this.head, this.tail append p2)
            is Await -> Await { i: Option<I> ->
                (this.recv andThen { p1 -> p1 append p2 })(i)
            }
        }

    fun <O2> flatMap(f: (O) -> Process<I, O2>): Process<I, O2> =
        when (this) {
            is Halt -> Halt()
            is Emit -> f(this.head) append this.tail.flatMap(f)
            is Await -> Await { i: Option<I> ->
                (this.recv andThen { p -> p.flatMap(f)})(i)
            }
        }

    companion object {
    }
}

data class Emit<I, O>(
    val head: O,
    val tail: Process<I, O> = Halt()
) : Process<I, O>()

data class Await<I, O>(
    val recv: (Option<I>) -> Process<I, O>
) : Process<I, O>()

class Halt<I, O> : Process<I, O>() {
    override fun equals(other: Any?): Boolean = true
    override fun hashCode(): Int = 123
}

infix fun <I, O, O2> Process<I, O>.pipe(
    g: Process<O, O2>,
): Process<I, O2> =
    when (g) {
        is Await -> when(this) {
            is Await -> Await { i -> this.recv(i) pipe g }
            is Emit -> this.tail pipe g.recv(Some(this.head))
            is Halt -> Halt<I, O>() pipe g.recv(None)
        }
        is Emit -> Emit(g.head, this pipe g.tail)
        is Halt-> Halt()
    }


fun <I, O> lift(f: (I) -> O): Process<I, O> =
    liftOne(f).repeat()

fun <I, O> liftOne(f: (I) -> O): Process<I, O> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> Emit(f(i.value))
            None -> Halt()
        }
    }

fun <I> filter(p: (I) -> Boolean): Process<I, I> =
    Await<I, I> { i: Option<I> ->
        when (i) {
            is Some -> if (p(i.value)) Emit(i.value) else Halt()
            None -> Halt()
        }
    }.repeat()

fun <I> exists(p: (I) -> Boolean): Process<I, Boolean> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> if (p(i.value)) Emit(true) else Emit(false, exists(p) )
            None -> Halt()
        }
    }

fun sum(): Process<Double, Double> {
    fun go(acc: Double): Process<Double, Double> =
        Await { i: Option<Double> ->
            when (i) {
                is Some -> Emit(i.value + acc, go(i.value + acc))
                None -> Halt()
            }
        }
    return go(0.0)
}

fun sum2(): Process<Double, Double> =
    loop(0.0, { i, s -> Pair(i + s, i + s)})

fun <I> take(n: Int): Process<I, I> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> if (n > 0) Emit(i.value, take(n - 1)) else Halt()
            None -> Halt()
        }
    }.repeat()

fun <I> drop(n: Int): Process<I, I> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> if (n > 0) drop(n - 1) else Emit(i.value)
            None -> Halt()
        }
    }.repeat()

fun <I> takeWhile(p: (I) -> Boolean): Process<I, I> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> if (p(i.value)) Emit(i.value, takeWhile(p)) else Halt()
            None -> Halt()
        }
    }.repeat()

fun <I> dropWhile(p: (I) -> Boolean): Process<I, I> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> if (p(i.value)) Halt() else Emit(i.value, dropWhile(p))
            None -> Halt()
        }
    }.repeat()

fun <I> count(): Process<I, Int> {
    fun go(acc: Int): Process<I, Int> =
        Await { i: Option<I> ->
            when (i) {
                None -> Halt()
                is Some -> Emit(acc + 1, go(acc + 1))
            }
        }
    return go(0)
}

fun <I> count2(): Process<I, Int> =
    loop(0, { i, n -> Pair(n + 1, n + 1)})

fun mean(): Process<Double, Double> {
    fun go(sum: Double, n: Int): Process<Double, Double> =
        Await { i: Option<Double> ->
            when (i) {
                is Some -> Emit((i.value + sum) / n, go(i.value + sum, n + 1))
                None -> Halt()
            }
        }
    return go(0.0, 1)
}

fun mean2(): Process<Double, Double> =
    zip(sum(), count()).map { (s: Double, c: Int) -> s / c }


fun <I, O> Process<I, O>.zipWithIndex(): Process<I, Pair<Int, O>> =
    zip(count<I>().map { it - 1 },  this)

// book solution
fun <I, A, B> zip(p1: Process<I, A>, p2: Process<I, B>): Process<I, Pair<A, B>> =
    when (p1) {
        is Halt -> Halt()
        is Await -> Await { oa -> zip(p1.recv(oa), feed(oa, p2)) }
        is Emit -> when (p2) {
            is Emit -> Emit(p1.head to p2.head, zip(p1.tail, p2.tail))
            else -> throw RuntimeException("unreachable")
        }
    }

// book solution
fun <A, B> feed(oa: Option<A>, p1: Process<A, B>): Process<A, B> =
    when (p1) {
        is Halt -> Halt()
        is Await -> p1.recv(oa)
        is Emit -> Emit(p1.head, feed(oa, p1.tail))
    }

fun <S, I, O> loop(z: S, f: (I, S) -> Pair<O, S>): Process<I, O> =
    Await { i: Option<I> ->
        when (i) {
            is Some -> {
                val (o, s2) = f(i.value, z)
                Emit(o, loop(s2, f))
            }
            None -> Halt()
        }
    }

fun <A, B> processFile(
    file: File,
    proc: Process<String, A>,
    z: B,
    fn: (B, A) -> B
): IO<B> = IO {
    tailrec fun go(
        ss: Iterator<String>,
        curr: Process<String, A>,
        acc: B
    ): B =
        when (curr) {
            is Halt -> acc
            is Await -> {
                val next = if (ss.hasNext()) curr.recv(Some(ss.next())) else curr.recv(None)
                go(ss, next, acc)
            }
            is Emit -> go(ss, curr.tail, fn(acc, curr.head))
        }

    file.bufferedReader().use { reader: BufferedReader ->
        go(reader.lines().iterator(), proc, z)
    }
}

fun convert(file: File, outFile: File): File {
    val p: Process<String, String> = lift {
        toCelsius(
            it.toDoubleOrNull() ?: throw IllegalStateException("Invalid")
        ).toString()
    }

    outFile.bufferedWriter().use { writer ->
        println("file: $file, exists: ${file.exists()}, path: ${file.absoluteFile.absolutePath}")
        runM(processFile(file, p, writer) { bw: BufferedWriter, line: String ->
            println(">> processing line: $line")
            bw.append(line)
            bw.newLine()
            bw
        })
    }
    return outFile
}

fun toCelsius(farhenheit: Double): Double =
    ((farhenheit - 32.0) * 5.0) / 9.0


fun main() {
    val of = convert(
        File("faren.txt"),
        File("result_celsius.txt")
    )
    println("output file: $of")
}
