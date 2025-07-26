package funkotlin.fp_in_kotlin_book.chapter12

import arrow.Kind
import arrow.Kind2
import funkotlin.fp_in_kotlin_book.chapter03.List as ListL
import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import java.text.SimpleDateFormat
import java.util.Date

data class WebForm(val f1: String, val f2: Date, val f3: String)


class ForValidation private constructor() { companion object }
typealias ValidationOf<E, A> = Kind2<ForValidation, E, A>
typealias ValidationPartialOf<E> = Kind<ForValidation, E>
fun <E, A> ValidationOf<E, A>.fix() = this as Validation<E, A>

sealed class Validation<out E, out A> : ValidationOf<E, A>

data class Failure<E>(
    val head: E,
    val tail: ListL<E> = ListL.empty(),
) : Validation<E, Nothing>()

data class Success<A>(val a: A) : Validation<Nothing, A>()

fun <E, A, B> Validation<E, A>.flatMap(f: (A) -> Validation<E, B>): Validation<E, B> = when (this) {
    is Failure -> this
    is Success -> f(this.a)
}

fun <E, A, B> Validation<E, A>.map(f: (A) -> B): Validation<E, B> = when (this) {
    is Failure -> this
    is Success -> Success(f(this.a))
}

fun <E, A, B, C> map2(
    ae: Validation<E, A>,
    be: Validation<E, B>,
    f: (A, B) -> C,
): Validation<E, C> =
    ae.flatMap { a ->
        be.map { b ->
            f(a, b)
        }
    }

object Validations {
    fun validName(name: String): Validation<String, String> = when {
        name.isNotBlank() -> Success(name)
        else -> Failure("<Name cannot be empty>")
    }
    fun validDateOfBirth(dob: String): Validation<String, Date> =
        try {
            Success(SimpleDateFormat("yyyy-MM-dd").parse(dob))
        } catch (e: Exception) {
            Failure("<Date of birth must be in format yyyy-MM-dd>")
        }

    fun validPhone(phone: String): Validation<String, String> = when {
        phone.matches("[0-9]{10}".toRegex()) -> Success(phone)
        else -> Failure("<Phone number must be 10 digits>")
    }
}

object ValidationsEither {
    fun validName(name: String): Either<String, String> = when {
        name.isNotBlank() -> Right(name)
        else -> Left("<Invalid name>")
    }
    fun validDateOfBirth(dob: String): Either<String, Date> = when {
        dob.isNotBlank() -> Right(Date(1234567))
        else -> Left("<Invalid date>")
    }
    fun validPhone(phone: String): Either<String, String> = when {
        phone.isNotBlank() -> Right(phone)
        else -> Left("<Invalid phone>")
    }
}


