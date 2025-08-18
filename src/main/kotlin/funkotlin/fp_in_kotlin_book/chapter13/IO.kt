package funkotlin.fp_in_kotlin_book.chapter13

import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some

fun main() {
    val p1 = Player("Joe", 3)
    val p2 = Player("Adam", 4)
    contest(p1, p2)
}

data class Player(val name: String, val score: Int)

fun contest(p1: Player, p2: Player): Unit = when (val player = winner(p1, p2)) {
    is Some ->
        println("${player.value.name} is the winner!")
    is None ->
        println("It's a draw!")
}

fun winner(p1: Player, p2: Player): Option<Player> = when {
    p1.score > p2.score -> Some(p1)
    p1.score < p2.score -> Some(p2)
    else -> None
}
