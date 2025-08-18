package funkotlin.fp_in_kotlin_book.chapter13

import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some

interface IO {
    companion object {
        fun empty(): IO = object : IO {
            override fun run(): Unit = Unit
        }
    }
    fun run(): Unit

    fun assoc(io: IO): IO = object : IO {
        override fun run() {
            this@IO.run()
            io.run()
        }
    }
}

data class Player(val name: String, val score: Int)

fun contest2(p1: Player, p2: Player): IO =
    stdout(winnerMsg(winner(p1, p2)))

fun stdout(msg: String): IO =
    object : IO {
        override fun run() = println(msg)
    }

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
