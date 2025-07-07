package funkotlin.fp_in_kotlin_book.chapter10

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class WCTest : StringSpec({

    "WC monoid" {
        val wcMonoid = wordCount
        val nil = wcMonoid.nil

        // expect: id law
        wcMonoid.combine(Stub(""), nil) shouldBe Stub("")
        wcMonoid.combine(Stub(", "), nil) shouldBe Stub(", ")
        wcMonoid.combine(Stub("abc"), nil) shouldBe Stub("abc")
        wcMonoid.combine(nil, Stub("abc")) shouldBe Stub("abc")
        wcMonoid.combine(Part("lorem", 1, "do"), nil) shouldBe Part("lorem", 1, "do")
        wcMonoid.combine(nil, Part("lorem", 1, "do")) shouldBe Part("lorem", 1, "do")
        // and:
        wcMonoid.combine(Part("lorem", 1, "do"), Stub("able")) shouldBe Part("lorem", 1, "doable")
        wcMonoid.combine(Part("lorem", 1, "do"), Part("able", 0, "end")) shouldBe Part("lorem", 2, "end")
    }

    "should count words over String" {
        wordCount("") shouldBe 0
        wordCount(" one ") shouldBe 1
        wordCount("one") shouldBe 1
        wordCount("lorem ipsum do") shouldBe 3
        wordCount("lor sit amet, ") shouldBe 3
    }

    "should parse chunks in parallel" {
        val largeText = "lorem ipsum dolor sit amet, "
    }
})
