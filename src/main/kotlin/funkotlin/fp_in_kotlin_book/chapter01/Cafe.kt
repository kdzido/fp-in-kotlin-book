package funkotlin.fp_in_kotlin_book.chapter01

// Listing 1.1
class Cafe {
    fun buyCoffee(cc: CreditCard): Coffee {
        val cup = Coffee()
        cc.charge(cup.price)
        return cup
    }
}

class Coffee {
    val price: Price = Price(0.0)
}

interface CreditCard {
    fun charge(price: Price)
}

data class Price(val d: Double)
