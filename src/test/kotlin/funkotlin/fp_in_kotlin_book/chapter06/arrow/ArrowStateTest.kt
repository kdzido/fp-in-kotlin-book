package funkotlin.fp_in_kotlin_book.chapter06.arrow


import arrow.core.Tuple2
import arrow.mtl.run
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ArrowStateTest : FunSpec({
    test("should get state") {
        get<Int>().run(0) shouldBe Tuple2(0, 0)
    }

    test("should set state") {
        set(1).run(0) shouldBe Tuple2(1, Unit)
    }
})
