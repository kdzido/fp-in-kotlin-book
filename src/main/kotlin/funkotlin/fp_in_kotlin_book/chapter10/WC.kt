package funkotlin.fp_in_kotlin_book.chapter10

sealed class WC

data class Stub(val chars: String)
data class Part(val ls: String, val words: Int, val rs: String)
