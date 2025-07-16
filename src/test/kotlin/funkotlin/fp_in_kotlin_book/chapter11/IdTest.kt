package funkotlin.fp_in_kotlin_book.chapter11

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class IdTest : StringSpec({
    "Id should unit" {
        Id.unit(123).a shouldBe 123
    }

    "Id should flatMap" {
        Id(1).flatMap { a: Int -> Id.unit((a + 1).toString()) } shouldBe Id("2")
    }

    "Id should map" {
        Id(1).map { a: Int -> (a + 1).toString() } shouldBe Id("2")
    }

    "law of identity in terms of compose" {
        val v = 123
        val m = idMonad()

        // expect: right identity
        m.compose<Int, Int, Int>({ a -> Id(a)} , { a -> m.unit(a) })(v).fix() shouldBe Id(v)

        // expect: left identity
        m.compose<Int, Int, Int>({ a -> m.unit(a) }, { a -> Id(a)})(v).fix() shouldBe Id(v)
    }

    "law of associativity in terms of compose" {
        val m = idMonad()
        val v = 123
        val f: (Int) -> IdOf<Int> = { x -> Id(x + 1) }
        val g: (Int) -> IdOf<Int> = { x -> Id(x * 2) }
        val h: (Int) -> IdOf<Int> = { x -> Id(x * x) }

        m.compose(m.compose(f, g), h)(v).fix() shouldBe
                m.compose(f, m.compose(g, h))(v).fix()
        // and:
        m.__compose(m.__compose(f, g), h)(v).fix() shouldBe
                m.__compose(f, m.__compose(g, h))(v).fix()
    }

    "try out idMonad" {
        val M: Monad<ForId> = idMonad()

        // expect:
        val id1: Id<String> = M.flatMap(Id("Hello, ")) { a ->
            M.flatMap(Id("monad!")) { b -> Id(a + b) }
        }.fix()
        id1 shouldBe Id("Hello, monad!")

        // expect:
        val id2: Id<String> = M.flatMap(Id("monad!")) { b ->
            M.flatMap(Id("Hello, ")) { a -> Id(a + b) }
        }.fix()
        id2 shouldBe Id("Hello, monad!")

        // expect:
        val id3: Id<String> = M.flatMap(Id("Hello, ")) { a ->
            M.map(Id("monad!")) { b -> a + b }
        }.fix()
        id3 shouldBe Id("Hello, monad!")
    }
})
