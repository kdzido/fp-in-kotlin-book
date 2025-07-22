package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import funkotlin.fp_in_kotlin_book.chapter04.ForOption
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter08.ForGen
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.fix
import funkotlin.fp_in_kotlin_book.chapter11.flatMap

object Applicatives {

    val genApplicative = object : Applicative<ForGen> {
        override fun <A> unit(a: A): Kind<ForGen, A> = Gen.unit(a)

        override fun <A, B, C> map2(
            fa: Kind<ForGen, A>,
            fb: Kind<ForGen, B>,
            f: (A, B) -> C,
        ): Kind<ForGen, C> =
            fa.fix().flatMap { a -> fb.fix().map { b -> f(a, b) } }
    }

    fun optionApplicative(): Applicative<ForOption> = object : Applicative<ForOption> {
        override fun <A> unit(a: A): Kind<ForOption, A> = Some(a)

        override fun <A, B, C> map2(
            fa: Kind<ForOption, A>,
            fb: Kind<ForOption, B>,
            f: (A, B) -> C,
        ): Kind<ForOption, C> =
            fa.flatMap { a -> fb.flatMap { b -> unit(f(a, b)) } }
    }
}
