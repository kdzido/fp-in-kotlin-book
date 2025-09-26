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

    "!should repeat" {
        val p = Halt<Int, Int>().repeat()   // stack overflow, Halt replaces recursive step of the driver
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
        sum()(Stream.of()).toList() shouldBe ListCh.of()
        sum()(Stream.of(1.0, 2.0, 3.0)).toList() shouldBe ListCh.of(1.0, 3.0, 6.0)
        // and:
        sum2()(Stream.of()).toList() shouldBe ListCh.of()
        sum2()(Stream.of(1.0, 2.0, 3.0)).toList() shouldBe ListCh.of(1.0, 3.0, 6.0)
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

class Exercise15_2 : StringSpec({
    val stream = Stream.of(1, 2, 3, 4, 5)

    "should count" {
        count<Int>()(Stream.empty()).toList() shouldBe ListCh.of()
        count<Int>()(stream).toList() shouldBe ListCh.of(1, 2, 3, 4, 5)
        // and:
        count2<Int>()(Stream.empty()).toList() shouldBe ListCh.of()
        count2<Int>()(stream).toList() shouldBe ListCh.of(1, 2, 3, 4, 5)
    }
})

class Exercise15_3 : StringSpec({
    "should mean" {
        mean()(Stream.empty()).toList() shouldBe ListCh.of()
        mean()(Stream.of(1.0)).toList() shouldBe ListCh.of(1.0)
        mean()(Stream.of(1.0, 2.0)).toList() shouldBe ListCh.of(1.0, 1.5)
    }
})

class Exercise15_6 : StringSpec({
    "should mean2" {
        mean2()(Stream.empty()).toList() shouldBe ListCh.of()
        mean2()(Stream.of(1.0)).toList() shouldBe ListCh.of(1.0)
        mean2()(Stream.of(1.0, 2.0)).toList() shouldBe ListCh.of(1.0, 1.5)
    }
})

class Exercise15_7 : StringSpec({
    "should zipWithIndex" {
        val h = Halt<Int, String>()
        val p1 = Emit("a", Emit("b", h))

        p1.zipWithIndex()(Stream.of()).toList() shouldBe
                ListCh.of()
        p1.zipWithIndex()(Stream.of(1, 2)).toList() shouldBe
                ListCh.of(0 to "a", 1 to "b")
    }
})

class Exercise15_8 : StringSpec({
    "should exists" {
        val so = Stream.of(1, 3, 5, 7, 9)
        val se = Stream.of(2, 4, 6, 8, 10)
        val soe = Stream.of(1, 3, 5, 6, 7)
        val pe = exists<Int> { it % 2 == 0 }

        pe(Stream.of()).toList() shouldBe ListCh.of()
        pe(so).toList() shouldBe ListCh.of(false, false, false, false, false)
        pe(se).toList() shouldBe ListCh.of(true)
        pe(soe).toList() shouldBe ListCh.of(false, false, false, true)
    }
})

class Exercise15_5 : StringSpec({
    "should pipe processes" {
        val stream = Stream.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0)
        val sumP: Process<Double, Double> = sum()
        val take4 = take<Double>(4)
        val fused = sumP pipe take4

        fused(stream).toList() shouldBe ListCh.of(1.0, 3.0, 6.0, 10.0)
    }

    "should pipe in-place processes" {
        val s1 = Stream.of(1, 2, 3, 4, 5, 6, 7, 8)
        val p2 = filter<Int> { it % 2 == 0 } pipe lift { it + 1 }

        p2(s1).toList() shouldBe ListCh.of(3, 5, 7, 9)
    }

    "should map over Process" {
        val stream = Stream.of(1.0, 2.0, 3.0, 4.0)
        val fused = sum().map { it + 1.0 }

        fused(stream).toList() shouldBe ListCh.of(2.0, 4.0, 7.0, 11.0)
    }

    "should append processes" {
        val h = Halt<Int, Int>()
        val p1 = Emit(1, h)
        val p2 = Emit(3, h)

        val appended = p1 append p2
        appended shouldBe Emit(1, Emit(3, h))
    }

    "should flatMap processes" {
        val p1 = Emit(1, Emit(2, Halt<Int, Int>()))

        fun transform(x: Int): Process<Int, String> =
            Emit("Number: $x", Emit("Number: $x"))

        p1.flatMap<String>(::transform) shouldBe
                Emit(
                    "Number: 1",
                    Emit(
                        "Number: 1",
                        Emit(
                            "Number: 2",
                            Emit(
                                "Number: 2",
                                Halt()
                            )
                        )
                    )
                )
    }

    "Process monad operations" {
        val M = Process.monad<Int, Int>()

        val e1 = M.unit(1).fix()
        val e2 = M.unit(2).fix()

        // expect: "unit"
        e1 shouldBe Emit(1)
        e2 shouldBe Emit(2)

        // expect: "map"
        val f1: (Int) -> String = { i: Int ->
            when (i) {
                0 -> "zero"
                1 -> "one"
                2 -> "two"
                else -> "<null>"
            }
        }
        M.map(Halt(), f1).fix() shouldBe Halt()
        M.map(e1, f1).fix() shouldBe Emit("one")
        M.map(e2, f1).fix() shouldBe Emit("two")

        // expect: "flatMap"
        val f2: (Int) -> ProcessOf<Int, String> = { i: Int ->
            when (i) {
                0 -> Emit("zero")
                1 -> Emit("one")
                2 -> Emit("two")
                else -> Halt()
            }
        }
        M.flatMap(Halt(), f2).fix() shouldBe Halt()
        M.flatMap(e1, f2).fix() shouldBe Emit("one")
        M.flatMap(e2, f2).fix() shouldBe Emit("two")
    }

})

