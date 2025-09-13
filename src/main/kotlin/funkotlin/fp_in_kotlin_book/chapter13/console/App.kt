package funkotlin.fp_in_kotlin_book.chapter13.console

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter07.ForPar
import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.fix
import funkotlin.fp_in_kotlin_book.chapter11.Monad
import funkotlin.fp_in_kotlin_book.chapter13.Free
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias IO<A> = Free<ForPar, A>

abstract class App {
    fun main(args: Array<String>) {
        val pool = Executors.newFixedThreadPool(8)
        unsafePerformIO(pureMain(args), pool)
    }

    private fun <A> unsafePerformIO(
        ioa: IO<A>,
        pool: ExecutorService,
    ): A =
        run(ioa, parMonad()).fix().run(pool).get()

        abstract fun pureMain(args: Array<String>): IO<Unit>
}

internal fun <A> run(io: IO<A>, m: Monad<ForPar>): Kind<ForPar, A> =
    TODO()
