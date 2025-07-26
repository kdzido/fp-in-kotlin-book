package funkotlin.fp_in_kotlin_book.chapter12

import funkotlin.fp_in_kotlin_book.chapter04.Either
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import java.util.Date

data class WebForm(val f1: String, val f2: Date, val f3: String)

object Validations {
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

