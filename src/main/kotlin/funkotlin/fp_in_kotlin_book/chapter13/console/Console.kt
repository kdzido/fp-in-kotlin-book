package funkotlin.fp_in_kotlin_book.chapter13.console

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter04.fix
import funkotlin.fp_in_kotlin_book.chapter07.ForPar
import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.ParOf
import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter07.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monad
import funkotlin.fp_in_kotlin_book.chapter13.ForFunction0
import funkotlin.fp_in_kotlin_book.chapter13.Free
import funkotlin.fp_in_kotlin_book.chapter13.Function0
import funkotlin.fp_in_kotlin_book.chapter13.Function0Of
import funkotlin.fp_in_kotlin_book.chapter13.Suspend
import funkotlin.fp_in_kotlin_book.chapter13.Translate
import funkotlin.fp_in_kotlin_book.chapter13.console.fix
import funkotlin.fp_in_kotlin_book.chapter13.fix
import funkotlin.fp_in_kotlin_book.chapter13.flatMap
import funkotlin.fp_in_kotlin_book.chapter13.freeMonad
import funkotlin.fp_in_kotlin_book.chapter13.runFree
import funkotlin.fp_in_kotlin_book.chapter13.runTrampoline
import funkotlin.fp_in_kotlin_book.chapter13.translate
import java.util.concurrent.Executors

sealed class ForConsole { companion object }
typealias ConsoleOf<A> = Kind<ForConsole, A>
inline fun <A> ConsoleOf<A>.fix(): Console<A> = this as Console<A>

typealias ConsoleIO<A> = Free<ForConsole, A>

sealed class Console<A> : ConsoleOf<A> {
    abstract fun toPar(): Par<A>
    abstract fun toThunk(): () -> A
    abstract fun toReader(): ConsoleReader<A>

    fun <B> flatMap(f: (A) -> Console<B>): Console<B> =
        when (this) {
            is PrintLine -> TODO("Not possible")
            ReadLine -> TODO("Not possible")
        }

    companion object {
        fun stdin(): ConsoleIO<Option<String>> =
            Suspend(ReadLine)
        fun stdout(line: String): ConsoleIO<Unit> =
            Suspend(PrintLine(line))
    }
}

object ReadLine : Console<Option<String>>() {
    override fun toPar(): Par<Option<String>> =
        Pars.unit(run())
    override fun toThunk(): () -> Option<String> =
        { run() }
    override fun toReader(): ConsoleReader<Option<String>> =
        ConsoleReader({ s: String -> Some(s) })

    private fun run(): Option<String> =
        try {
            Some(readLine().orEmpty())
        } catch (e: Exception) {
            None
        }
}

data class PrintLine(val line: String) : Console<Unit>() {
    override fun toPar(): Par<Unit> =
        Pars.lazyUnit { println(line) }
    override fun toThunk(): () -> Unit =
        { println(line) }
    override fun toReader(): ConsoleReader<Unit> =
        ConsoleReader({ s: String -> Unit })
}

fun functionMonad() = object : Monad<ForFunction0> {
    override fun <A> unit(a: A): Function0Of<A> = Function0 { a }
    override fun <A, B> flatMap(
        fa: Function0Of<A>,
        f: (A) -> Function0Of<B>
    ): Function0Of<B> = { f(fa.fix().f()) }()
}

fun parMonad() = object : Monad<ForPar> {
    override fun <A> unit(a: A): ParOf<A> = Pars.unit(a)
    override fun <A, B> flatMap(
        fa: ParOf<A>,
        f: (A) -> ParOf<B>
    ): ParOf<B> = Pars.flatMap(fa.fix()) { a -> f(a).fix() }
}

fun consoleToFunction0() = object : Translate<ForConsole, ForFunction0> {
    override fun <A> invoke(fa: Kind<ForConsole, A>): Kind<ForFunction0, A> = Function0(fa.fix().toThunk())
}
fun consoleToPar() = object : Translate<ForConsole, ForPar> {
    override fun <A> invoke(fa: Kind<ForConsole, A>): Kind<ForPar, A> = fa.fix().toPar()
}
fun consoleToReader() = object : Translate<ForConsole, ForConsoleReader> {
    override fun <A> invoke(fa: Kind<ForConsole, A>): Kind<ForConsoleReader, A> = fa.fix().toReader()
}

fun <A> runConsolePar(a: Free<ForConsole, A>): Par<A> =
    runFree(a, consoleToPar(), parMonad()).fix().fix()

// not stack-safe
fun <A> runConsoleFunction0(a: Free<ForConsole, A>): Function0<A> =
    runFree(a, consoleToFunction0(), functionMonad()).fix()

fun <A> runConsoleReader(a: ConsoleIO<A>): ConsoleReader<A> =
    runFree(a, consoleToReader(), ConsoleReader.monad()).fix().fix()

fun <A> runConsole(a: Free<ForConsole, A>): A {
    val t = object : Translate<ForConsole, ForFunction0> {
        override fun <A> invoke(ca: ConsoleOf<A>): Function0Of<A> =
            Function0(ca.fix().toThunk())
    }
    return runTrampoline(translate(a, t))
}

fun main() {
    val f1: Free<ForConsole, Option<String>> =
        Console.stdout("I can only interact with the console")
            .flatMap { _ -> Console.stdin() }

//    runConsoleFunction0(f1).fix().f() // not stack-safe
//    runConsole(f1)

//    val pool = Executors.newFixedThreadPool(1)  // hangs, awaiting input
//    runConsolePar(f1).fix().run(pool)

    val cr = runConsoleReader(f1).fix().run("one")
    println("runConsoleReader: $cr")
}
