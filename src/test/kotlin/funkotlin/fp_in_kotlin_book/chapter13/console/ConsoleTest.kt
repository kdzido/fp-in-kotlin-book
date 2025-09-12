package funkotlin.fp_in_kotlin_book.chapter13.console

import funkotlin.fp_in_kotlin_book.chapter04.Option
import funkotlin.fp_in_kotlin_book.chapter04.Some
import funkotlin.fp_in_kotlin_book.chapter13.Free
import funkotlin.fp_in_kotlin_book.chapter13.flatMap
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.String

class ConsoleTest : StringSpec({
    val f1: Free<ForConsole, Option<String>> =
        Console.stdout("I can only interact with the console")
            .flatMap { _ -> Console.stdin() }

    val f2 = Console.stdin()
        .flatMap { ins: Option<String> -> Console.stdout("Console echo: $ins") }

    "should runConsoleReader" {
        val cr: Option<String> = runConsoleReader(f1).fix().run("Some input..")

        cr shouldBe Some("Some input..")
    }

    "should runConsoleState" {
        val b = Buffers(
            input = listOf("firstInput", "lastInput"),
            output = listOf("output1", "output2"),
        )
        val (sa2, sb2) = runConsoleState(f2).fix().run(b)
        println("runConsoleState: ($sa2, $sb2)")

        sa2 shouldBe Unit
        sb2 shouldBe Buffers(
            input = listOf("firstInput"),
            output = listOf("output1", "output2", "Console echo: Some(value=lastInput)"),
        )
    }
})
