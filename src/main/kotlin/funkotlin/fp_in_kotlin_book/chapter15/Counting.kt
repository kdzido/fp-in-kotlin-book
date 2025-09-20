package funkotlin.fp_in_kotlin_book.chapter15

import funkotlin.fp_in_kotlin_book.chapter13.tailrec.Tailrec
import java.io.File

typealias IO<A> = Tailrec<A>

object Counting {
    fun linesGt40k(fileName: String): Tailrec<Boolean> = IO {
        val limit = 40_000
        val src = File(fileName)
        val br = src.bufferedReader()
        try {
            var count = 0
            val lines = br.lineSequence().iterator()
            while (count <= limit && lines.hasNext()) {
                lines.next()
                count += 1
            }
            count > limit
        } finally {
            br.close()
        }
    }

}
