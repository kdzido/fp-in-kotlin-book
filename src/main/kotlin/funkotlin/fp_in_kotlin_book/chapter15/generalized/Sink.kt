package funkotlin.fp_in_kotlin_book.chapter15.generalized

import funkotlin.fp_in_kotlin_book.chapter13.fahrenheitToCelsius
import funkotlin.fp_in_kotlin_book.chapter13.io.ForIO
import funkotlin.fp_in_kotlin_book.chapter13.io.IO
import funkotlin.fp_in_kotlin_book.chapter15.generalized.Process.Companion.await1
import funkotlin.fp_in_kotlin_book.chapter15.generalized.Process.Companion.emit1
import funkotlin.fp_in_kotlin_book.chapter15.generalized.Process.Companion.tee
import java.io.FileWriter

typealias Sink<F, O> = Process<F, (O) -> Process<F, Unit>>

fun fileW(file: String, append: Boolean = false): Sink<ForIO, String> =
    resource(
        acquire = IO { FileWriter(file, append) },
        use = { fw ->
            constant { s: String ->
                eval(IO {
                    fw.write(s)
                    fw.flush()
                })
            }
        },
        release = { fw ->
            evalDrain(IO { fw.close() })
        }
    )

fun <A> constant(a: A): Process<ForIO, A> =
    eval(IO { a }).flatMap { Process.Companion.Emit(it, constant(it)) }

fun <F, I1, I2, O> zipWith(
    p1: Process<F, I1>,
    p2: Process<F, I2>,
    f: (I1, I2) -> O
): Process<F, O> =
    tee(p1, p2, zipWith(f))

fun <F, O> Process<F, O>.to(sink: Sink<F, O>): Process<F, Unit> =
    join(
        zipWith(this, sink) { o: O, fn: (O) -> Process<F, Unit> ->
            fn(o)
        }
    )

fun <F, O> join(p: Process<F, Process<F, O>>): Process<F, O> =
    p.flatMap { it }

fun converter(inputFile: String, outputFile: String): Process<ForIO, Unit> =
    lines(inputFile)
        .filter { !it.startsWith("#") }
        .map { line -> fahrenheitToCelsius(line.toDouble()).toString() }
        .pipe(intersperse("\n"))
        .to(fileW(outputFile))
        .drain()

fun <I> intersperse(sep: I): Process1<I, I> =
    await1<I, I>({ i ->
        emit1<I, I>(i).append {
            id<I>().flatMap { i2 ->
                emit1<I, I>(
                    sep
                ).append { emit1<I, I>(i2) }
            }
        }
    })

fun <I> id(): Process1<I, I> = await1({ i: I -> Process.Companion.Emit(i, id()) })
