package funkotlin.fp_in_kotlin_book.chapter01

// Listing 1.2
class Cafe {
    fun buyCoffee(cc: CreditCard, p: Payments): Coffee {
        val cup = Coffee()
        p.charge(cc, cup.price)
        return cup
    }
}

class Coffee {
    val price: Price = Price(0.0)
}

interface Payments {
    fun charge(cc: CreditCard, price: Price)
}

interface CreditCard

data class Price(val d: Double)
