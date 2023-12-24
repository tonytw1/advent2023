import org.testng.annotations.Test

class Day23 : Helpers {

    @Test
    fun part1() {
        // Parse the map to an array of chars; we can discard it once we have a graph
        val map = stringsFromFile("day23example.txt").withIndex().map { line ->
            line.value.toCharArray()
        }.toTypedArray()

        // Start and end are give
        val start = Point(0, 1)
        val end = Point(21, 22)
        val visited = mutableSetOf<Point>()
        fun neighboursOf(p: Point): Set<Point> {
            val neighbours = mutableListOf<Point>()
            val minY = 0
            val maxY = map.size -1
            val minX = 0
            val maxX = map[0].size -1
            for (y in listOf(p.y - 1, minY).max()..listOf(p.y + 1, maxY).min()) {
                val n = Point(y, p.x)
                neighbours.add(n)
            }
            for (x in listOf(p.x - 1, minX).max()..listOf(p.x + 1, maxX).min()) {
                val n = Point(p.y, x)
                neighbours.add(n)
            }
            return neighbours.filter { it != p && map[it.y][it.x] != '#'}.toSet()
        }

        val queue = ArrayDeque<Point>()

        // Start at start and follow the trail to the first junction.
        queue.add(start)

        while(queue.isNotEmpty()) {
            var current = queue.removeFirst()
            println("" + current + ": " + map[current.y][current.x])
            val ns = neighboursOf(current)

            val isJunction = ns.size == 3
            if (isJunction) {
                println("*")
            }

            ns.filter { !visited.contains(it) }.forEach {
                visited.add(it)
                queue.add(it)
            }
        }
    }

    data class Point(val y: Int, val x: Int)

}