package funkotlin.fp_in_kotlin_book.chapter02

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Chapter02KtTest : FunSpec({

    test("findFirst should return index of first matching element") {
        val ls = arrayOf("one", "two", "three")
        findFirst(ls, "one") shouldBe 0
        findFirst(ls, "three") shouldBe 2
        findFirst(ls, "NONE") shouldBe -1

        findFirst(ls, {it == "two"}) shouldBe 1
        findFirst(ls, {it == "NONE"}) shouldBe -1
    }

    // Exercise 2-2, implement isSorted
    test("isSorted should check if collection is sorted as per predicate") {
        val gteq: (Int, Int) -> Boolean = { a, b -> a >= b }
        val gt: (Int, Int) -> Boolean = { a, b -> a > b }

        isSorted(listOf(1), gteq) shouldBe true
        isSorted(listOf(3, 2, 1), gteq) shouldBe true
        isSorted(listOf(3, 3, 3), gteq) shouldBe true

        isSorted(listOf(1, 2), gteq) shouldBe false
        isSorted(listOf(1, 2, 3), gteq) shouldBe false

        isSorted(listOf(1), gt) shouldBe true
        isSorted(listOf(3, 3, 3), gt) shouldBe false
        isSorted(listOf(3, 3, 3), gt) shouldBe false
    }

    test("learning extension methods and properties") {
        println(1.show())
        println(1.showProp)
    }

    test("partial1 should return parially applied HOC") {
        val pf = partial1<String, Int, String>("one", { a, b -> a + b })
        pf(2) shouldBe "one2"
    }

    // Exercise 2-3, implement curry
    test("curry should return composed single arg functions") {
        val cf = curry<String, Int, String>({a, b -> a + b})
        cf("one")(2) shouldBe "one2"
    }

    // Exercise 2-4, implement uncurry
    test("uncurry should reverse curried function") {
        // given
        val curriedF = curry<String, Int, String>({ a, b -> a + b })
        curriedF("one")(2) shouldBe "one2"

        // expect
        uncurry(curriedF)("one", 2) shouldBe "one2"
    }

    // Exercise 2-5, implement compose
    test("compose should chain two functions") {
        val len: (String) -> Int = { str -> str.length }
        val plusVat: (Int) -> Double = {n -> n.toDouble() * 1.23}

        compose(plusVat, len)("Hello world") shouldBe 13.53
    }
})

fun Int.show(): String = "The fun value is $this"
val Int.showProp: String
    get() = "The prop value is $this"
