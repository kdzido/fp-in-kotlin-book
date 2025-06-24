package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.char
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.listOfN
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.or
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.run
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.slice
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.string
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.defer
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.many
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.map
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.product
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
        run(or(string("abra"), string("cadabra").defer()), "abra") == Right("abra")
        run(or(string("abra"), string("cadabra").defer()), "cadabra") == Right("cadabra")
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

    "parser to recognize repetitions zero or more times" {
        run(char('a').many(), "aaa") == Right(3)
        run(char('a').many(), "") == Right(0)
        run(char('a').many(), "b123") == Right(0)
        run(char('a').many(), "bbbaaa") == Right(0) // from beginning only?
    }

    "combiner to map over parser" {
        run(map(char('a').many()) { it.size }, "aaa") == Right(3)
        run(map(char('a').many()) { it.size }, "") == Right(0)
        run(map(char('a').many()) { it.size }, "b123") == Right(0)
        run(map(char('a').many()) { it.size }, "bbbaaa") == Right(0) // from beginning only?
    }

    "numA" {
        val numA: Parser<Int> = map(char('a').many()) { it.size }

        run(numA, "aaa") == Right(3)
        run(numA, "") == Right(0)
        run(numA, "b123") == Right(0)
        run(numA, "bbbaaa") == Right(0) // from beginning only?
    }

    "slice" {
        run(slice(("a" or "b").many()), "abba") == Right("abba")
    }

    "count chars" {
        run(map(slice(char('a').many())) { it.length }, "aaba") == Right(2)
    }

    "parser counting 'a' chars followed parser counting 'b' chars" {
        val abp: Parser<Pair<Int, Int>> =
            map(slice(char('a').many())) { it.length } product
                    map(slice(char('b').many())) { it.length }

        run(abp, "aabbb") == Right(Pair(2, 3))
    }
})

