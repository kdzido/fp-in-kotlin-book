package funkotlin.fp_in_kotlin_book.chapter14

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class STTest : StringSpec({

    "should write and read local state" {
        val p3 = object : RunnableST<Pair<Int, Int>> {
            override fun <S> invoke(): ST<S, Pair<Int, Int>> {
                return STRef<S, Int>(10).flatMap { r1: STRef<S, Int> ->
                    STRef<S, Int>(20).flatMap { r2: STRef<S, Int> ->
                        r1.read().flatMap { x: Int ->
                            r2.read().flatMap { y: Int ->
                                r1.write(y + 1).flatMap {
                                    r2.write(x + 1).flatMap {
                                        r1.read().flatMap { a: Int ->
                                            r2.read().map { b: Int ->
                                                a to b
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // expect:
        ST.runST(p3) shouldBe Pair(21, 11)
    }

    "should write and read local state with Arrow for-comprehension" {
//        val p2 = ST.fx<Nothing, Pair<Int, Int>>(Id.monad()) {
//            val r1 = STRef<Nothing, Int>(10).bind()
//            val r2 = STRef<Nothing, Int>(20).bind()
//            val x = r1.read().bind()
//            val y = r2.read().bind()
//            r1.write(y + 1).bind()
//            r2.write(x + 1).bind()
//            val a = r1.read().bind()
//            val b = r2.read().bind()
//            a to b
//        }

        // TODO runST(p2)
    }

    "should write and read local array state" {
        val p3 = object : RunnableST<List<Int>> {
            override fun <S> invoke(): ST<S, List<Int>> {
                return STArray<S, Int>(10, 1).flatMap { r1: STArray<S, Int> ->
                    r1.read(1).flatMap { x: Int ->
                        r1.write(2, x + 1).flatMap { _: Unit ->
                            r1.write(3, x + 2).flatMap { _: Unit ->
                                r1.freeze().map { ls: List<Int> ->
                                    ls
                                }
                            }
                        }
                    }
                }
            }
        }

        // expect:
        ST.runST(p3) shouldBe listOf(1, 1, 2, 3, 1, 1, 1, 1, 1, 1)
    }

    "should fill array from map" {
        val p4 = object : RunnableST<List<String>> {
            override fun <S> invoke(): ST<S, List<String>> {
                return STArray<S, String>(5, "<nil>").flatMap { r1: STArray<S, String> ->
                        r1.fill(
                            mapOf(
                                0 to "zero",
                                1 to "one",
                                2 to "two",
                            )
                        ).flatMap { _ -> r1.freeze() }
                    }
            }
        }

        // expect:
        ST.runST(p4) shouldBe listOf("zero", "one", "two", "<nil>", "<nil>")
    }
})

