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

    "!should emit infinitely" {
        val units = Stream.constant(Unit)
        val p = lift<Unit, Int>{ _ -> 1 }(units)

        p.toList() // runs infinetely
    }

    "should filter stream" {
        val even = filter<Int> { it % 2 == 0 }

        even(Stream.of(1, 2, 3, 4, 5, 6)).toList() shouldBe ListCh.of(2, 4, 6)
    }

    "should sum stream" {
        sum()(Stream.of(1.0, 2.0, 3.0)).toList() shouldBe ListCh.of(1.0, 3.0, 6.0)
    }

})

class Exercise15_1 : StringSpec({
    val stream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    "should take" {
        take<Int>(5)(stream).toList() shouldBe ListCh.of(1, 2, 3, 4, 5)
    }

    "should drop" {
        drop<Int>(5)(stream).toList() shouldBe ListCh.of(6, 7, 8, 9, 10)
    }

    "should takeWhile" {
        takeWhile<Int>({ it <= 4 })(stream).toList() shouldBe ListCh.of(1, 2, 3, 4)
    }

    "should dropWhile" {
        dropWhile<Int>({ it <= 4 })(stream).toList() shouldBe ListCh.of(5, 6, 7, 8, 9, 10)
    }
})
