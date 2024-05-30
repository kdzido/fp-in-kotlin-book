package funkotlin.fp_in_kotlin_book.chapter01

// Listing 1.4
class Cafe {
    fun buyCoffee(cc: CreditCard): Pair<Coffee, Charge> = TODO()

    fun buyCoffees(
        cc: CreditCard,
        n: Int
    ): Pair<List<Coffee>, Charge> {
        val purchases: List<Pair<Coffee, Charge>> = List(n) { buyCoffee(cc)}
        val (coffees, charges) = purchases.unzip()

        return Pair(
            coffees,
            charges.reduce { c1, c2 -> c1.combine(c2)}
        )
    }
}

// Listing 1.6
fun List<Charge>.coalesce(): List<Charge> =
    this.groupBy { it.cc }.values
        .map { it.reduce { a, b -> a.combine(b) } }

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
