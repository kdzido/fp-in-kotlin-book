package funkotlin.fp_in_kotlin_book.chapter13

import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some

interface IO<A> {
    companion object {
        fun <A> unit(a: () -> A) = object : IO<A> {
            override fun run(): A = a()
        }

        operator fun <A> invoke(a: () -> A) = unit(a)
    }

    fun run(): A

    fun <B> map(f: (A) -> B): IO<B> =
        object : IO<B> {
            override fun run(): B = f(this@IO.run())
        }

    fun <B> flatMap(f: (A) -> IO<B>): IO<B> =
        object : IO<B> {
            override fun run(): B = f(this@IO.run()).run()
        }

    infix fun <B> assoc(io: IO<B>): IO<Pair<A, B>> =
        object : IO<Pair<A, B>> {
            override fun run(): Pair<A, B> =
                this@IO.run() to io.run()
        }
}

data class Player(val name: String, val score: Int)

fun contest2(p1: Player, p2: Player): IO<Unit> =
    stdout(winnerMsg(winner(p1, p2)))

fun stdin(): IO<String> = IO { readLine().orEmpty() }

fun stdout(msg: String): IO<Unit> = IO { println(msg) }

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

fun main() {
//    converter()
    converter2().run()

//    println("echo: ")
//    echo.run()
//    println("readInt: ")
//    readInt.run()
//    println("readInts: ")
//    readInts.run()
}
