package funkotlin.fp_in_kotlin_book.chapter07

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ParTest : FunSpec({
 test("should sum ints") {
  sum(listOf<Int>()) shouldBe 0
  sum(listOf(1)) shouldBe 1
  sum(listOf(1, 2, 3)) shouldBe 6
 }
})
