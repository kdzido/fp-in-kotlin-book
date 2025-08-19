package funkotlin.fp_in_kotlin_book.chapter13

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some

sealed class ForIO { companion object }
typealias IOOf<A> = Kind<ForIO, A>
inline fun <A> IOOf<A>.fix(): IO<A> = this as IO<A>

data class IORef<A>(var value: A) {
    fun set(a: A): IO<A> = Suspend { value = a; a }
    fun get(): IO<A> = Suspend { value }
    fun modify(f: (A) -> A): IO<A> = get().flatMap { a -> set(f(a)) }
}

sealed class IO<A> : IOOf<A> {
    companion object {
        fun <A> unit(a: () -> A): IO<A> =
            Suspend(a)

        operator fun <A> invoke(a: () -> A) = unit(a)

        fun ref(i: Int): IO<IORef<Int>> = Suspend { IORef(i) }
    }

    fun <B> map(f: (A) -> B): IO<B> =
        flatMap { a -> Return(f(a)) }

    fun <B> flatMap(f: (A) -> IO<B>): IO<B> =
        FlatMap(this, f)

    infix fun <B> assoc(io: IO<B>): IO<Pair<A, B>> =
        unit { runM(this@IO) to runM(io) }
}

data class Return<A>(val a: A) : IO<A>()
data class Suspend<A>(val resume: () -> A) : IO<A>()
data class FlatMap<A, B>(
    val sub: IO<A>,
    val f: (A) -> IO<B>,
) : IO<B>()

fun IO.Companion.monad(): IOMonad = object : IOMonad {}

data class Player(val name: String, val score: Int)

fun contest2(p1: Player, p2: Player): IO<Unit> =
    stdout(winnerMsg(winner(p1, p2)))

fun stdin(): IO<String> = Suspend { readLine().orEmpty() }

fun stdout(msg: String): IO<Unit> = Suspend { println(msg) }

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

fun converter2(): IO<Unit> =
    stdout("Enter a temperature in degrees Farenheit: ").flatMap {
        stdin().map { it.toDouble() }.flatMap { df ->
            stdout("Degrees Celsius: ${farenheitToCelsius(df)}")
        }
    }

val echo: IO<Unit> = stdin().flatMap(::stdout)
val readInt: IO<Int> = stdin().map { it.toInt() }
val readInts: IO<Pair<Int, Int>> = readInt assoc readInt

val ioMonad: IOMonad = IO.monad()

private fun factorial(n: Int): IO<Int> =
    IO.ref(1).flatMap { acc: IORef<Int> ->
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

val factorialREPL: IO<Unit> =
    ioMonad.sequenceDiscard(
        IO { println(help) }.fix(),
        ioMonad.doWhile(IO { readLine().orEmpty() }) { line ->
            ioMonad.whenM(line != "q") {
                factorial(line.toInt()).flatMap { n ->
                    IO { println("factorial: $n") }
                }
            }
        }.fix()
    ).fix()

tailrec fun <A> runM(io: IO<A>): A =
    when (io) {
        is Return -> io.a
        is Suspend -> io.resume()
        is FlatMap<*, *> -> {
            val x = io.sub as IO<A>
            val f = io.f as (A) -> IO<A>
            when (x) {
                is Return -> runM(f(x.a))
                is Suspend -> runM(f(x.resume()))
                is FlatMap<*, *> -> {
                    val g = x.f as (A) -> IO<A>
                    val y = x.sub as IO<A>
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
