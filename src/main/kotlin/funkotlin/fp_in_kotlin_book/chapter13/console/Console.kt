package funkotlin.fp_in_kotlin_book.chapter13.console

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.Pars

sealed class ForConsole { companion object }
typealias ConsoleOf<A> = Kind<ForConsole, A>
inline fun <A> ConsoleOf<A>.fix(): Console<A> = this as Console<A>

sealed class Console<A> : ConsoleOf<A> {
    abstract fun toPar(): Par<A>
    abstract fun toThunk(): () -> A
}

object ReadLine : Console<Option<String>>() {
    override fun toPar(): Par<Option<String>> = Pars.unit(run())
    override fun toThunk(): () -> Option<String> = { run() }

    private fun run(): Option<String> =
        try {
            Some(readLine().orEmpty())
        } catch (e: Exception) {
            None
        }
}

data class PrintLine(val line: String) : Console<Unit>() {
    override fun toPar(): Par<Unit> = Pars.lazyUnit { println(line) }
    override fun toThunk(): () -> Unit = { println(line) }
}
