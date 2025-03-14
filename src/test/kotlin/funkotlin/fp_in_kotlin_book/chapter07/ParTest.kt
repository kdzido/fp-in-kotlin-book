package funkotlin.fp_in_kotlin_book.chapter07

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ParTest : FunSpec({
 test("should sum ints") {
  sum(listOf<Int>()) shouldBe 0
  sum(listOf(1)) shouldBe 1
  sum(listOf(1, 2, 3)) shouldBe 6
 }

 test("should sum2 ints") {
  sum2(listOf<Int>()).get shouldBe 0
  sum2(listOf(1)).get shouldBe 1
  sum2(listOf(1, 2, 3)).get shouldBe 6
 }

 test("should get Par<Int>") {
  Par(1).get shouldBe 1
 }

 test("should create unit of Par") {
  unit({ 2 }).get shouldBe 2
 }

 test("should get Par<Int>") {
  get(Par(3)) shouldBe 3
 }
})
