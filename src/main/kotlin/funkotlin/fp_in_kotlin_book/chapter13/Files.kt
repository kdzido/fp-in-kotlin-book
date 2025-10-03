package funkotlin.fp_in_kotlin_book.chapter13

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.Tailrec
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.farenheitToCelsius


sealed class ForFiles { companion object }
typealias FilesOf<A> = Kind<ForFiles, A>
inline fun <A> FilesOf<A>.fix(): Files<A> = this as Files<A>
interface Files<A> : FilesOf<A>

data class ReadLines(
    val file: String,
) : Files<List<String>>

data class WriteLines(
    val file: String,
    val lines: List<String>,
): Files<Unit>

fun fahrenheitToCelsius(f: Double): Double = (f - 32) * 5.0 / 9.0

fun main() {
    val p: Free<ForFiles, Unit> =
        Suspend(ReadLines("fahrenheit.txt")).flatMap { lines ->
            Suspend(WriteLines("celsius.txt", lines.map { s ->
                farenheitToCelsius(s.toDouble()).toString()
            }))
        }
}
