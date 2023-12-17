import org.testng.annotations.Test

class Day17 : Helpers {

    @Test
    fun part1() {

        // Build a graph of the network with an adj graph which ignores the dynamics of the carts
        // Parse the input into an array for starters

        val filename = "day17example..txt"
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

        // From the starting point we could do a traditional least distance build out is the step size always 1
        val start = nodes[0][0]
        val end = nodes[maxY][maxX]

        val visited = mutableSetOf<Node>()  // TODO this will be expanded to account for different arrive at the node options

        val distanceTo = mutableMapOf<Node, Int>()  // TODO this will be change depending on how you arrive
        adjMap.keys.forEach { distanceTo[it] = Int.MAX_VALUE }
        distanceTo[start] = 0

        val queue = ArrayDeque<Node>()
        queue.add(start)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            val availableNextSteps = adjMap[current]!!  // TODO this will be expanded to fit the problems

            availableNextSteps.forEach { adjNode ->
                if (!visited.contains(adjNode)) {
                    val costToNode = distanceTo[current]!! + adjNode.cost
                    if (distanceTo[adjNode]!! > costToNode) {
                        distanceTo[adjNode] = costToNode
                    }
                }
            }
            visited.add(current)

            availableNextSteps.sortedBy { adjNode ->
                distanceTo[adjNode]!!
            }.filter { !visited.contains(it) }.forEach {
                queue.add(it)
            }
        }

        println(distanceTo[end]!!)
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

data class Node(val id: Int, val cost: Int, val y: Int, val x: Int)
