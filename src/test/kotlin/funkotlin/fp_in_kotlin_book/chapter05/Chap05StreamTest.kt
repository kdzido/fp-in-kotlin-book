package funkotlin.fp_in_kotlin_book.chapter05

import funkotlin.fp_in_kotlin_book.chapter03.List
import funkotlin.fp_in_kotlin_book.chapter03.Nil
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.drop
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.exists
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.exists2
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.forAll
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.foldRight
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.headOption2
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.map
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.filter
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.flatMap
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.map2
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.startsWith
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.tails
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.take
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.take2
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.takeWhile
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.takeWhile2
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.takeWhile_3
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.zipWith
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.toList
import funkotlin.fp_in_kotlin_book.chapter05.Stream.Companion.zipAll
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

    // EXER 5.6
    test("headOption2 in terms of foldRight") {
        Stream.of(1,2,3).headOption2() shouldBe Some(1)
        Stream.of(2,3).headOption2() shouldBe Some(2)
        Stream.of(3).headOption2() shouldBe Some(3)
        Stream.of<Int>().headOption2() shouldBe None
    }

    // EXER 5.7
    test("map in terms of foldRight") {
        Stream.of(1, 2, 3).map({ a -> a * 2 }).toList() shouldBe List.of(2, 4, 6)
        Stream.of<Int>().map({ a -> a * 2 }).toList() shouldBe List.of()
    }

    // EXER 5.13
    test("map2 in terms of unfold") {
        Stream.of(1, 2, 3).map2({ a -> a * 2 }).toList() shouldBe List.of(2, 4, 6)
        Stream.of<Int>().map2({ a -> a * 2 }).toList() shouldBe List.of()
    }

    // EXER 5.7
    test("flatMap in terms of foldRight") {
        Stream.of(1, 2, 3).flatMap({ a -> Stream.of(a * 2, a + 1) }).toList() shouldBe List.of(2, 2, 4, 3, 6, 4)
        Stream.of<Int>().flatMap({ a -> Stream.of(a * 2, a * 3) }).toList() shouldBe List.of()
    }

    // EXER 5.7
    test("filter in terms of foldRight") {
        Stream.of(1, 2, 3, 4).filter({ it % 2 == 0 }).toList() shouldBe List.of(2, 4)
        Stream.of<Int>().filter({ it % 2 == 0 }).toList() shouldBe List.of()
    }

    // EXER 5.7
    test("append in terms of foldRight") {
        Stream.append<Int>(Stream.of(), Stream.of()).toList() shouldBe List.of()
        Stream.append(Stream.of(), Stream.of(4, 5, 6)).toList() shouldBe List.of(4, 5, 6)
        Stream.append(Stream.of(1, 2, 3), Stream.of()).toList() shouldBe List.of(1, 2, 3)
        Stream.append(Stream.of(1, 2, 3), Stream.of(4, 5, 6)).toList() shouldBe List.of(1, 2, 3, 4, 5, 6)
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

    // EXER 5.13
    test("take2 in terms from unfold") {
        val s3 = Stream.of(1, 2, 3)
        s3.take2(0).toList() shouldBe List.of()
        s3.take2(1).toList() shouldBe List.of(1)
        s3.take2(2).toList() shouldBe List.of(1, 2)
        s3.take2(3).toList() shouldBe List.of(1, 2, 3)
        s3.take2(4).toList() shouldBe List.of(1, 2, 3)
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
        // and
        s3.takeWhile_3({ it <= 0 }).toList() shouldBe List.of()
        s3.takeWhile_3({ it <= 1 }).toList() shouldBe List.of(1)
        s3.takeWhile_3({ it <= 2 }).toList() shouldBe List.of(1, 2)
        s3.takeWhile_3({ it <= 3 }).toList() shouldBe List.of(1, 2, 3)
    }

    // EXER 5.13
    test("should zipWith two streams") {
        Empty.zipWith<Int, Int, Int>(Empty, { x, y -> x + y}).toList() shouldBe Nil
        Stream.of(1).zipWith<Int, Int, Int>(Empty, { x, y -> x + y}).toList() shouldBe Nil
        Empty.zipWith<Int, Int, Int>(Stream.of(2), { x, y -> x + y}).toList() shouldBe Nil
        Stream.of(1).zipWith(Stream.of(2), { x, y -> x + y}).toList() shouldBe List.of(3)
        Stream.of(1, 3).zipWith(Stream.of(2), { x, y -> x + y}).toList() shouldBe List.of(3)
        Stream.of(1).zipWith(Stream.of(2, 3), { x, y -> x + y}).toList() shouldBe List.of(3)
        Stream.of(1, 2, 3).zipWith(Stream.of(4, 5, 6), { x, y -> x + y}).toList() shouldBe List.of(5, 7, 9)
    }

    // EXER 5.13
    test("should zipAll two streams") {
        Empty.zipAll<Int, Int>(Empty).toList() shouldBe Nil
        Stream.of(1).zipAll<Int, Int>(Empty).toList() shouldBe List.of(Pair(Some(1), None))
        Empty.zipAll<Int, Int>(Stream.of(2)).toList() shouldBe List.of(Pair(None, Some(2)))
        Stream.of(1).zipAll(Stream.of(2)).toList() shouldBe List.of(Pair(Some(1), Some(2)))
        Stream.of(1, 3).zipAll(Stream.of(2)).toList() shouldBe List.of(Pair(Some(1), Some(2)), Pair(Some(3), None))
        Stream.of(1).zipAll(Stream.of(2, 3)).toList() shouldBe List.of(Pair(Some(1), Some(2)), Pair(None, Some(3)))
        Stream.of(1, 3).zipAll(Stream.of(2, 4)).toList() shouldBe List.of(Pair(Some(1), Some(2)), Pair(Some(3), Some(4)))
    }

    // EXER 5.14
    test("should startsWith") {
        Empty.startsWith(Empty) shouldBe true
        Empty.startsWith(Stream.of(1)) shouldBe false
        Stream.of(1).startsWith(Empty) shouldBe true
        Stream.of(1).startsWith(Stream.of(1)) shouldBe true
        Stream.of(1, 2, 3).startsWith(Stream.of(1, 2)) shouldBe true
        Stream.of(1, 2, 3).startsWith(Stream.of(1, 2, 3)) shouldBe true
        Stream.of(1, 2, 3).startsWith(Stream.of(1, 2, 3, 4)) shouldBe false
    }

    // EXER 5.15
    test("tails should return stream of tails") {
        Stream.of<Int>().tails().map { it.toList() }.toList() shouldBe List.of(List.of())
        Stream.of(1).tails().map { it.toList() }.toList() shouldBe List.of(List.of(1), List.of())
        Stream.of(1, 2, 3).tails().map { it.toList() }.toList() shouldBe List.of(List.of(1, 2, 3), List.of(2, 3), List.of(3), List.of())
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

    test("infinite ones") {
        Stream.ones().take(3).toList() shouldBe List.of(1, 1, 1)
        Stream.ones().map { it + 1}.exists { it % 2 == 0 } shouldBe true
        // to bottom
//        Stream.ones().takeWhile { it == 1}.toList() shouldBe List.of(1)
//        Stream.ones().forAll { it == 1} // does not terminate
    }

    // EXER 5.8
    test("infinite constant") {
        Stream.constant(2).take(3).toList() shouldBe List.of(2, 2, 2)
        Stream.constant(2).map { it + 1 }.exists { it % 2 == 1 } shouldBe true
    }

    // EXER 5.9
    test("infinite from") {
        Stream.from(2).take(3).toList() shouldBe List.of(2, 3, 4)
    }

    // EXER 5.10
    test("infinite fibs") {
        Stream.fibs().take(6).toList() shouldBe List.of(0, 1, 1, 2, 3, 5)
    }

    // EXER 5.11, 5.12
    test("infinite unfold") {
        // Nil
        Stream.unfold<Int, Int>(0, { s -> None }).take(6).toList() shouldBe List.of()
        // constant(1)
        Stream.unfold(1, { s -> Some(Pair(s, s)) }).take(6).toList() shouldBe List.of(1, 1, 1, 1, 1, 1)
        // from(0)
        Stream.unfold(0, { s -> Some(Pair(s, s + 1)) }).take(6).toList() shouldBe List.of(0, 1, 2, 3, 4, 5)
        // fibs()
        Stream.unfold(Pair(0, 1), { s -> Some(Pair(s.first, Pair(s.second, s.first + s.second))) }).take(6)
            .toList() shouldBe List.of(0, 1, 1, 2, 3, 5)
    }

})

