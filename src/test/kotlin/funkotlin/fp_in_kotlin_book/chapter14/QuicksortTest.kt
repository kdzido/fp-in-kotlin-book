package funkotlin.fp_in_kotlin_book.chapter14

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import funkotlin.fp_in_kotlin_book.chapter14.Quicksort

class QuicksortTest : StringSpec({
    "should partition array" {
        val a = listOf(1, 5, 3, 1, 2).toIntArray()

        Quicksort.partition0(a, 0, 4, 2) shouldBe 3

        a.toList() shouldBe listOf(1, 2, 1, 3, 5)
    }

    "should swap STSArray elements" {
        val p5 = object : RunnableST<List<String>> {
            override fun <S> invoke(): ST<S, List<String>> =
                fromList<S, String>(listOf("zero", "two", "one")).flatMap { r1 ->
                    r1.swap(1, 2).flatMap {
                        r1.freeze()
                    }
                }
        }

        // expect:
        ST.runST(p5) shouldBe listOf("zero", "one", "two")
    }

    "should quicksort STArray" {
        // given:
        val l = listOf(1, 5, 3, 1, 2)

        // expect:
        Quicksort.quicksort(l) shouldBe listOf(1, 1, 2, 3, 5)
    }

    "should qs STArray" {
        // given:
        val l = listOf(1, 5, 3, 1, 2)

        // expect:
        val p = object : RunnableST<List<Int>> {
            override fun <S> invoke(): ST<S, List<Int>> {
                val v1: ST<S, List<Int>> = fromList<S, Int>(l).flatMap { ar ->
                    Quicksort.qs(ar, 0, l.size - 1).flatMap { rn ->
                        ar.freeze()
                    }
                }
                return v1
            }
        }
        ST.runST(p) shouldBe listOf(1, 1, 2, 3, 5)
    }

    "should partition STArray" {
        // given:
        val l = listOf(1, 5, 3, 1, 2)

        // expect:
        val p = object : RunnableST<List<Int>> {
            override fun <S> invoke(): ST<S, List<Int>> {
                val v1: ST<S, List<Int>> = fromList<S, Int>(l).flatMap { ar ->
                    Quicksort.partition(ar, 0, 4, 2).flatMap { rn ->
                        ar.freeze()
                    }
                }
                return v1
            }
        }
        ST.runST(p) shouldBe listOf(1, 2, 1, 3, 5)
    }
})

