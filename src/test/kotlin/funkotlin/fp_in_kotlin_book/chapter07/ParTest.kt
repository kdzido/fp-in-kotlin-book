package funkotlin.fp_in_kotlin_book.chapter07

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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

 test("should map2") {
  val p1 = Pars.lazyUnit { Thread.sleep(300); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(200); 2 }

  val mp = Pars.map2(p1, p2) { a, b -> a + b }
  mp(pool).get() shouldBe 3
 }

 test("should pass with timeout of map2") {
  val p1 = Pars.lazyUnit { Thread.sleep(300); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(200); 2 }

  val mp = Pars.map2(p1, p2) { a, b -> a + b }
  mp(pool).get(400, TimeUnit.MILLISECONDS) shouldBe 3
 }

 test("should throw on timeout of map2") {
  val p1 = Pars.lazyUnit { Thread.sleep(300); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(200); 2 }

  val mp = Pars.map2(p1, p2) { a, b -> a + b }
  shouldThrow<TimeoutException> {
   mp(pool).get(250, TimeUnit.MILLISECONDS)
  }
 }
})
