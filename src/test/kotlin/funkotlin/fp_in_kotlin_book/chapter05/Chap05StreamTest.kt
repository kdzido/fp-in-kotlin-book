package funkotlin.fp_in_kotlin_book.chapter05

import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.drop
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.exists
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.exists2
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.forAll
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.foldRight
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.take
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.takeWhile
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.takeWhile2
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
        // and
        s3.takeWhile2({ it <= 0 }).toList() shouldBe List.of()
        s3.takeWhile2({ it <= 1 }).toList() shouldBe List.of(1)
        s3.takeWhile2({ it <= 2 }).toList() shouldBe List.of(1, 2)
        s3.takeWhile2({ it <= 3 }).toList() shouldBe List.of(1, 2, 3)
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

    test("exists in stream") {
        val s3 = Stream.of(1, 2, 3)
        s3.exists({ it == 0 }) shouldBe false
        s3.exists({ it == 1 }) shouldBe true
        s3.exists({ it == 2 }) shouldBe true
        s3.exists({ it == 3 }) shouldBe true
        s3.exists({ it == 4 }) shouldBe false
        // and
        s3.exists2({ it == 0 }) shouldBe false
        s3.exists2({ it == 1 }) shouldBe true
        s3.exists2({ it == 2 }) shouldBe true
        s3.exists2({ it == 3 }) shouldBe true
        s3.exists2({ it == 4 }) shouldBe false
    }

    test("foldRight of stream to sum elements") {
        Stream.of<Int>().foldRight({ 0 }, { a, bThunk -> a + bThunk() }) shouldBe 0
        Stream.of(1).foldRight({ 0 }, { a, bThunk -> a + bThunk() }) shouldBe 1
        Stream.of(1, 2, 3).foldRight({ 0 }, { a, bThunk -> a + bThunk() }) shouldBe 6
    }

    // EXER 5.4
    test("forAll in stream") {
        val s3 = Stream.of(1, 2, 3)
        s3.forAll({ it <= 0 }) shouldBe false
        s3.forAll({ it <= 1 }) shouldBe false
        s3.forAll({ it <= 2 }) shouldBe false
        s3.forAll({ it <= 3 }) shouldBe true
        s3.forAll({ it <= 4 }) shouldBe true
    }
})

