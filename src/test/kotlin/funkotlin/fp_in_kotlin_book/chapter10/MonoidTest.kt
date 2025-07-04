package funkotlin.fp_in_kotlin_book.chapter10

import funkotlin.fp_in_kotlin_book.chapter04.None
import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

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
})
