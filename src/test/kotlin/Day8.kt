import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day8 : Helpers {

    @Test
    fun part1() {
        assertEquals(travelFromTo(parseMap("day8example.txt")), 2)
        assertEquals(travelFromTo(parseMap("day8.txt")), 19783)
    }

    @Test
    fun part2() {
        val game = parseMap("day8example2.txt")
        // Find starting nodes.
        val startingNodes = game.nodes.values.filter { it.id.endsWith("A") }.toList()

        var current = startingNodes.toList()
        var i = 0L
        var isFinished = current.all{ it.id.endsWith("Z")}
        while (!isFinished) {
            // Tell all nodes to take a step
            val nextTurnIndex = i % game.turns.length
            val nextTurn = game.turns[nextTurnIndex.toInt()]
            current = current.map { node ->
                step(nextTurn, node, game)
            }
            i += 1
            isFinished = current.all{ it.id.endsWith("Z")}
        }
        assertEquals(i, 6)
    }

    private fun step(nextTurn: Char, current: Node, game: Game): Node {
        var current1 = current
        val nextNode = if (nextTurn == 'L') {
            current1.turns.first()
        } else {
            current1.turns.last()
        }
        current1 = game.nodes[nextNode]!!
        return current1
    }



    private fun travelFromTo(game: Game): Int {
        val start = "AAA"
        val end = "ZZZ"
        var current = game.nodes[start]!!
        var i = 0

        // Take turns until we reach ZZZ
        while (current.id != end) {
            val nextTurnIndex = i % game.turns.length
            val nextTurn = game.turns[nextTurnIndex]
            current = step(nextTurn, current, game)
            i += 1
        }
        return i
    }



    fun parseMap(filename: String): Game {
        val lines = stringsFromFile(filename).iterator()
        val turns = lines.next()
        lines.next()

        val nodes = mutableListOf<Node>()
        while (lines.hasNext()) {
            val line = lines.next()
            val split = line.split(" = ")
            val id = split[0]
            val pair = split[1]
            val turns = pair.substring(1, pair.length - 1).split(", ").toList()
            nodes.add(Node(id, turns))
        }
        val nodeMap = nodes.map { node ->
            Pair(node.id, node)
        }.toMap()
        return Game(turns, nodeMap)
    }

}

data class Game(val turns: String, val nodes: Map<String, Node>)
data class Node(val id: String, val turns: List<String>)