package funkotlin.fp_in_kotlin_book.chapter14

import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import arrow.mtl.State
import arrow.mtl.StateApi
import arrow.mtl.extensions.fx
import arrow.mtl.run
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class STTest : StringSpec({

    "should write and read local state" {
        val p1: ST<Nothing, Pair<Int, Int>> =
            STRef<Nothing, Int>(10).flatMap { r1: STRef<Nothing, Int> ->
                STRef<Nothing, Int>(20).flatMap { r2: STRef<Nothing, Int> ->
                    r1.read().flatMap { x: Int ->
                        r2.read().flatMap { y: Int ->
                            r1.write(y + 1).flatMap {
                                r2.write(x + 1).flatMap {
                                    r1.read().flatMap { a ->
                                        r2.read().map { b ->
                                            a to b
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // TODO runST(p1)
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
})

