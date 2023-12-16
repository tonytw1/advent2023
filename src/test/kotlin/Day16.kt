import org.testng.Assert.assertEquals
import org.testng.annotations.Test

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
        val verticalEntries = (0..map.maxX()).flatMap { x ->
            listOf(
                Location(0, x, Pair(1, 0)),
                Location(map.maxY(), x, Pair(-1, 0))
            )
        }
        val horizontalEntries = (0..map.maxY()).flatMap { y ->
            listOf(
                Location(y, 0, Pair(0, 1)),
                Location(y, map.maxX(), Pair(0, -1))
            )
        }

        val results = (verticalEntries + horizontalEntries).map { start ->
            countEnergised(map, start)
        }
        return results.max()
    }

    private fun countEnergised(map: Map, start: Location): Int {
        // Record visit squares and the direction to visitation; this lets us know when we are entering a loop
        val visited = mutableSetOf<Location>()

        // Queue paths to explore
        val queue = ArrayDeque<Location>()
        queue.add(start)

        while (queue.isNotEmpty()) {
            var l = queue.removeFirst()

            // Loop until we leave the map or encounter a previously explored trace
            while ((l.x in (0..map.maxX())) && (l.y in (0..map.maxY())) && !visited.contains(l)) {
                var y = l.y
                var x = l.x
                var dir = l.dir

                when (map.charAt(y, x)) {
                    '/' -> dir = Pair(-dir.second, -dir.first)
                    '\\' -> dir = Pair(dir.second, dir.first)
                    '|' -> {
                        if (dir.second != 0) {
                            // One turns and is queued
                            queue.add(l.copy(dir = Pair(dir.second, 0)))
                            // One turns the other way and continues
                            dir = Pair(-dir.second, 0)
                        }
                    }

                    '-' -> {
                        if (dir.first != 0) {
                            // One turns and is queued
                            queue.add(l.copy(dir = Pair(0, dir.first)))
                            // One turns the other way and continues
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

    class Map(private var points: Set<Point>) {
        private val maxX: Int = points.map { it.x }.max()
        private val maxY: Int = points.map { it.y }.max()
        private val ps: Array<Array<Char>> = Array(this.maxY + 1) {
            Array(this.maxX + 1) {
                '.'
            }
        }

        init {
            points.forEach { it ->
                ps[it.y][it.x] = it.c
            }
        }

        fun maxX(): Int {
            return maxX
        }

        fun maxY(): Int {
            return maxY
        }

        fun charAt(y: Int, x: Int): Char {
            return ps[y][x]
        }
    }
}