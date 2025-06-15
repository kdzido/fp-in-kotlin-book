package funkotlin.fp_in_kotlin_book.chapter08

import funkotlin.fp_in_kotlin_book.chapter07.Par
import funkotlin.fp_in_kotlin_book.chapter07.Pars.map2

fun <A> equal(p1: Par<A>, p2: Par<A>): Par<Boolean> =
    map2(p1, p2, { a, b -> a == b })
