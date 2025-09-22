package funkotlin.fp_in_kotlin_book.chapter15

import funkotlin.fp_in_kotlin_book.chapter03.List as ListCh
import funkotlin.fp_in_kotlin_book.chapter05.Stream
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.toList
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ProcessTest : StringSpec({

    "should liftOne to process" {
        val p: Process<Int, String> = liftOne { i: Int ->
          when (i) {
              1 -> "one"
              2 -> "two"
              3 -> "three"
              else -> "<UNKNOWN>"
          }
      }

      p(Stream.of(1, 2)).toList() shouldBe ListCh.of("one")
  }

    "should lift to process" {
        val p: Process<Int, String> = lift { i: Int ->
            when (i) {
                1 -> "one"
                2 -> "two"
                3 -> "three"
                else -> "<UNKNOWN>"
            }
        }

        p(Stream.of(1, 2, 3)).toList() shouldBe ListCh.of("one", "two", "three")
    }

})
