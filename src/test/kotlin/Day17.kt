import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.*


private const val maxStraightLine = 3

class Day17 : Helpers {

    @Test
    fun part1() {
        assertEquals(bestPath("day17example.txt"), 102)
        assertEquals(bestPath("day17.txt"), 817)
    }

    private fun bestPath(filename: String): Int {
        val input = readInputToArray(filename)

        // Translate this into a traditional adj graph
        // Build nodes
        val maxY = input.size - 1
        val maxX = input[0].size - 1
        var nodeId = 0

        val nodes = input.withIndex().map { row ->
            row.value.withIndex().map { c ->
                nodeId += 1
                Node(nodeId, c.value, row.index, c.index)
            }.toTypedArray()
        }.toTypedArray()

        val adjMap = mutableMapOf<Node, List<Node>>()
        (0..maxY).forEach { y ->
            (0..maxX).forEach { x ->
                val node = nodes[y][x]
                val vertical = (listOf(y - 1, 0).max()..listOf(y + 1, maxY).min()).map { ay ->
                    nodes[ay][x]
                }
                val horizontal = (listOf(x - 1, 0).max()..listOf(x + 1, maxY).min()).map { ax ->
                    nodes[y][ax]
                }
                adjMap[node] = (vertical + horizontal).filter { it != node }
            }
        }

        val start = nodes[0][0]
        // There are 2 starting points to enqueue; 0,0 heading east, and 0,0 heading south; both with full distance available
        val startingEast = Arrival(start, Pair(0, 1), 1)
        val startingSouth = Arrival(start, Pair(1, 0), 1)

        val visited = mutableSetOf<Arrival>()  // Arrivals which have already been explored

        val distanceTo = mutableMapOf<Arrival, Int>()  // TODO this will be change depending on how you arrive
        distanceTo[startingEast] = 0
        distanceTo[startingSouth] = 0

        val byClosestFirst: Comparator<Arrival> = compareBy { distanceTo.getOrDefault(it, Int.MAX_VALUE) }
        val queue = PriorityQueue<Arrival>(byClosestFirst)
        queue.add(startingEast)
        queue.add(startingSouth)

        fun nodeAhead(node: Node, dir: Pair<Int, Int>) = adjMap[node]!!.find { an ->
            an.y == node.y + dir.first && an.x == node.x + dir.second
        }

        fun possibleNextDestinations(current: Arrival): List<Arrival> {
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

            // Left and right a always available
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
            return next.filter { it != current }
        }

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
        return distanceTo.filter { it.key.node == end }.map { it.value }.min()
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

data class Node(val id: Int, val cost: Int, val y: Int, val x: Int)
