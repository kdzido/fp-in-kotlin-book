package funkotlin.fp_in_kotlin_book.chapter09

import funkotlin.fp_in_kotlin_book.chapter02.head
import funkotlin.fp_in_kotlin_book.chapter02.tail
import funkotlin.fp_in_kotlin_book.chapter04.Left
import funkotlin.fp_in_kotlin_book.chapter04.Right
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.attempt
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.char
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.listOfN
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.or
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.run
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.slice
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.string
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.regexp
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.defer
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.fail
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.flatMap
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.many
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.map
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.product
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.scope
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.succeed
import funkotlin.fp_in_kotlin_book.chapter09.ParsersInterpreter.tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.codepoints
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import kotlin.run as kotlinRun
import kotlin.Result as kotlinResult

class ParsersTest : StringSpec({
    "parser should always succeed" {
        checkAll<String>(256) { s ->
            run(succeed(s), s) shouldBe Right(s)
        }
    }

    "parser should always fail" {
        checkAll<String>(256) { s ->
            run(fail(), s) shouldBe Left(ParseError(listOf(Location(s) to "FAIL")))
        }
    }

    "parser to recognize string" {
        checkAll<String>(256) { s ->
            run(string(s), s) shouldBe Right(s)
        }
    }
    "parser to recognize regex" {
        run(regexp("(a){3}"), "aaa") shouldBe Right("aaa")
    }

    "parser should not recognize string" {
        val input = "__OTHER__"
        checkAll<String>(256, Arb.string(minSize = 1)) { s ->
            run(string(s), input) shouldBe Left(ParseError(listOf(Location(input) to "Expected: $s")))
        }
    }

    "parser to recognize single character" {
        checkAll<Char>(256) { c ->
            run(char(c), c.toString()) shouldBe Right(c)
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
        run(slice(string("aaa")), "aaabbbaaa") == Right("aaa")
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

    "tagged parser should return parsing error location with the tag message" {
        // given
        val input = "hello world"
        val tagMsg = "expected magic word"

        // when
        val result = run(tag(tagMsg, string("abra")), input)
        // then
        val left: Left<ParseError> = result.shouldBeInstanceOf<Left<ParseError>>()
        errorStack(left.value).head shouldBe Pair(Location(input = input, offset = 0), tagMsg)
    }

    "composed tagged parsers should return stacked parse errors" {
        // given
        val tag1 = "first magic word"
        val tag2 = "second magic word"
        val input = "abraCadabra"

        // when
        val result = run(
            tag(tag1, string("abra")) product
            string(" ").many() product
            tag(tag2, string("cadabra")),
            input,
        )
        // then
        val left = result.shouldBeInstanceOf<Left<ParseError>>()
        errorStack(left.value).head shouldBe Pair(Location(input = input, offset = 0), tag2)
    }

    "scoped parser should return stacked parse errors" {
        // given
        val scopeMsg = "magic spell"
        val tag1 = "first magic word"
        val tag2 = "second magic word"
        val input = "abraCadabra"

        // when
        val result = run(
            scope(scopeMsg) { ->
                tag(tag1, string(" abra ")) product string(" ").many() product tag(tag2, string("cadabra"))
            },
            input,
        )
        // then
        val left = result.shouldBeInstanceOf<Left<ParseError>>()
        errorStack(left.value).head shouldBe Pair(Location(input = input, offset = 0), scopeMsg)
        errorStack(left.value).tail.head shouldBe Pair(Location(input = input, offset = 0), tag2)
    }

    "should control branching in parser" {
        // given
        val scopeMsg1 = "magic spell"
        val scopeMsg2 = "gibberish"
        val input = "abra cAdabra"
        // and
        val spaces = string(" ").many()
        val p1 = scope(scopeMsg1) { ->
            string("abra") product spaces product string("cadabra")
        }
        val p2 = scope(scopeMsg2) { ->
            string("abba") product spaces product string("babba")
        }

        // when
        val p = p1 or p2
        val left = run(p, input).shouldBeInstanceOf<Left<ParseError>>()
        // then
        errorStack(left.value).head shouldBe Pair(Location(input = input, offset = 0), scopeMsg1)
    }


}) {
    @Test
    fun `should attempt to execute branch of and fallback to 2nd branch`() {
        // given
        val scopeMsg2 = "short magic spell"
        val input = "abra cadabra"
        // and
        val spaces = string(" ").many()
        val p1 = string("abra") product spaces product string("abra") product string("cadabra")
        val p2 = scope("short magic spell") { -> string("abra") product spaces product string("cadabra") }
        val p = attempt(flatMap(p1) { _ -> fail() }) or p2

        // when
        val left = run(p, input).shouldBeInstanceOf<Left<ParseError>>()
        // then
        errorStack(left.value).head shouldBe Pair(Location(input = input, offset = 0), scopeMsg2)
    }
}

