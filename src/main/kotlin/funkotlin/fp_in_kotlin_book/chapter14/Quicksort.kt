package funkotlin.fp_in_kotlin_book.chapter14

fun quicksort(xs: List<Int>): List<Int> =
    if (xs.isEmpty()) xs else {
        val arr = xs.toIntArray()

        fun swap(x: Int, y: Int) {
            val tmp = arr[x]
            arr[x] = arr[y]
            arr[y] = tmp
        }

        fun partition(n: Int, r: Int, pivot: Int): Int {
            val pivotVal = arr[pivot]
            swap(pivot, r)
            var j = n
            for (i in n until r) if (arr[i] < pivotVal) {
                swap(i, j)
                j += 1
            }
            swap(j, r)
            return j
        }

        fun qs(n: Int, r: Int): Unit = if (n < r) {
            val pi = partition(n, r, n + (r - n) / 2)
            qs(n, pi - 1)
            qs(pi + 1, r)
        } else Unit

        qs(0, arr.size - 1)
        arr.toList()
    }

fun main() {
    println("sorted: ${quicksort(listOf(3, 1, 2, 4, 5))}")

}
