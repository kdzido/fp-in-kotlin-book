package funkotlin.fp_in_kotlin_book.chapter13

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class IOTest : StringSpec({

    "contest should print declared winner" {
        val p1 = Player("Joe", 3)
        val p2 = Player("Adam", 4)

        contest(p1, p2)
    }

    "contest2 should print declared winner" {
        val p1 = Player("Joe", 3)
        val p2 = Player("Adam", 4)

        contest2(p1, p2).run()
    }

    "should run forever" {
        val IM = IO.monad()

        val p: IO<Unit> = IM.forever<Unit, Unit>(
            stdout("Still going...")
        ).fix()

        p.run() // stack overflow
    }
})
