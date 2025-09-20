package funkotlin.fp_in_kotlin_book.chapter15

import arrow.core.extensions.sequence.foldable.exists
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.Tailrec
import funkotlin.fp_in_kotlin_book.chapter13.tailrec.runM
import funkotlin.fp_in_kotlin_book.chapter15.Counting.FILE_10
import funkotlin.fp_in_kotlin_book.chapter15.Counting.FILE_20
import funkotlin.fp_in_kotlin_book.chapter15.Counting.FILE_ABRA
import funkotlin.fp_in_kotlin_book.chapter15.Counting.LINE_LIMIT
import java.io.File

typealias IO<A> = Tailrec<A>

object Counting {
    const val FILE_10 = "./src/main/kotlin/funkotlin/fp_in_kotlin_book/chapter15/lines10.txt"
    const val FILE_20 = "./src/main/kotlin/funkotlin/fp_in_kotlin_book/chapter15/lines20.txt"
    const val FILE_ABRA = "./src/main/kotlin/funkotlin/fp_in_kotlin_book/chapter15/abra.txt"
    const val LINE_LIMIT = 10

    fun linesGtLimit(fileName: String, limit: Int): IO<Boolean> = IO {
        val src = File(fileName)
        val br = src.bufferedReader()
        try {
            val lines = br.lineSequence()
            lines.filter { it.trim().isNotBlank() }
                .withIndex()
                .exists { it.index >= limit }
        } finally {
            br.close()
        }
    }

    fun firstLettersBeforeLimit(fileName: String, limit: Int): IO<Int> = IO {
        val src = File(fileName)
        val br = src.bufferedReader()
        try {
            val lines = br.lineSequence()
            lines.filter { it.trim().isNotBlank() }
                .take(limit)
                .map { it.first() }
                .joinToString("")
                .indexOf("abracadabra")
        } finally {
            br.close()
        }
    }

    fun lines(fileName: String): IO<Sequence<String>> = IO {
        val src = File(fileName)
        val br = src.bufferedReader()
        val end: String by lazy {
            br.close()
            System.lineSeparator()
        }

        sequence {
            yieldAll(br.lineSequence())
            yield(end)
        }
    }

}

fun main() {
    val res10 = runM(Counting.linesGtLimit(FILE_10, LINE_LIMIT))
    println("file_10: $res10")

    val res20 = runM(Counting.linesGtLimit(FILE_20, LINE_LIMIT))
    println("file_20: $res20")

    val resAbra = runM(Counting.firstLettersBeforeLimit(FILE_ABRA, 50))
    println("abra_50: $resAbra")

    val resLines = runM(Counting.lines(FILE_10))
    println("lines count: ${resLines.count()}")

}
