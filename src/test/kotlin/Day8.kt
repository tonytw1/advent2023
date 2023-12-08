import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day8 : Helpers {

    @Test
    fun part1() {
        val isZZZNode: (Node) -> Boolean = { node -> node.id == "ZZZ" }
        assertEquals(travelFromTo(parseMap("day8example.txt"), "AAA", isZZZNode), 2)
        assertEquals(travelFromTo(parseMap("day8.txt"), "AAA", isZZZNode), 19783)
    }

    @Test
    fun part2() {
        assertEquals(whenPathsCross("day8example2.txt"), 6)
        assertEquals(whenPathsCross("day8.txt"), 9177460370549)
    }

    private fun whenPathsCross(filename: String): Long {
        val game = parseMap(filename)

        val startingNodes = game.nodes.values.filter { it.id.endsWith("A") }
        // Each starting node gets into a repeating loop with a fixed period; work these out
        val periods = startingNodes.map { startingNode: Node ->
            val isZNode: (Node) -> Boolean = { node -> node.id.endsWith("Z") }
            travelFromTo(game, startingNode.id, isZNode).toLong()
        }

        // If you multiplied these together, you'd find a location where they all meet up but it's not the lowest possible one.
        // val tooHigh = periods.reduce { acc, it -> acc * it }
        // The answer is the least common multiplier of the periods
        return leastCommonMultipleOf(periods)
    }

    @Test
    fun leastCommonMultiple() {
        assertEquals(leastCommonMultipleOf(listOf(4, 7, 12, 21, 42)), 84)
    }

    private fun travelFromTo(game: Game, start: String, isFinished: (Node) -> Boolean): Int {
        var current = game.nodes[start]!!
        var i = 0
        while (!isFinished(current)) {
            val nextTurnIndex = i % game.turns.length
            val nextTurn = game.turns[nextTurnIndex]
            val nextNode = if (nextTurn == 'L') {
                current.turns.first()
            } else {
                current.turns.last()
            }
            current = game.nodes[nextNode]!!
            i += 1
        }
        return i
    }

    private fun leastCommonMultipleOf(periods: List<Long>): Long {
        // By table method https://en.wikipedia.org/wiki/Least_common_multiple
        val headers = mutableListOf<Long>()
        val table = periods.associateWith { n ->
            mutableListOf(n)
        }

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

    private fun parseMap(filename: String): Game {
        val lines = stringsFromFile(filename).iterator()
        val turns = lines.next()
        lines.next()

        val nodes = mutableMapOf<String, Node>()
        while (lines.hasNext()) {
            val line = lines.next()
            val split = line.split(" = ")
            val id = split[0]
            val pair = split[1]
            nodes[id] = Node(id, pair.substring(1, pair.length - 1).split(", ").toList())
        }
        return Game(turns, nodes.toMap())
    }

    private fun primesUpToo(n: Long) = (2..n).filter { num -> (2..<num).none { num % it == 0L } }

    data class Game(val turns: String, val nodes: Map<String, Node>)
    data class Node(val id: String, val turns: List<String>)
}
