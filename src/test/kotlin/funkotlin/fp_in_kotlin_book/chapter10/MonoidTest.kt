package funkotlin.fp_in_kotlin_book.chapter10

import arrow.core.extensions.list.foldable.foldLeft
import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter06.SimpleRNG
import funkotlin.fp_in_kotlin_book.chapter08.Gen
import funkotlin.fp_in_kotlin_book.chapter08.Passed
import funkotlin.fp_in_kotlin_book.chapter08.Prop
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors

class MonoidTest : StringSpec({
    "should combine strings" {
        stringMonoid.combine("one", "two") shouldBe "onetwo"
        stringMonoid.combine("one", stringMonoid.nil) shouldBe "one"
    }

    "should combine lists of strings" {
        listMonoid<String>().combine(listOf("one"), listOf("two")) shouldBe listOf("one", "two")
        listMonoid<String>().combine(listOf("one"), listMonoid<String>().nil) shouldBe listOf("one")
    }

    "ints add monoid" {
        val addMonoid = intAddition()
        val nil = addMonoid.nil
        addMonoid.combine(2, 3) shouldBe 5
        addMonoid.combine(2, nil) shouldBe 2
    }

    "ints multi monoid" {
        val mulMonoid = intMultiplication()
        val nil = mulMonoid.nil
        mulMonoid.combine(2, 3) shouldBe 6
        mulMonoid.combine(2, nil) shouldBe 2
    }

    "boolean or monoid" {
        val orMonoid = booleanOr()
        val nil = orMonoid.nil
        orMonoid.combine(true, true) shouldBe true
        orMonoid.combine(true, false) shouldBe true
        orMonoid.combine(false, true) shouldBe true
        orMonoid.combine(false, false) shouldBe false
        // and
        orMonoid.combine(false, nil) shouldBe false
        orMonoid.combine(true, nil) shouldBe true
    }

    "boolean and monoid" {
        val andMonoid = booleanAnd()
        val nil = andMonoid.nil
        andMonoid.combine(true, true) shouldBe true
        andMonoid.combine(true, false) shouldBe false
        andMonoid.combine(false, true) shouldBe false
        andMonoid.combine(false, false) shouldBe false
        // and
        andMonoid.combine(false, nil) shouldBe false
        andMonoid.combine(true, nil) shouldBe true
    }

    "first Option monoid" {
        val intOption = firstOptionMonoid<Int>()
        val nil = intOption.nil

        intOption.combine(Some(1), Some(2)) shouldBe Some(1)
        intOption.combine(Some(1), None) shouldBe Some(1)
        intOption.combine(None, Some(2)) shouldBe Some(2)
        intOption.combine(None, None) shouldBe None
        // and: id law
        intOption.combine(Some(1), nil) shouldBe Some(1)
        intOption.combine(nil, Some(1)) shouldBe Some(1)
        intOption.combine(nil, nil) shouldBe nil
    }

    "last Option monoid" {
        val intOption = lastOptionMonoid<Int>()
        val nil = intOption.nil

        intOption.combine(Some(1), Some(2)) shouldBe Some(2)
        intOption.combine(Some(1), None) shouldBe Some(1)
        intOption.combine(None, Some(2)) shouldBe Some(2)
        intOption.combine(None, None) shouldBe None
        // and: id law
        intOption.combine(Some(1), nil) shouldBe Some(1)
        intOption.combine(nil, Some(1)) shouldBe Some(1)
        intOption.combine(nil, nil) shouldBe nil
    }

    "endo-andThen monoid" {
        val f1f2 = endoMonoidAndThen<Int>()
        val nil = f1f2.nil

        val inc1: (Int) -> Int = { it + 1 }
        val mul2: (Int) -> Int = { it * 2 }

        f1f2.combine(inc1, mul2)(2) shouldBe 6
        f1f2.combine(mul2, inc1)(2) shouldBe 5
        // and: id law
        f1f2.combine(inc1, nil)(2) shouldBe 3
        f1f2.combine(nil, mul2)(2) shouldBe 4
        f1f2.combine(nil, nil)(2) shouldBe 2
    }

    "endo-compose monoid" {
        val f1f2 = endoMonoidComposed<Int>()
        val nil = f1f2.nil

        val inc1: (Int) -> Int = { it + 1 }
        val mul2: (Int) -> Int = { it * 2 }

        f1f2.combine(inc1, mul2)(2) shouldBe 5
        f1f2.combine(mul2, inc1)(2) shouldBe 6
        // and: id law
        f1f2.combine(inc1, nil)(2) shouldBe 3
        f1f2.combine(nil, mul2)(2) shouldBe 4
        f1f2.combine(nil, nil)(2) shouldBe 2
    }

    "monoid laws for int monoids" {
        val rng = SimpleRNG(1L)
        val intGen = Gen.choose(-1000, 1000)

        Prop.run(monoidLaws(intAddition(), intGen), 100, 100, rng) shouldBe Passed
        Prop.run(monoidLaws(intMultiplication(), intGen), 100, 100, rng) shouldBe Passed
    }

    "should fold list with stringMonoid" {
        val words = listOf("Hic", "Est", "Index")

        words.foldRight(stringMonoid.nil, stringMonoid::combine) shouldBe "HicEstIndex"
        words.foldLeft(stringMonoid.nil, stringMonoid::combine) shouldBe "HicEstIndex"
    }

    "should concat list with given monoid" {
        val words = listOf("Hic", "Est", "Index")

        concatenate(words, stringMonoid) shouldBe "HicEstIndex"
    }

    "should foldMap over list" {
        val nums = listOf(1, 2, 3)
        val num2word: (Int) -> String = {
            when (it) {
                1 -> "One"
                2 -> "Two"
                3 -> "Three"
                else -> "<unsuported number>"
            }
        }

        foldMap(nums, stringMonoid, num2word) shouldBe "OneTwoThree"
    }

    "should balFoldMap over list" {
        val nums0 = listOf<Int>()
        val nums1 = listOf(1)
        val nums2 = listOf(1, 2)
        val nums4 = listOf(1, 2, 3, 4)
        val nums5 = listOf(1, 2, 3, 4, 5)

        balFoldMap(nums0, stringMonoid, num2word) shouldBe ""
        balFoldMap(nums1, stringMonoid, num2word) shouldBe "One"
        balFoldMap(nums2, stringMonoid, num2word) shouldBe "OneTwo"
        balFoldMap(nums4, stringMonoid, num2word) shouldBe "OneTwoThreeFour"
        balFoldMap(nums5, stringMonoid, num2word) shouldBe "OneTwoThreeFourFive"
    }

    "should parFoldMap over list" {
        val pool = Executors.newFixedThreadPool(10)
        val stringMonoidPar = monoidPar(stringMonoid)

        val nums0 = listOf<Int>()
        val nums1 = listOf(1)
        val nums2 = listOf(1, 2)
        val nums4 = listOf(1, 2, 3, 4)
        val nums10 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val nums13 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3)
        val nums100 = List(10) { nums10 }.flatten()

        parFoldMap(nums0, stringMonoidPar, num2word)(pool).get() shouldBe ""
        parFoldMap(nums1, stringMonoidPar, num2word)(pool).get() shouldBe "One"
        parFoldMap(nums2, stringMonoidPar, num2word)(pool).get() shouldBe "OneTwo"
        parFoldMap(nums4, stringMonoidPar, num2word)(pool).get() shouldBe "OneTwoThreeFour"
        parFoldMap(nums10, stringMonoidPar, num2word)(pool).get() shouldBe "OneTwoThreeFourFiveSixSevenEightNineTen"
        parFoldMap(nums13, stringMonoidPar, num2word)(pool).get() shouldBe "OneTwoThreeFourFiveSixSevenEightNineTenOneTwoThree"
        parFoldMap(nums100, stringMonoidPar, num2word)(pool).get() shouldBe ("OneTwoThreeFourFiveSixSevenEightNineTen".repeat(10))
    }

    "should foldLeft list" {
        val words = listOf("aa", "bbb", "cccc")

        words.foldRight(1, { s: String, acc: Int -> acc * s.length }) shouldBe 24
        words.foldLeft(1, { acc: Int, s: String -> acc * s.length }) shouldBe 24

        // expect
        foldRight(words.asSequence(), 1, { s: String, acc: Int -> acc * s.length }) shouldBe 24
        foldLeft(words.asSequence(), 1, { acc: Int, s: String -> acc * s.length }) shouldBe 24
    }

    "should detect ascending order of List<Int>" {
        val nums0 = listOf<Int>()
        val nums1 = listOf(1)
        val nums2asc = listOf(1, 2)
        val nums2not = listOf(1, 0)
        val nums4asc = listOf(1, 2, 3, 4)
        val nums4not = listOf(1, 2, 3, 4, 3)
        val nums10asc = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val nums10not10 = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, -1)
        val nums10not9 = listOf(1, 2, 3, 4, 5, 6, 7, 8, -1, 10)
        val nums10not8 = listOf(1, 2, 3, 4, 5, 6, 7, -1, 9, 10)
        val nums10not7 = listOf(1, 2, 3, 4, 5, 6, -1, 8, 9, 10)
        val nums10not6 = listOf(1, 2, 3, 4, 5, -1, 7, 8, 9, 10)
        val nums10not5 = listOf(1, 2, 3, 4, -1, 6, 7, 8, 9, 10)
        val nums10not4 = listOf(1, 2, 3, -1, 5, 6, 7, 8, 9, 10)
        val nums10not3 = listOf(1, 2, -1, 4, 5, 6, 7, 8, 9, 10)
        val nums10not2 = listOf(1, -1, 3, 4, 5, 6, 7, 8, 9, 10)
        val nums10not1 = listOf(50, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        ordered(nums0.asSequence()) shouldBe true
        ordered(nums1.asSequence()) shouldBe true
        ordered(nums2asc.asSequence()) shouldBe true
        ordered(nums4asc.asSequence()) shouldBe true
        ordered(nums10asc.asSequence()) shouldBe true
        // and
        ordered(nums2not.asSequence()) shouldBe false
        ordered(nums4not.asSequence()) shouldBe false
        ordered(nums10not10.asSequence()) shouldBe false
        ordered(nums10not9.asSequence()) shouldBe false
        ordered(nums10not8.asSequence()) shouldBe false
        ordered(nums10not7.asSequence()) shouldBe false
        ordered(nums10not6.asSequence()) shouldBe false
        ordered(nums10not5.asSequence()) shouldBe false
        ordered(nums10not4.asSequence()) shouldBe false
        ordered(nums10not3.asSequence()) shouldBe false
        ordered(nums10not2.asSequence()) shouldBe false
        ordered(nums10not1.asSequence()) shouldBe false
    }

    "should productMonoid" {
        val pm = productMonoid(intAddition(), stringMonoid)

        // expect: "id laws hold"
        pm.combine(1 to "one", pm.nil) shouldBe (1 to "one")
        pm.combine(pm.nil, 1 to "one") shouldBe (1 to "one")
        // and:
        pm.combine(1 to "one", 2 to "two") shouldBe (3 to "onetwo")
    }

    "should merge Maps with mapMergeMonoid" {
        val mmm = mapMergeMonoid<String, List<Int>>(listMonoid())

        // expect:
        mmm.combine(
            mapOf(
                "first" to listOf(1, 2, 3),
                "second" to listOf(11, 12, 13)
            ),
            mapOf("first" to listOf(4, 5, 6)),
        ) shouldBe mapOf(
            "first" to listOf(1, 2, 3, 4, 5, 6),
            "second" to listOf(11, 12, 13)
        )
    }

    "should combine with functionMonoid" {
        val fm = functionMonoid<String, List<String>>(listMonoid())

        fm.combine(
            { s -> listOf(s, s) },
            { s -> listOf(s.uppercase()) },
        )("one") shouldBe
                listOf("one", "one", "ONE")
    }
})

val num2word: (Int) -> String = {
    when (it) {
        1 -> "One"
        2 -> "Two"
        3 -> "Three"
        4 -> "Four"
        5 -> "Five"
        6 -> "Six"
        7 -> "Seven"
        8 -> "Eight"
        9 -> "Nine"
        10 -> "Ten"
        else -> TODO("Unsupported number")
    }
}
