package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.char
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.charZeroOrMore
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.listOfN
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.or
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.run
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.string
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class ParsersTest : StringSpec({
    "parser to recognize single character" {
        checkAll<Char>(256) { c ->
            run(char(c), c.toString()) shouldBe Right(c)
        }
    }

    "parser to recognize string" {
        checkAll<String>(256) { s ->
            run(string(s), s) shouldBe Right(s)
        }
    }

    "or parser to recognize either parser" {
        run(or(string("abra"), string("cadabra")), "abra") == Right("abra")
        run(or(string("abra"), string("cadabra")), "cadabra") == Right("cadabra")
    }

    "or parser to recognize either string" {
        run("abra" or "cadabra", "abra") == Right("abra")
    }

    "listOfN parser" {
        run(listOfN(3, "ab" or "cad"), "ababab") == Right("ababab")
        run(listOfN(3, "ab" or "cad"), "cadcadcad") == Right("cadcadcad")
        run(listOfN(3, "ab" or "cad"), "ababcad") == Right("ababcad")
        run(listOfN(3, "ab" or "cad"), "cadabcad") == Right("cadabcad")
    }

    "parser to recognize seq of zero or more of chars" {
        run(charZeroOrMore('a'), "aaa") == Right(3)
        run(charZeroOrMore('a'), "abba") == Right(2)
        run(charZeroOrMore('a'), "0") == Right(0)
        run(charZeroOrMore('a'), "b123") == Right(0)
    }

    "parser to recognize repetitions" {
        run(char('a').many(), "aaa") == Right(3)
        run(char('a').many(), "") == Right(0)
        run(char('a').many(), "b123") == Right(0)
        run(char('a').many(), "bbbaaa") == Right(0) // from beginning only?
    }

    "combiner to map over parser" {
        run(char('a').many().map { it.size }, "aaa") == Right(3)
        run(char('a').many().map { it.size }, "") == Right(0)
        run(char('a').many().map { it.size }, "b123") == Right(0)
        run(char('a').many().map { it.size }, "bbbaaa") == Right(0) // from beginning only?
    }

    "numA" {
        val numA: Parsers.Parser<Int> = char('a').many().map { it.size }

        run(numA, "aaa") == Right(3)
        run(numA, "") == Right(0)
        run(numA, "b123") == Right(0)
        run(numA, "bbbaaa") == Right(0) // from beginning only?
    }

    "slice" {
        run(("a" or "b").many().slice(), "abba") == Right("abba")
    }

    "count chars" {
        run(char('a').many().slice().map { it.length }, "aaba") == Right(2)
    }
})

