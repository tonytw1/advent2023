import java.io.File

interface Helpers {
    fun stringsFromFile(filename: String): List<String> {
        val input = File(javaClass.classLoader.getResource(filename).toURI())
        return input.readLines()
    }

    fun intsFromFile(filename: String): List<Int> {
        return stringsFromFile(filename).map { it.toInt() }
    }

    fun longsFromFile(filename: String): List<Long> {
        return stringsFromFile(filename).map { it.toLong() }
    }

    val multipleBlankSpace: Regex
        get() = Regex("\\s+")

    fun leastCommonMultipleOf(periods: List<Long>): Long {
        // By table method https://en.wikipedia.org/wiki/Least_common_multiple
        val headers = mutableListOf<Long>()
        val table = periods.associateWith { n ->
            mutableListOf(n)
        }

        fun primesUpToo(n: Long) = (2..n).filter { num -> (2..<num).none { num % it == 0L } }

        val primes = primesUpToo(periods.max())

        fun iterate(numbers: List<Long>, lowestPrime: Long): Pair<Long, List<Long>> {
            val primesToTry = primes.filter { it -> it >= lowestPrime }
            val primeToUse = primesToTry.find { prime ->
                numbers.any { it % prime == 0L }    // dividesAnyOfTheNumbersEvenly
            }!!
            val rows = numbers.map { n ->
                if (n % primeToUse == 0L) {
                    n / primeToUse
                } else {
                    n
                }
            }
            return Pair(primeToUse, rows)
        }

        var lowestPrime = 2L
        var isFinished = false
        while (!isFinished) {
            val rightMostColumn = periods.map { i ->
                table[i]!!.last()
            }
            val result = iterate(rightMostColumn, lowestPrime)

            // Append row to tables
            headers.add(result.first)
            periods.forEach { n ->
                table[n]!!.add(result.second.get(periods.indexOf(n)))
            }
            // Check if finished
            isFinished = result.second.all { it == 1L }
        }
        return headers.reduce { acc, it -> acc * it }
    }

}