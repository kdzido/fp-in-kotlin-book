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

object Quicksort {
    fun quicksort(xs: List<Int>): List<Int> =
        if (xs.isEmpty()) xs else ST.runST(object : RunnableST<List<Int>> {
            override fun <S> invoke(): ST<S, List<Int>> =
                fromList<S, Int>(xs).flatMap { arr: STArray<S, Int> ->
                    arr.size.flatMap { size ->
                        qs(arr, 0, size - 1).flatMap {
                            arr.freeze()
                        }
                    }
                }
        })

    // ops for quicksort
    fun <S> partition(
        arr: STArray<S, Int>,
        n: Int,
        r: Int,
        pivot: Int
    ): ST<S, Int> =
        arr.read(pivot).flatMap { vp: Int ->
            arr.swap(pivot, r).flatMap {
                STRef<S, Int>(n).flatMap { j: STRef<S, Int> ->
                    (n until r).fold(noop<S>()) { st, i: Int ->
                        st.flatMap {
                            arr.read(i).flatMap { vi ->
                                if (vi < vp) {
                                    j.read().flatMap { vj ->
                                        arr.swap(i, vj).flatMap {
                                            j.write(vj + 1)
                                        }
                                    }
                                } else noop()
                            }
                        }
                    }.flatMap {
                        j.read().flatMap { j: Int ->
                            arr.swap(r, j)
                        }.flatMap {
                            j.read()
                        }
                    }
                }
            }
        }

    fun <S> qs(arr: STArray<S, Int>, l: Int, r: Int): ST<S, Unit> =
        if (l < r) {
            partition(arr, l, r, l + (r - l) / 2).flatMap { pi ->
                qs(arr, l, pi - l).flatMap {
                    qs(arr, pi + 1, r)
                }
            }
        } else noop()

    fun <S> noop() = ST<S, Unit> { Unit }

    fun partition0(arr: IntArray, n: Int, r: Int, pivot: Int): Int {
        val pivotVal = arr[pivot]
        swap0(arr, pivot, r)
        var j = n
        for (i in n until r) if (arr[i] < pivotVal) {
            swap0(arr, i, j)
            j += 1
        }
        swap0(arr, j, r)
        return j
    }

    fun swap0(arr: IntArray, x: Int, y: Int) {
        val tmp = arr[x]
        arr[x] = arr[y]
        arr[y] = tmp
    }
}


fun main() {
    println("sorted: ${quicksort(listOf(3, 1, 2, 4, 5))}")

}
