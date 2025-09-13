package funkotlin.fp_in_kotlin_book.chapter13

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter10.Monoid
import funkotlin.fp_in_kotlin_book.ktinactoin.fahrenheitToCelsius

sealed class ForFilesH { companion object }
typealias FilesHOf<A> = Kind<ForFilesH, A>
inline fun <A> FilesHOf<A>.fix(): FilesH<A> = this as FilesH<A>
interface FilesH<A> : FilesHOf<A>

data class OpenRead(val file: String) : FilesH<HandleR>
data class OpenWrite(val file: String) : FilesH<HandleW>
data class ReadLine(val h: HandleR) : FilesH<Option<String>>
data class WriteLine(val h: HandleW) : FilesH<Unit>

interface HandleR
interface HandleW

fun loop(f: HandleR, c: HandleW): Free<ForFilesH, Unit> =
    Suspend(ReadLine(f)).flatMap { line: Option<String> ->
        when (line) {
            None -> Return(Unit)
            is Some -> Suspend(
                WriteLine(
                    handleW {
                        fahrenheitToCelsius(line.value.toDouble())
                    })
            ).flatMap { _ -> loop(f, c) }
        }
    }

fun convertFiles() =
    Suspend(OpenRead("fahrenheit.txt")).flatMap { f ->
        Suspend(OpenWrite("celsius.txt")).map { c ->
            loop(f, c)
        }
    }

fun handleW(f: () -> Double): HandleW = object : HandleW {}

fun movingAvg(n: Int, ld: List<Double>): List<Double> = TODO()

fun <A, B> windowed(
    n: Int,
    l: List<A>,
    f: (A) -> B,
    M: Monoid<A>
): List<B> = TODO()

val lines: List<String> = listOf("1", "2")
val cs = movingAvg(
    5,
    lines.map { s: String ->
        fahrenheitToCelsius(s.toDouble())
    }).map { it.toString() }


fun main() {

}
