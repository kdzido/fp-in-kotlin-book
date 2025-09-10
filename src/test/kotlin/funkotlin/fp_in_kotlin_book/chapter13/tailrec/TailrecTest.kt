package funkotlin.fp_in_kotlin_book.chapter13.tailrec

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TailrecTest : StringSpec({

    "contest should print declared winner" {
        val p1 = Player("Joe", 3)
        val p2 = Player("Adam", 4)

        contest(p1, p2)
    }

    "contest2 should print declared winner" {
        val p1 = Player("Joe", 3)
        val p2 = Player("Adam", 4)

        runM(contest2(p1, p2))
    }

    "should run forever" {
        val IM = Tailrec.monad()

        val p: Tailrec<Unit> = IM.forever<Unit, Unit>(
            stdout("To infinity ...")
        ).fix()

//        runM(p) // never engine
    }

    "should run out of call frames" {
        val f: (Int) -> Int = { x: Int -> x }
        val g: (Int) -> Int = List(100_000) { idx -> f }
            .fold(f) { ff, h -> { n: Int -> ff(h(n)) } }

//        g(42) shouldBe 42 // causes StackOverFlow
    }

    "should compute large fold" {
        val f: (Int) -> Return<Int> = { x: Int -> Return(x) }
        val g: (Int) -> Tailrec<Int> = List(100000) { idx -> f }
            .fold(f) { a: (Int) -> Tailrec<Int>, b: (Int) -> Tailrec<Int> ->
                { x : Int ->
                    Suspend { Unit }.flatMap { _ -> a(x).flatMap(b) }
                }
            }

        runM(g(42)) shouldBe 42
    }
})
