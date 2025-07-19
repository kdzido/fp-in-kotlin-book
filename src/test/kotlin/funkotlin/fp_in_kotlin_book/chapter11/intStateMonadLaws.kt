package funkotlin.fp_in_kotlin_book.chapter11

import funkotlin.fp_in_kotlin_book.chapter06.State
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class intStateMonadLaws : StringSpec({
    "learn State" {
        State.setState(1).run(2) shouldBe Pair(Unit, 1)
        State.getState<Int>().run(2) shouldBe Pair(2, 2)
    }

    "getState, setState " {
        State.getState<Int>().flatMap { a -> State.setState(a) }.run(123) shouldBe
                State.unit<Int, Unit>(Unit).run(123)
    }

    "setState, getState " {
        State.setState(1).flatMap { _ -> State.getState() }.run(123) shouldBe
                State.unit<Int, Int>(1).run(1)
    }
})
