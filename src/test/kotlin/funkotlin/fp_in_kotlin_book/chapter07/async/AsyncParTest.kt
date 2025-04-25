package funkotlin.fp_in_kotlin_book.chapter07.async

import funkotlin.fp_in_kotlin_book.chapter07.async.AsyncPars.shouldBePar
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors

class AsyncParTest : FunSpec({
 val pool = Executors.newFixedThreadPool(10)

 test("should create unit of Par") {
  AsyncPars.run(pool, AsyncPars.unit(2)) shouldBe 2
 }

 test("should sum2 ints") {
  AsyncPars.run(pool, sum2(listOf())) shouldBe 0
  AsyncPars.run(pool, sum2(listOf(1))) shouldBe 1
  AsyncPars.run(pool, sum2(listOf(1, 2, 3))) shouldBe 6
 }

 test("should sum3 ints") { // hangs if not enough threads in pool
  AsyncPars.run(pool, sum3(listOf())) shouldBe 0
  AsyncPars.run(pool, sum3(listOf(1))) shouldBe 1
  AsyncPars.run(pool, sum3(listOf(1, 2, 3))) shouldBe 6
 }

 test("should map2") {
  val p1 = AsyncPars.lazyUnit { Thread.sleep(300); 1 }
  val p2 = AsyncPars.lazyUnit { Thread.sleep(200); 2 }

  val mp = AsyncPars.map2(p1, p2) { a, b -> a + b }
  AsyncPars.run(pool, mp) shouldBe 3
 }

 test("should sum with map3") {
  val p1 = AsyncPars.lazyUnit { Thread.sleep(50); 1 }
  val p2 = AsyncPars.lazyUnit { Thread.sleep(60); 2 }
  val p3 = AsyncPars.lazyUnit { Thread.sleep(70); 3 }

  val mp = AsyncPars.map3(p1, p2, p3) { a, b, c -> a + b + c }
  AsyncPars.run(pool, mp) shouldBe 6
 }

 test("should sum with map4") {
  val p1 = AsyncPars.lazyUnit { Thread.sleep(50); 1 }
  val p2 = AsyncPars.lazyUnit { Thread.sleep(60); 2 }
  val p3 = AsyncPars.lazyUnit { Thread.sleep(70); 3 }
  val p4 = AsyncPars.lazyUnit { Thread.sleep(70); 4 }

  val mp = AsyncPars.map4(p1, p2, p3, p4) { a, b, c, d -> a + b + c + d}
  AsyncPars.run(pool, mp) shouldBe 10
 }

 test("should sum with map5") {
  val p1 = AsyncPars.lazyUnit { Thread.sleep(50); 1 }
  val p2 = AsyncPars.lazyUnit { Thread.sleep(60); 2 }
  val p3 = AsyncPars.lazyUnit { Thread.sleep(70); 3 }
  val p4 = AsyncPars.lazyUnit { Thread.sleep(70); 4 }
  val p5 = AsyncPars.lazyUnit { Thread.sleep(30); 5 }

  val mp = AsyncPars.map5(p1, p2, p3, p4, p5) { a, b, c, d, e -> a + b + c + d + e }
  AsyncPars.run(pool, mp) shouldBe 15
 }

 test("should asyncF") {
  val ap = AsyncPars.asyncF { a: Int -> Thread.sleep(50); a + 1 }(5)
  AsyncPars.run(pool, ap) shouldBe 6
 }

 test("should sortPar") {
  val ap = AsyncPars.sortPar(AsyncPars.unit(listOf(3, 1, 4, 2)))
  AsyncPars.run(pool, ap) shouldBe listOf(1, 2, 3, 4)
 }

 test("should map") {
  val ap = AsyncPars.map(AsyncPars.unit(listOf(3, 1, 4, 2))) { a -> a.sorted() }
  AsyncPars.run(pool, ap) shouldBe listOf(1, 2, 3, 4)
 }

// test("should sequence pars") {
//  val ap = AsyncPars.sequence(listOf(AsyncPars.unit(3), AsyncPars.unit(4), AsyncPars.lazyUnit { 5 }))
//  AsyncPars.run(pool, ap) shouldBe listOf(3, 4, 5)
// }
//
// test("should parMap") {
//  val ap = Pars.parMap(listOf(3, 1, 4, 2)) { a -> a + 1 }
//  ap(pool).get() shouldBe listOf(4, 2, 5, 3)
// }

 test("should parFilter") {
  val ap = AsyncPars.parFilter(listOf(1, 2, 3, 4, 5)) { a: Int -> a % 2 == 0 }
  AsyncPars.run(pool, ap) shouldBe listOf(2, 4)
 }

 test("should parFoldLeft, find max") {
  val ap = AsyncPars.parFoldLeft(listOf(3, 5, 1, 4, 3), 0) { b, a -> Math.max(a, b) }
  AsyncPars.run(pool, ap) shouldBe 5
 }

 test("fork should not deadlock on fixed-size pool") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = AsyncPars.lazyUnit { 42 + 1 }
  val b: Par<Int> = AsyncPars.fork { a }
  (a shouldBePar b)(es) // no deadlocks, fixed
 }

 test("delay should not deadlock ") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = AsyncPars.lazyUnit { 42 + 1 }
  val b: Par<Int> = AsyncPars.delay { a }
  (a shouldBePar b)(es)
 }
})
