package funkotlin.fp_in_kotlin_book.chapter07

import funkotlin.fp_in_kotlin_book.chapter07.Pars
import funkotlin.fp_in_kotlin_book.chapter07.Pars.shouldBePar
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ParTest : FunSpec({
 val pool = Executors.newFixedThreadPool(10)

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

  val mp = Pars.map2(p1, p2) { a, b -> a + b }(pool)
  mp.get() shouldBe 3
 }

 test("should sum with map3") {
  val p1 = Pars.lazyUnit { Thread.sleep(50); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(60); 2 }
  val p3 = Pars.lazyUnit { Thread.sleep(70); 3 }

  val mp = Pars.map3(p1, p2, p3) { a, b, c -> a + b + c }(pool)
  mp.get() shouldBe 6
 }

 test("should sum with map4") {
  val p1 = Pars.lazyUnit { Thread.sleep(50); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(60); 2 }
  val p3 = Pars.lazyUnit { Thread.sleep(70); 3 }
  val p4 = Pars.lazyUnit { Thread.sleep(70); 4 }

  val mp = Pars.map4(p1, p2, p3, p4) { a, b, c, d -> a + b + c + d}(pool)
  mp.get() shouldBe 10
 }

 test("should sum with map5") {
  val p1 = Pars.lazyUnit { Thread.sleep(50); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(60); 2 }
  val p3 = Pars.lazyUnit { Thread.sleep(70); 3 }
  val p4 = Pars.lazyUnit { Thread.sleep(70); 4 }
  val p5 = Pars.lazyUnit { Thread.sleep(30); 5 }

  val mp = Pars.map5(p1, p2, p3, p4, p5) { a, b, c, d, e -> a + b + c + d + e }(pool)
  mp.get() shouldBe 15
 }

 test("should pass with timeout of map2") {
  val p1 = Pars.lazyUnit { Thread.sleep(300); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(200); 2 }

  val mp = Pars.map2(p1, p2) { a, b -> a + b }(pool)
  mp.get(400, TimeUnit.MILLISECONDS) shouldBe 3
 }

 test("should throw on timeout of map2") {
  val p1 = Pars.lazyUnit { Thread.sleep(300); 1 }
  val p2 = Pars.lazyUnit { Thread.sleep(200); 2 }

  val mp = Pars.map2(p1, p2) { a, b -> a + b }(pool)
  shouldThrow<TimeoutException> {
   mp.get(250, TimeUnit.MILLISECONDS)
  }
 }

 test("should asyncF") {
  val ap: Future<Int> = Pars.asyncF { a: Int -> Thread.sleep(50); a + 1 }(5)(pool)
  ap.get() shouldBe 6
 }

 test("should sortPar") {
  val ap = Pars.sortPar(Pars.unit(listOf(3, 1, 4, 2)))
  ap(pool).get() shouldBe listOf(1, 2, 3, 4)
 }

 test("should map") {
  val ap = Pars.map(Pars.unit(listOf(3, 1, 4, 2))) { a -> a.sorted() }
  ap(pool).get() shouldBe listOf(1, 2, 3, 4)
 }

 test("should parMap") {
  val ap = Pars.parMap(listOf(3, 1, 4, 2)) { a -> a + 1 }
  ap(pool).get() shouldBe listOf(4, 2, 5, 3)
 }

 test("should sequence pars") {
  val ap = Pars.sequence(listOf(Pars.unit(3), Pars.unit(4), Pars.lazyUnit { 5 }))
  ap(pool).get() shouldBe listOf(3, 4, 5)
 }

 test("should parFilter") {
  val ap = Pars.parFilter(listOf(1, 2, 3, 4, 5)) { a: Int -> a % 2 == 0 }
  ap(pool).get() shouldBe listOf(2, 4)
 }

 test("should parFoldLeft, find max") {
  val ap = Pars.parFoldLeft(listOf(3, 5, 1, 4, 3), 0) { b, a -> Math.max(a, b) }
  ap(pool).get() shouldBe 5
 }

 test("fork should deadlock on fixed-size pool") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = Pars.lazyUnit { 42 + 1 }
  val b: Par<Int> = Pars.fork { a }
//  (a shouldBePar b)(es) // deadlocks
 }

 test("delay should not deadlock ") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = Pars.lazyUnit { 42 + 1 }
  val b: Par<Int> = Pars.delay { a }
  (a shouldBePar b)(es) // deadlocks
 }

 test("should choice based on Bool") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = Pars.lazyUnit { 42 }
  val b: Par<Int> = Pars.lazyUnit { 8 }

  (Pars.choice(Pars.lazyUnit { true }, a, b) shouldBePar Pars.unit(42))(es)
  (Pars.choice(Pars.lazyUnit { false }, a, b) shouldBePar Pars.unit(8))(es)
  // and
  (Pars.choice2(Pars.lazyUnit { true }, a, b) shouldBePar Pars.unit(42))(es)
  (Pars.choice2(Pars.lazyUnit { false }, a, b) shouldBePar Pars.unit(8))(es)
 }

 test("should choiceN") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = Pars.lazyUnit { 42 }
  val b: Par<Int> = Pars.lazyUnit { 8 }
  val c: Par<Int> = Pars.lazyUnit { 3 }

  (Pars.choiceN(Pars.lazyUnit { 0 }, listOf(a, b, c)) shouldBePar Pars.unit(42))(es)
  (Pars.choiceN(Pars.lazyUnit { 1 }, listOf(a, b, c)) shouldBePar Pars.unit(8))(es)
  (Pars.choiceN(Pars.lazyUnit { 2 }, listOf(a, b, c)) shouldBePar Pars.unit(3))(es)
 }

 test("should choiceMap") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = Pars.lazyUnit { 42 }
  val b: Par<Int> = Pars.lazyUnit { 8 }
  val c: Par<Int> = Pars.lazyUnit { 3 }

  val choices = mapOf(EnumKey.ONE to a, EnumKey.TWO to b, EnumKey.THREE to c)
  (Pars.choiceMap(Pars.lazyUnit { EnumKey.ONE }, choices) shouldBePar Pars.unit(42))(es)
  (Pars.choiceMap(Pars.lazyUnit { EnumKey.TWO }, choices) shouldBePar Pars.unit(8))(es)
  (Pars.choiceMap(Pars.lazyUnit { EnumKey.THREE }, choices) shouldBePar Pars.unit(3))(es)
 }

 test("should chooser") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = Pars.lazyUnit { 42 }
  val b: Par<Int> = Pars.lazyUnit { 8 }
  val c: Par<Int> = Pars.lazyUnit { 3 }

  val dict = mapOf(EnumKey.ONE to a, EnumKey.TWO to b, EnumKey.THREE to c)
  val choices: (EnumKey) -> Par<Int> = { k -> dict.getValue(k) }

  (Pars.chooser(Pars.lazyUnit { EnumKey.ONE }, choices) shouldBePar Pars.unit(42))(es)
  (Pars.chooser(Pars.lazyUnit { EnumKey.TWO }, choices) shouldBePar Pars.unit(8))(es)
  (Pars.chooser(Pars.lazyUnit { EnumKey.THREE }, choices) shouldBePar Pars.unit(3))(es)
 }

 test("should flatMap") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Int> = Pars.lazyUnit { 42 }
  val b: Par<Int> = Pars.lazyUnit { 8 }
  val c: Par<Int> = Pars.lazyUnit { 3 }

  val dict = mapOf(EnumKey.ONE to a, EnumKey.TWO to b, EnumKey.THREE to c)
  val choices: (EnumKey) -> Par<Int> = { k -> dict.getValue(k) }

  (Pars.flatMap(Pars.lazyUnit { EnumKey.ONE }, choices) shouldBePar Pars.unit(42))(es)
  (Pars.flatMap(Pars.lazyUnit { EnumKey.TWO }, choices) shouldBePar Pars.unit(8))(es)
  (Pars.flatMap(Pars.lazyUnit { EnumKey.THREE }, choices) shouldBePar Pars.unit(3))(es)
 }

 test("should join") {
  val es = Executors.newFixedThreadPool(1)
  val a: Par<Par<Int>> = Pars.lazyUnit { Pars.lazyUnit { 42 } }
  val b: Par<Par<Int>> = Pars.lazyUnit { Pars.lazyUnit { 8 } }
  val c: Par<Par<Int>> = Pars.lazyUnit { Pars.lazyUnit { 3 } }

  (Pars.join(a) shouldBePar Pars.unit(42))(es)
  (Pars.join(b) shouldBePar Pars.unit(8))(es)
  (Pars.join(c) shouldBePar Pars.unit(3))(es)
 }
})

enum class EnumKey { ONE, TWO, THREE }
