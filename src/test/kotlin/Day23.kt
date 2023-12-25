import org.testng.annotations.Test

class Day23 : Helpers {

    @Test
    fun part1() {
        // Parse the map to an array of chars; we can discard it once we have a graph
        val map = stringsFromFile("day23example.txt").withIndex().map { line ->
            line.value.toCharArray()
        }.toTypedArray()

        // Record the vertices we find
        val vertices = mutableSetOf<Point>()
        // Start and end are give
        val start = Point(0, 1)
        val end = Point(21, 22)
        vertices.add(start.copy(y = start.y + 1))
        vertices.add(end)

        // Also record visited cells
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

        val verticesToExploreFrom = ArrayDeque<Point>()
        println("A")
        // Queue of vertices to be explored from
        verticesToExploreFrom.add(start)

        while(verticesToExploreFrom.isNotEmpty()) {
            val start =  verticesToExploreFrom.removeFirst()
            var current = start
            var d = 1

            println("Starting from " + current + ": " + map[current.y][current.x])

            var done = false
            while (!done) {
                visited.add(current)
                val neighbours = neighboursOf(current)

                val isJunction = neighbours.size == 3 || vertices.contains(current)
                if (isJunction) {
                    println("VT: $start -> $current: $d")
                    vertices.add(current)

                    // Queue for unvisited branches
                    val next = neighbours.filter { !visited.contains(it) }
                    next.forEach { nextStart ->
                        verticesToExploreFrom.add(nextStart)
                    }
                    done = true
                } else {

                    // Expect 1 only next step
                    val next = neighbours.filter { !visited.contains(it) }
                    done = next.isEmpty()

                    if (!done) {
                        if (next.size != 1) {
                            throw RuntimeException("" + next.size)
                        }
                        current = next.first()
                        d += 1
                    }
                }
            }

        }
    }

    data class Point(val y: Int, val x: Int)

}