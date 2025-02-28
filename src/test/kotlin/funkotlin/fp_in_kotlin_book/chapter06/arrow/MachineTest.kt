package funkotlin.fp_in_kotlin_book.chapter06.arrow

import arrow.core.Tuple2
import arrow.mtl.StateApi
import arrow.mtl.run
import arrow.typeclasses.internal.IdBimonad
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MachineTest : FunSpec({

 test("Locked machine with candies Should get unlocked when coin is inserted") {
  Machine(true, 1, 0).transit(Coin) shouldBe Machine(false, 1, 1)
  Machine(true, 1,  1).transit(Coin) shouldBe Machine(false, 1, 2)
 }

 test("Unlocked machine with candies Should dispose candy when knob is turned") {
  Machine(false, 1, 1).transit(Turn) shouldBe Machine(true, 0, 1)
  Machine(false, 2, 1).transit(Turn) shouldBe Machine(true, 1, 1)
 }

 test("Locked machine Should do nothing when turning knob") {
  Machine(true, 1, 0).transit(Turn) shouldBe Machine(true, 1, 0)
  Machine(true, 1,  1).transit(Turn) shouldBe Machine(true, 1, 1)
  Machine(true, 0, 0).transit(Turn) shouldBe Machine(true, 0, 0)
  Machine(true, 0,  1).transit(Turn) shouldBe Machine(true, 0, 1)
 }

 test("Unlocked machine Should do nothing when inserting coin") {
  Machine(false, 1, 0).transit(Coin) shouldBe Machine(false, 1, 0)
  Machine(false, 1,  1).transit(Coin) shouldBe Machine(false, 1, 1)
  Machine(false, 0, 0).transit(Coin) shouldBe Machine(false, 0, 0)
  Machine(false, 0,  1).transit(Coin) shouldBe Machine(false, 0, 1)
 }

 test("Machine out of candies Should ignore all inputs") {
  Machine(false, 0, 0).transit(Coin) shouldBe Machine(false, 0, 0)
  Machine(false, 0,  0).transit(Turn) shouldBe Machine(false, 0, 0)
  Machine(false, 0, 1).transit(Coin) shouldBe Machine(false, 0, 1)
  Machine(false, 0,  1).transit(Turn) shouldBe Machine(false, 0, 1)
  Machine(true, 0, 0).transit(Coin) shouldBe Machine(true, 0, 0)
  Machine(true, 0,  0).transit(Turn) shouldBe Machine(true, 0, 0)
  Machine(true, 0, 1).transit(Coin) shouldBe Machine(true, 0, 1)
  Machine(true, 0,  1).transit(Turn) shouldBe Machine(true, 0, 1)
 }

 test("Machine Should transit on Input stimuli") {
  val lockedMachine = Machine.simple(Coin).flatMap(IdBimonad) { m: Tuple2<Int, Int> -> StateApi.just(m) }
  val (machineState2, candiesCoinsInside2) = lockedMachine.run(Machine(true, 1, 0))

  machineState2 shouldBe Machine(false, 1, 1)
  candiesCoinsInside2 shouldBe Tuple2(1, 1)

  // and
  val unlockedMachine = Machine.simple(Turn).flatMap(IdBimonad) { m: Tuple2<Int, Int> -> StateApi.just(m) }
  val (newMachineState3, candiesCoinsInside3) = unlockedMachine.run(Machine(false, 1, 1))
  newMachineState3 shouldBe Machine(true, 0, 1)
  candiesCoinsInside3 shouldBe Tuple2(1, 0)
 }

 test("Candy machine Should accept inputs and return number of candies and coins inside of it at the end") {
    Machine.simulateMachine(listOf(Turn)).run(Machine(false, 1, 1)).b shouldBe Tuple2(1, 0)
    Machine.simulateMachine(listOf(Coin, Turn, Coin, Turn)).run(Machine(true, 1, 0)).b shouldBe Tuple2(1, 0) // no more candies

    Machine.simulateMachine(listOf(Coin, Turn)).run(Machine(true, 1, 0)).b shouldBe Tuple2(1, 0)
    Machine.simulateMachine(listOf(Coin, Turn, Coin, Turn)).run(Machine(true, 1, 0)).b shouldBe Tuple2(1, 0) // no more candies

    Machine.simulateMachine(listOf(Coin, Turn, Coin, Turn)).run(Machine(false, 2, 0)).b shouldBe Tuple2(1, 0)
    Machine.simulateMachine(listOf(Coin, Turn, Coin, Turn)).run(Machine(true, 2, 0)).b shouldBe Tuple2(2, 0)

    Machine.simulateMachine(listOf(Coin, Coin, Coin, Coin)).run(Machine(false, 5, 10)).b shouldBe Tuple2(10, 5)
    Machine.simulateMachine(listOf(Coin, Coin, Coin, Coin)).run(Machine(true, 5, 10)).b shouldBe Tuple2(11, 5)
    Machine.simulateMachine(listOf(Coin, Turn, Coin, Turn, Coin, Turn, Coin)).run(Machine(true, 5, 10)).b shouldBe Tuple2(14, 2)
    Machine.simulateMachine(listOf(Coin, Turn, Coin, Turn, Coin, Turn, Coin, Turn)).run(Machine(true, 5, 10)).b shouldBe Tuple2(14, 1)
 }
})
