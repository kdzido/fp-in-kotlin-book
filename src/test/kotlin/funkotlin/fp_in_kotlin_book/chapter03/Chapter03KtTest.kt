package funkotlin.fp_in_kotlin_book.chapter03

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Chapter03KtTest : FunSpec({
    test("should get sum of list") {
        List.sum(Nil) shouldBe 0
        List.sum(List.of(6)) shouldBe 6
        List.sum(List.of(1, 2, 3)) shouldBe 6
        // and
        List.sum2(Nil) shouldBe 0
        List.sum2(List.of(6)) shouldBe 6
        List.sum2(List.of(1, 2, 3)) shouldBe 6
    }

    test("should get product of list") {
        List.product(Nil) shouldBe 1.0
        List.product(List.of(6.0)) shouldBe 6.0
        List.product(List.of(2.0, 3.0)) shouldBe 6.0
        // and
        List.product2(Nil) shouldBe 1.0
        List.product2(List.of(6.0)) shouldBe 6.0
        List.product2(List.of(2.0, 3.0)) shouldBe 6.0
    }

    // Exercise 3.1
    test("should return tail of list") {
        List.tail(Nil) shouldBe Nil
        List.tail(List.of(1)) shouldBe Nil
        List.tail(List.of(1, 2, 3)) shouldBe List.of(2, 3)
    }

    // Exercise 3.2
    test("should replace head of list") {
        List.setHead(Nil, 1) shouldBe List.of(1)
        List.setHead(List.of(1), 0) shouldBe List.of(0)
        List.setHead(List.of(1, 2, 3), 0) shouldBe List.of(0, 2, 3)
    }

    // Exercise 3.3
    test("should drop n elements of list") {
        List.drop(Nil, 0) shouldBe Nil
        List.drop(Nil, 1) shouldBe Nil
        List.drop(List.of(1), 0) shouldBe List.of(1)
        List.drop(List.of(1), 1) shouldBe Nil
        List.drop(List.of(1, 2, 3), 1) shouldBe List.of(2, 3)
        List.drop(List.of(1, 2, 3), 2) shouldBe List.of(3)
        List.drop(List.of(1, 2, 3), 3) shouldBe Nil
    }

    // Exercise 3.4
    test("should dropWhile elements of list") {
        List.dropWhile(List.of(1, 2, 3), { it <= 0 }) shouldBe List.of(1, 2, 3)
        List.dropWhile(List.of(1, 2, 3), { it <= 1 }) shouldBe List.of(2, 3)
        List.dropWhile(List.of(1, 2, 3), { it <= 2 }) shouldBe List.of(3)
        List.dropWhile(List.of(1, 2, 3), { it <= 3 }) shouldBe Nil
    }

    // Listing 3.9
    test("should append two lists") {
        List.append(Nil, Nil) shouldBe Nil
        List.append(Nil, List.of(1, 2, 3)) shouldBe List.of(1, 2, 3)
        List.append(List.of(1, 2, 3), Nil) shouldBe List.of(1, 2, 3)
        List.append(List.of(1, 2, 3), List.of(4, 5, 6)) shouldBe List.of(1, 2, 3, 4, 5,6)
    }

    // Exercise 3.5
    test("should return init of list") {
        List.init(Nil) shouldBe Nil
        List.init(List.of(1)) shouldBe Nil
        List.init(List.of(1, 2)) shouldBe List.of(1)
        List.init(List.of(1, 2, 3)) shouldBe List.of(1, 2)
        List.init(List.of(1, 2, 3, 4)) shouldBe List.of(1, 2, 3)
    }

    // Exercise 3.7
    test("should foldRight with type constructors of List") {
        List.foldRight(Cons(1, Cons(2, Cons(3, Nil))),
            Nil as List<Int>,
            { x, y -> Cons(x, y) }) shouldBe List.of(1, 2, 3)
    }

    // Exercise 3.8
    test("should return length of list") {
        List.length(Nil) shouldBe 0
        List.length(List.of(1)) shouldBe 1
        List.length(List.of(1, 2)) shouldBe 2
        List.length(List.of(1, 2, 3)) shouldBe 3
    }

    // Exercise 3.9
    test("should foldLeft") {
        List.foldLeft(Nil as List<Int>, 0, { x, y -> x + y }) shouldBe 0
        List.foldLeft(List.of(1), 0, { x, y -> x + y }) shouldBe 1
        List.foldLeft(List.of(1, 2), 0, { x, y -> x + y }) shouldBe 3
        List.foldLeft(List.of(1, 2, 3), 0, { x, y -> x + y }) shouldBe 6
    }

    // Exercise 3.11
    test("should reverse list") {
        List.foldLeft(List.of(1, 2, 3),
            Nil as List<Int>,
            { x, y -> Cons(y, x) }) shouldBe List.of(3, 2, 1)
    }

})
