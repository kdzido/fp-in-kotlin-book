package funkotlin.fp_in_kotlin_book.chapter08

import arrow.core.extensions.list.foldable.firstOption
import arrow.core.lastOrNone
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.forAll
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list

class PropertyTest : StringSpec({
    "List reverse" {
        val intList = Arb.list(Arb.int(0, 100), 1..100)

        forAll(intList) { list ->
            (list.firstOption() == list.reversed().lastOrNone())
                    (list.reversed().reversed() == list)
        }
    }

    "Sum of list of same value" {
        val sameValueList = Arb.list(Arb.int(50, 50), 1..100)

        forAll(sameValueList) { ls ->
            (ls.sum() == ls.size * 50) and
                    (ls.reversed().sum() == ls.sum())
        }
    }

    "Sum of list" {
        val intList = Arb.list(Arb.int(0, 100), 1..100)

        forAll(intList) { ls ->
            (ls.reversed().sum() == ls.sum())
        }
    }

    "Max of list of 50s" {
        val sameValueList = Arb.list(Arb.int(50, 50), 1..100)

        forAll(sameValueList) { ls ->
            ls.max() == 50
        }
    }

    "Max of list" {
        val intList = Arb.list(Arb.int(0, 100), 1..100)

        forAll(intList) { ls ->
            ls.reversed().max() == ls.max()
        }
    }
})

