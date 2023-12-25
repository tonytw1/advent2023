import org.testng.annotations.Test

class Day23 : Helpers {

    @Test
    fun part1() {
        // Parse the map to an array of chars; we can discard it once we have a graph
        val paths = extractPaths("day23example.txt")
        println(paths)
    }

    private fun extractPaths(filename: String): MutableMap<Pair<Point, Point>, Int> {
        val map = stringsFromFile(filename).withIndex().map { line ->
            line.value.toCharArray()
        }.toTypedArray()

        val paths = mutableMapOf<Pair<Point, Point>, Int>()

        // Record the vertices we find
        val vertices = mutableSetOf<Point>()
        // Start and end are give
        val start =  Point(0, map[0].indexOf('.'))
        val end = Point(map.lastIndex, map.last().indexOf('.'))
        
        println("START: " + start)
        println("END: " + end)

        vertices.add(start)
        vertices.add(end)

        // Also record visited cells
        val visited = mutableSetOf<Point>()

        fun neighboursOf(p: Point): Set<Point> {
            val neighbours = mutableListOf<Point>()
            val minY = 0
            val maxY = map.size - 1
            val minX = 0
            val maxX = map[0].size - 1
            for (y in listOf(p.y - 1, minY).max()..listOf(p.y + 1, maxY).min()) {
                val n = Point(y, p.x)
                neighbours.add(n)
            }
            for (x in listOf(p.x - 1, minX).max()..listOf(p.x + 1, maxX).min()) {
                val n = Point(p.y, x)
                neighbours.add(n)
            }
            return neighbours.filter { it != p && map[it.y][it.x] != '#' }.toSet()
        }

        val verticesToExploreFrom = ArrayDeque<Pair<Point, Point>>()
        // Queue of vertices to be explored from
        verticesToExploreFrom.add(Pair(start, start.copy(y = start.y + 1)))


        while (verticesToExploreFrom.isNotEmpty()) {
            val start = verticesToExploreFrom.removeFirst()

            var previous = start.first
            var current = start.second
            println("Starting from " + previous + ": " + map[previous.y][previous.x])

            visited.add(start.first)
            var d = 1

            // Track direction so that one way paths can be correctly ignored
            fun dirFrom(a: Point, b: Point): Pair<Int, Int> {
                val dy = b.y - a.y
                val dx = b.x - a.x
                return Pair(dy, dx)
            }

            var done = false
            while (!done) {
                visited.add(current)


                val dir = dirFrom(previous, current)
                val c = map[current.y][current.x]
                val wrongWay = if (c != '.') {
                    // Need to check for 1 way violations
                    when (c) {
                        '>' -> dir == Pair(0, -1)
                        '<' -> dir == Pair(0, 1)
                        '^' -> dir == Pair(1, 0)
                        'v' -> dir == Pair(-1, 0)
                        else -> {
                            throw RuntimeException("" + c)
                        }
                    }
                } else {
                    false
                }

                if (wrongWay) {
                    println("WW: ${start.first} -> $current: !")
                    done = true
                    
                    
                    
                    
                } else {
                    val neighbours = neighboursOf(current)
                    val isJunction = neighbours.size >= 3 || vertices.contains(current)
                    if (isJunction) {
                        println("VT: ${start.first} -> $current: $d")
                        vertices.add(current)

                        paths[(Pair(start.first, current))] = d

                        // Queue for unvisited branches
                        val next = neighbours.filter { !visited.contains(it) }
                        next.forEach { nextStart ->
                            verticesToExploreFrom.add(Pair(current, nextStart))
                        }
                        done = true

                    } else {

                        // Expect 1 only next step
                        val next = neighbours.filter { !visited.contains(it) }
                        done = next.isEmpty()
                        if (next.isEmpty()) {
                            println("DE: ${start.first} -> $current: *")
                        }

                        if (!done) {
                            if (next.size != 1) {
                                throw RuntimeException("$current " + next.size)
                            }
                            previous = current
                            current = next.first()
                            d += 1
                        }
                    }
                }
            }
        }
        return paths
    }

    data class Point(val y: Int, val x: Int)

}