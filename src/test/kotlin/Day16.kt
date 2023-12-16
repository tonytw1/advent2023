import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day16 : Helpers {

    @Test
    fun part1() {
        assertEquals(countEnergised(parseMap("day16example.txt")), 46)
        assertEquals(countEnergised(parseMap("day16.txt")), 7067)
    }

    fun countEnergised(map: Set<Point>): Int {
        val maxX = map.map { it.x }.max()
        val maxY = map.map { it.y }.max()

        // Record visit squares and the direction to visitation; this lets us know when we are entering a loop
        val visited = mutableSetOf<Location>()

        // Queue paths to explore
        val queue = ArrayDeque<Location>()
        queue.add(Location(0, 0, Pair(0, 1)))

        while (queue.isNotEmpty()) {
            var l = queue.removeFirst()

            // Loop until we leave the map or encounter a previously explored trace
            while ((l.x in (0..maxX)) && (l.y in (0..maxY)) && !visited.contains(l)) {
                var y = l.y
                var x = l.x
                var dir = l.dir

                val currentPoint =
                    map.find { it.y == y && it.x == x }!! // TODO Not a great data structure for this but fear of part2

                when (currentPoint.c) {
                    '/' -> dir = Pair(-dir.second, -dir.first)
                    '\\' -> dir = Pair(dir.second, dir.first)
                    '|' -> {
                        if (dir.second != 0) {
                            // One turns the other way and is queued
                            queue.add(l.copy(dir = Pair(dir.second, 0)))
                            // One turns and continues
                            dir = Pair(-dir.second, 0)
                        }
                    }

                    '-' -> {
                        if (dir.first != 0) {
                            // One turns the other way and is queued
                            queue.add(l.copy(dir = Pair(0, dir.first)))
                            // One turns  and continues
                            dir = Pair(0, -dir.first)
                        }
                    }
                }

                // Record visit then move
                visited.add(l)

                y += dir.first
                x += dir.second
                l = Location(y, x, dir)
            }
        }
        // Count the visited points; discarding direction information
        return visited.map { Pair(it.y, it.x) }.toSet().size
    }

    private fun parseMap(filename: String): Set<Point> {
        return stringsFromFile(filename).withIndex().flatMap { line ->
            line.value.toCharArray().withIndex().map { c ->
                Point(line.index, c.index, c.value)
            }
        }.toSet()
    }

    data class Location(val y: Int, val x: Int, val dir: Pair<Int, Int>)
    data class Point(val y: Int, val x: Int, val c: Char)
    data class Visit(val point: Point, val dir: Pair<Int, Int>)
}