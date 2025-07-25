package funkotlin.fp_in_kotlin_book.chapter10

import kotlin.math.min

sealed class WC

data class Stub(val chars: String) : WC()
data class Part(val ls: String, val words: Int, val rs: String) : WC()

val wordCount: Monoid<WC> = object : Monoid<WC> {
    override fun combine(a1: WC, a2: WC): WC =
        when {
            a1 is Stub && a2 is Stub ->
                Stub(a1.chars + a2.chars)
            a1 is Stub && a2 is Part ->
                Part(a1.chars + a2.ls, a2.words, a2.rs)
            a1 is Part && a2 is Stub ->
                Part(a1.ls, a1.words, a1.rs + a2.chars)
            a1 is Part && a2 is Part ->
                Part(a1.ls, a1.words + isThereAdditionalWord(a1.rs, a2.ls) + a2.words, a2.rs)
            else -> TODO("Not supported combination")
        }

    override val nil: WC get() = Stub("")
}

private fun isThereAdditionalWord(rs: String, ls: String): Int =
    if ((rs + ls).isEmpty()) 0 else 1

fun wordCount(s: String): Int =
    when (val wc = foldMap(s.asSequence().toList(), wordCount, { it: Char -> wc(it) })) {
        is Stub -> unstub(wc.chars)
        is Part -> unstub(wc.ls) + wc.words + unstub(wc.rs)
    }

private fun wc(c: Char): WC =
    if (c.isWhitespace()) Part("", 0, "") else Stub("$c")

private fun unstub(c: String): Int = min(c.length, 1)
