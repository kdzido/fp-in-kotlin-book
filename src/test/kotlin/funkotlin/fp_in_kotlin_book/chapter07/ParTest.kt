package funkotlin.fp_in_kotlin_book.chapter07

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors

class ParTest : FunSpec({
 val pool = Executors.newFixedThreadPool(5)

 test("should create unit of Par") {
  Pars.unit(2)(pool).get() shouldBe 2
 }

 test("should sum2 ints") {
  sum2(listOf())(pool).get() shouldBe 0
  sum2(listOf(1))(pool).get() shouldBe 1
  sum2(listOf(1, 2, 3))(pool).get() shouldBe 6
 }

 test("should sum3 ints") { // hangs if not enough threads in pool
  sum3(listOf())(pool).get() shouldBe 0
  sum3(listOf(1))(pool).get() shouldBe 1
  sum3(listOf(1, 2, 3))(pool).get() shouldBe 6
 }
})
