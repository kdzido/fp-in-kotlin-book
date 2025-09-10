package funkotlin.fp_in_kotlin_book.chapter13.tailrec

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter13.IOMonad
import funkotlin.fp_in_kotlin_book.chapter13.toStream

sealed class ForTailrec { companion object }
typealias TailrecOf<A> = Kind<ForTailrec, A>
inline fun <A> TailrecOf<A>.fix(): Tailrec<A> = this as Tailrec<A>

data class IORef<A>(var value: A) {
    fun set(a: A): Tailrec<A> = Suspend { value = a; a }
    fun get(): Tailrec<A> = Suspend { value }
    fun modify(f: (A) -> A): Tailrec<A> = get().flatMap { a -> set(f(a)) }
}

sealed class Tailrec<A> : TailrecOf<A> {
    companion object {
        fun <A> unit(a: () -> A): Tailrec<A> =
            Suspend(a)

        operator fun <A> invoke(a: () -> A) = unit(a)

        fun ref(i: Int): Tailrec<IORef<Int>> = Suspend { IORef(i) }
    }

    fun <B> map(f: (A) -> B): Tailrec<B> =
        flatMap { a -> Return(f(a)) }

    fun <B> flatMap(f: (A) -> Tailrec<B>): Tailrec<B> =
        FlatMap(this, f)

    infix fun <B> assoc(io: Tailrec<B>): Tailrec<Pair<A, B>> =
        unit { runM(this@Tailrec) to runM(io) }
}

data class Return<A>(val a: A) : Tailrec<A>()
data class Suspend<A>(val resume: () -> A) : Tailrec<A>()
data class FlatMap<A, B>(
    val sub: Tailrec<A>,
    val f: (A) -> Tailrec<B>,
) : Tailrec<B>()

fun Tailrec.Companion.monad(): IOMonad = object : IOMonad {}

data class Player(val name: String, val score: Int)

fun contest2(p1: Player, p2: Player): Tailrec<Unit> =
    stdout(winnerMsg(winner(p1, p2)))

fun stdin(): Tailrec<String> = Suspend { readLine().orEmpty() }

fun stdout(msg: String): Tailrec<Unit> = Suspend { println(msg) }

fun contest(p1: Player, p2: Player): Unit =
    println(winnerMsg(winner(p1, p2)))

fun winnerMsg(p: Option<Player>): String = when (p) {
    None -> "It's a draw!"
    is Some -> "${p.value.name} is the winner!"
}

fun winner(p1: Player, p2: Player): Option<Player> = when {
    p1.score > p2.score -> Some(p1)
    p1.score < p2.score -> Some(p2)
    else -> None
}

fun farenheitToCelsius(f: Double): Double = (f - 32) * 5.0 / 9.0

@Deprecated("Use converter2")
fun converter() {
    println("Enter a temperature in degrees Farenheit: ")
    val d = readLine().orEmpty().toDouble()
    println(farenheitToCelsius(d))
}

fun converter2(): Tailrec<Unit> =
    stdout("Enter a temperature in degrees Farenheit: ").flatMap {
        stdin().map { it.toDouble() }.flatMap { df ->
            stdout("Degrees Celsius: ${farenheitToCelsius(df)}")
        }
    }

val echo: Tailrec<Unit> = stdin().flatMap(::stdout)
val readInt: Tailrec<Int> = stdin().map { it.toInt() }
val readInts: Tailrec<Pair<Int, Int>> = readInt assoc readInt

val ioMonad: IOMonad = Tailrec.monad()

private fun factorial(n: Int): Tailrec<Int> =
    Tailrec.ref(1).flatMap { acc: IORef<Int> ->
        ioMonad.foreachM((1..n).toStream()) { i ->
            acc.modify { it * i }.map { Unit }
        }.fix().flatMap {
            acc.get()
        }
    }

private val help = """
        | The Amazing Factorial REPL, v0.1
        | q - quit
        | <number> - compute the factorial of the given number
        | <anything else> - bomb with horrible error
        """.trimMargin("|")

val factorialREPL: Tailrec<Unit> =
    ioMonad.sequenceDiscard(
        Tailrec { println(help) }.fix(),
        ioMonad.doWhile(Tailrec { readLine().orEmpty() }) { line ->
            ioMonad.whenM(line != "q") {
                factorial(line.toInt()).flatMap { n ->
                    Tailrec { println("factorial: $n") }
                }
            }
        }.fix()
    ).fix()

tailrec fun <A> runM(io: Tailrec<A>): A =
    when (io) {
        is Return -> io.a
        is Suspend -> io.resume()
        is FlatMap<*, *> -> {
            val x = io.sub as Tailrec<A>
            val f = io.f as (A) -> Tailrec<A>
            when (x) {
                is Return -> runM(f(x.a))
                is Suspend -> runM(f(x.resume()))
                is FlatMap<*, *> -> {
                    val g = x.f as (A) -> Tailrec<A>
                    val y = x.sub as Tailrec<A>
                    runM(y.flatMap { a: A -> g(a).flatMap(f) })
                }
            }
        }
    }

fun main() {
//    converter()
//    converter2().run()

//    println("echo: ")
//    echo.run()
//    println("readInt: ")
//    readInt.run()
//    println("readInts: ")
//    readInts.run()

//    factorialREPL.run()
    runM(factorialREPL)
}
