package funkotlin.fp_in_kotlin_book.chapter05

import funkotlin.fp_in_kotlin_book.chapter04.Some
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class Chap05StreamTest : FunSpec({
    test("create empty stream") {
        val s = Empty
        s.shouldBeInstanceOf<Empty>()
    }

    test("create stream") {
        val s = Cons({ 1 }, { Cons({ 2 }, { Empty }) })
        s.head() shouldBe 1
        s.tail().shouldBeInstanceOf<Cons<Int>>()
        (s.tail() as Cons<Int>).head() shouldBe 2
        (s.tail() as Cons<Int>).tail().shouldBeInstanceOf<Empty>()
    }

    test("create stream with lazy smart constructor") {
        // given
        var headCounter: Int? = 0
        var tailCounter: Int? = 5
        // and
        val h: () -> Int = { println("Hi head"); headCounter = headCounter?.plus(1); headCounter!! }
        val t: () -> Cons<Int> = { println("Hi tail"); tailCounter = tailCounter?.plus(1); Cons({ 2 }, { Empty }) }

        // when
        val s = Stream.cons(h, t)
        // then: "the head expr called the side effect on the counter only once"
        s.headOption() shouldBe Some(1)
        headCounter shouldBe 1
        s.headOption() shouldBe Some(1)
        headCounter shouldBe 1
    }
})

