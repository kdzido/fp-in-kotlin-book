package funkotlin.fp_in_kotlin_book.chapter01

// Listing 1.3
class Cafe {
    fun buyCoffee(cc: CreditCard, p: Payments): Pair<Coffee, Charge> {
        val cup = Coffee()
        return Pair(cup, Charge(cc, cup.price))
    }
}

class Coffee {
    val price: Float = 0.0F
}

// Listing 1.4 charge as data type
data class Charge(val cc: CreditCard, val amount: Float) {
    fun combine(other: Charge): Charge =
        if (cc == other.cc)
            Charge(cc, amount + other.amount)
        else throw Exception(
            "Cannot combine charges to different cards"
        )
}

interface Payments {
    fun charge(cc: CreditCard, price: Price)
}

interface CreditCard

data class Price(val d: Float)
