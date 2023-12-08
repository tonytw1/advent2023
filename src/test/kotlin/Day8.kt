import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day8 : Helpers {

    @Test
    fun part() {
        assertEquals(travelFromTo(parseMap("day8example.txt")), 2)
        assertEquals(travelFromTo(parseMap("day8.txt")), 19783)
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