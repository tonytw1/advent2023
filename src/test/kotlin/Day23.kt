import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day23 : Helpers {

    @Test
    fun part1() {
        assertEquals(findLongestPathByDFS(extractPaths("day23example.txt")), 94)
        assertEquals(findLongestPathByDFS(extractPaths("day23.txt")), 2222)
    }

    private fun findLongestPathByDFS(maze: Maze): Int {
        var best = 0

        // Naive DFS from start to end
        fun visit(p: Point, d: Int, path: List<Point>) {
            val updatedPath = path.toMutableList()
            updatedPath.add(p)
            if (p == maze.end) {
                if (d > best) {
                    best = d
                }
            }
            val downStreams = maze.paths.filter {
                it.key.first == p
            }.filter { !updatedPath.contains(it.key.second) }

            downStreams.entries.forEach { ds ->
                val to = ds.key.second
                visit(to, d + ds.value, updatedPath)
            }
        }

        visit(maze.start, 0, emptyList())
        return best
    }

    private fun extractPaths(filename: String): Maze {
        val map = stringsFromFile(filename).withIndex().map { line ->
            line.value.toCharArray()
        }.toTypedArray()

        val paths = mutableMapOf<Pair<Point, Point>, Int>()

        // Record the vertices we find
        val vertices = mutableSetOf<Point>()
        // Start and end are given
        val start =  Point(0, map[0].indexOf('.'))
        val end = Point(map.lastIndex, map.last().indexOf('.'))
        vertices.add(start)
        vertices.add(end)

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
            // Explore from this vertice to discover the next ones
            val startOfPath = verticesToExploreFrom.removeFirst()
            val visited = mutableSetOf<Point>()

            var previous = startOfPath.first
            var current = startOfPath.second

            visited.add(startOfPath.first)
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
                    // Drop path
                    done = true

                } else {
                    val neighbours = neighboursOf(current)
                    val isJunction = neighbours.size >= 3 || vertices.contains(current)
                    if (isJunction) {
                        vertices.add(current)
                        paths[(Pair(startOfPath.first, current))] = d

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
                            // Dead end
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
        return Maze(start, end, paths)
    }

    data class Maze( val start: Point, val end: Point, val paths: Map<Pair<Point, Point>, Int>)

    data class Point(val y: Int, val x: Int)

}