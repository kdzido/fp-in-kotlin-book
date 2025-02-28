package funkotlin.fp_in_kotlin_book.chapter06.arrow

import arrow.core.ForId
import arrow.mtl.State
import arrow.core.Tuple2
import arrow.core.extensions.id.comonad.extract
import arrow.mtl.StateApi
import arrow.mtl.StateT
import arrow.mtl.stateSequential
import arrow.typeclasses.internal.IdBimonad

sealed class Input
object Coin : Input()
object Turn : Input()

data class Machine(
    val locked: Boolean,
    val candies: Int,
    val coins: Int,
) {
    fun transit(input: Input): Machine {
        return when {
            this.candies <= 0 -> this
            this.locked && input == Coin -> this.copy(locked = false, coins = this.coins + 1)
            !this.locked && this.candies >= 1 && input == Turn -> this.copy(locked = true, candies = this.candies - 1)
            else -> this
        }
    }

    companion object {
        fun simple(input: Input): State<Machine, Tuple2<Int, Int>> = State { m ->
            val newState = m.transit(input)
            Tuple2(newState, Tuple2(newState.coins, newState.candies))
        }

        // EXER-6.11 (hard)
        fun simulateMachine(
            theInputs: List<Input>,
        ): State<Machine, Tuple2<Int, Int>> = State { m: Machine ->
            val leftCoins =
                theInputs.map { input -> Machine.simple(input).flatMap(IdBimonad) { m -> StateApi.just(m) } }
            val ls: StateT<Machine, ForId, List<Tuple2<Int, Int>>> = leftCoins.stateSequential()
            ls.map(IdBimonad) { it.last() }.runF(m).extract()
        }
    }
}


