package funkotlin.fp_in_kotlin_book.chapter08

import funkotlin.fp_in_kotlin_book.chapter07.Pars
import io.kotest.core.spec.style.StringSpec
import java.util.concurrent.Executors

class ParTestingTest : StringSpec({
    "should ensure Par map over unit law" {
        val es = Executors.newCachedThreadPool()

        val p = Prop.check {
            val p1 = Pars.map(Pars.unit(1)) { it + 1}
            val p2 = Pars.unit(2)
            p1(es).get() == p2(es).get()
        }
        Prop.run(p)
    }

    "should equal Par map over unit law" {
        val es = Executors.newCachedThreadPool()

        val p = Prop.check {
            val p1 = Pars.map(Pars.unit(1)) { it + 1}
            val p2 = Pars.unit(2)
            equal(p1, p2)(es).get()
        }
        Prop.run(p)
    }

    "should checkPar of Par map over unit law" {
        val p2 = checkPar(equal(
            Pars.map(Pars.unit(1)) { it + 1},
            Pars.unit(2)
        ))
        Prop.run(p2)
    }
})
