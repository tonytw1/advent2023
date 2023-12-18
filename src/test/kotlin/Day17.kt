import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.*


class Day17 : Helpers {

    @Test
    fun part1() {
        val maxStraightLine = 3
        assertEquals(bestPath("day17example.txt", maxStraightLine, 0), 102)
        assertEquals(bestPath("day17.txt", maxStraightLine, 0), 817)
    }

    @Test
    fun part2() {
        val maxStraightLine = 10
        val minStraightLne = 4
        assertEquals(bestPath("day17example.txt", maxStraightLine, minStraightLne), 94)
        assertEquals(bestPath("day17example2.txt", maxStraightLine, minStraightLne), 71)
        assertEquals(bestPath("day17.txt", maxStraightLine, minStraightLne), 925)
    }

    private fun bestPath(filename: String, maxStraightLine: Int, minStraightLne: Int): Int {
        val input = readInputToArray(filename)

        // Translate array of costs into a traditional adj graph
        // First to an array of nodes; can probably remove this step
        val maxY = input.size - 1
        val maxX = input[0].size - 1
        val nodes = input.withIndex().map { row ->
            row.value.withIndex().map { c ->
                Node(c.value, row.index, c.index)
            }.toTypedArray()
        }.toTypedArray()
        // Then to adj map
        val adjMap = mutableMapOf<Node, List<Node>>()
        (0..maxY).forEach { y ->
            (0..maxX).forEach { x ->
                val node = nodes[y][x]
                val vertical = (listOf(y - 1, 0).max()..listOf(y + 1, maxY).min()).map { ay ->
                    nodes[ay][x]
                }
                val horizontal = (listOf(x - 1, 0).max()..listOf(x + 1, maxX).min()).map { ax ->
                    nodes[y][ax]
                }
                adjMap[node] = (vertical + horizontal).filter { it != node }
            }
        }

        // Setup for a least distance; the key is not just nodes; it's node + direction + remaining straight line
        val visited = mutableSetOf<Arrival>()  // Arrivals which have already been explored
        val distanceTo = mutableMapOf<Arrival, Int>()

        // Start at top left
        // There are 2 starting tracks to enqueue; heading east, heading south; both with 1 square already placed
        val start = nodes[0][0]
        val startingEast = Arrival(start, Pair(0, 1), 1)
        val startingSouth = Arrival(start, Pair(1, 0), 1)
        distanceTo[startingEast] = 0
        distanceTo[startingSouth] = 0

        val byClosestFirst: Comparator<Arrival> = compareBy { distanceTo.getOrDefault(it, Int.MAX_VALUE) }
        val queue = PriorityQueue(byClosestFirst)
        queue.add(startingEast)
        queue.add(startingSouth)

        fun possibleNextDestinations(current: Arrival): List<Arrival> {
            fun nodeAhead(node: Node, dir: Pair<Int, Int>) = adjMap[node]!!.find { an ->
                an.y == node.y + dir.first && an.x == node.x + dir.second
            }

            val next = mutableListOf<Arrival>()
            val dist = current.distanceInStraightLine
            val dir = current.dir
            if (dist < maxStraightLine) {
                // If we have remaining dist them forward is available
                val fwd = nodeAhead(current.node, dir)
                if (fwd != null) {
                    next.add(Arrival(node = fwd, dir = dir, distanceInStraightLine = dist + 1))
                }
            }
            if (dist >= minStraightLne) {
                val left = Pair(dir.second, -dir.first)
                val leftNode = nodeAhead(current.node, left)
                if (leftNode != null) {
                    next.add(Arrival(node = leftNode, dir = left, distanceInStraightLine = 1))
                }
                val right = Pair(-dir.second, dir.first)
                val rightNode = nodeAhead(current.node, right)
                if (rightNode != null) {
                    next.add(Arrival(node = rightNode, dir = right, distanceInStraightLine = 1))
                }
            }
            return next.filter { it != current }
        }

        // Normal least distance algo
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            val availableNextSteps = possibleNextDestinations(current)
            availableNextSteps.forEach { adjNode ->
                if (!visited.contains(adjNode)) {
                    val costToNode = distanceTo[current]!! + adjNode.node.cost
                    if (distanceTo.getOrDefault(adjNode, Int.MAX_VALUE) == Int.MAX_VALUE) {
                        distanceTo[adjNode] = costToNode
                        queue.offer(adjNode)
                    }
                }
            }
            visited.add(current)
        }

        val end = nodes[maxY][maxX]
        val validEndings = distanceTo.filter { it.key.node == end && it.key.distanceInStraightLine >= minStraightLne }
        return validEndings.map { it.value }.min()
    }

    private fun readInputToArray(filename: String): Array<Array<Int>> {
        return stringsFromFile(filename).withIndex().map { line ->
            val map = line.value.toCharArray().withIndex().map { c ->
                ("" + c.value).toInt()
            }
            val toTypedArray: Array<Int> = map.toTypedArray()
            toTypedArray
        }.toTypedArray()
    }
}

data class Arrival(val node: Node, val dir: Pair<Int, Int>, val distanceInStraightLine: Int)

data class Node(val cost: Int, val y: Int, val x: Int)
