package funkotlin.fp_in_kotlin_book.chapter05

import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.drop
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.take
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.takeWhile
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.toList
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

    // EXER 5.1
    test("create stream of") {
        val s3 = Stream.of(1,2,3)
        s3.toList() shouldBe List.of(1,2,3)
    }

    // EXER 5.2
    test("take n from stream") {
        val s3 = Stream.of(1, 2, 3)
        s3.take(0).toList() shouldBe List.of()
        s3.take(1).toList() shouldBe List.of(1)
        s3.take(2).toList() shouldBe List.of(1, 2)
        s3.take(3).toList() shouldBe List.of(1, 2, 3)
        s3.take(4).toList() shouldBe List.of(1, 2, 3)
    }

    // EXER 5.3
    test("takeWhile from stream") {
        val s3 = Stream.of(1, 2, 3)
        s3.takeWhile({ it <= 0 }).toList() shouldBe List.of()
        s3.takeWhile({ it <= 1 }).toList() shouldBe List.of(1)
        s3.takeWhile({ it <= 2 }).toList() shouldBe List.of(1, 2)
        s3.takeWhile({ it <= 3 }).toList() shouldBe List.of(1, 2, 3)
    }

    // EXER 5.2
    test("drop n from stream") {
        val s3 = Stream.of(1, 2, 3)
        s3.drop(0).toList() shouldBe List.of(1, 2, 3)
        s3.drop(1).toList() shouldBe List.of(2, 3)
        s3.drop(2).toList() shouldBe List.of(3)
        s3.drop(3).toList() shouldBe List.of()
        s3.drop(4).toList() shouldBe List.of()
    }
})

