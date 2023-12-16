import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.stream.Collectors

class Day16 : Helpers {

    @Test
    fun part1() {
        val start = Location(0, 0, Pair(0, 1))
        assertEquals(countEnergised(parseMap("day16example.txt"), start), 46)
        assertEquals(countEnergised(parseMap("day16.txt"), start), 7067)
    }

    @Test
    fun part2() {
        assertEquals(bestPossibleEnergised("day16example.txt"), 51)
        assertEquals(bestPossibleEnergised("day16.txt"), 7324)
    }

    private fun bestPossibleEnergised(filename: String): Int {
        val map = parseMap(filename)

        // Generate possible entry points
        val maxX = map.maxX()
        val maxY = map.maxY()

        val verticalEntries = (0..maxX).flatMap { x ->
            listOf(
                Location(0, x, Pair(1, 0)),
                Location(maxY, x, Pair(-1, 0))
            )
        }
        val horizontalEntries = (0..maxY).flatMap { y ->
            listOf(
                Location(y, 0, Pair(0, 1)),
                Location(y, maxX, Pair(0, -1))
            )
        }

        val results = (verticalEntries + horizontalEntries).parallelStream().map { start ->
            countEnergised(map, start)
        }.collect(Collectors.toList())
        return results.max()
    }

    fun countEnergised(map: Map, start: Location): Int {
        val maxX = map.maxX()
        val maxY = map.maxY()

        // Record visit squares and the direction to visitation; this lets us know when we are entering a loop
        val visited = mutableSetOf<Location>()

        // Queue paths to explore
        val queue = ArrayDeque<Location>()
        queue.add(start)

        while (queue.isNotEmpty()) {
            var l = queue.removeFirst()

            // Loop until we leave the map or encounter a previously explored trace
            while ((l.x in (0..maxX)) && (l.y in (0..maxY)) && !visited.contains(l)) {
                var y = l.y
                var x = l.x
                var dir = l.dir

                when (map.charAt(y, x)) {
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

    private fun parseMap(filename: String): Map {
        return Map(stringsFromFile(filename).withIndex().flatMap { line ->
            line.value.toCharArray().withIndex().map { c ->
                Point(line.index, c.index, c.value)
            }
        }.toSet())
    }

    data class Location(val y: Int, val x: Int, val dir: Pair<Int, Int>)
    data class Point(val y: Int, val x: Int, val c: Char)

    class Map(val points: Set<Point>) {
        fun maxX(): Int {
            return points.map { it.x }.max()
        }
        fun maxY(): Int {
            return points.map { it.y }.max()
        }
        fun charAt(y: Int, x: Int): Char {
            return points.find { it.y == y && it.x == x }!!.c // TODO Not a great data structure for this but fear of part2
        }
    }
}