import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day21 : Helpers {

    @Test
    fun part1() {
        assertEquals(walk(parseGarden("day21example.txt"), 6), 16)
        assertEquals(walk(parseGarden("day21.txt"), 64), 3503)
    }

    private fun walk(garden: Garden, range: Int): Int {
        fun neighboursOf(p: Point): Set<Point> {
            val neighbours = mutableListOf<Point>()
            val maxWidth = garden.width - 1
            val maxHeight = garden.height - 1
            for (y in listOf(p.y - 1, 0).max()..listOf(p.y + 1, maxHeight).min()) {
                val n = Point(y, p.x)
                if (!garden.rocks.contains(n)) {
                    neighbours.add(n)
                }
            }
            for (x in listOf(p.x - 1, 0).max()..listOf(p.x + 1, maxWidth).min()) {
                val n = Point(p.y, x)
                if (!garden.rocks.contains(n)) {
                    neighbours.add(n)
                }
            }
            return neighbours.filter { it != p }.toSet()
        }

        val endPoints = mutableSetOf<Point>()

        // There is alot of revisting here; so remember paths we have already exhausted
        val cache = mutableSetOf<Pair<Point, Int>>()

        // DFS not BFS because we are allowed to back track!
        fun visit(p: Point, rangeRemaining: Int) {
            val cacheKey = Pair(p, rangeRemaining)
            if (cache.contains(cacheKey)) {
                return
            }
            cache.add(cacheKey)

            val ns = neighboursOf(p)
            if (rangeRemaining - 1 == 0) {
                endPoints.addAll(ns)
            } else {
                ns.forEach {
                    visit(it, rangeRemaining - 1)
                }
            }
        }

        visit(garden.start, range)
        return endPoints.size
    }

    private fun parseGarden(filename: String): Garden {
        // Need to know the bounds of the garden and the location of the rocks and the starting point
        val lines: List<String> = stringsFromFile(filename)
        val height = lines.size
        val width = lines[0].length

        val rocks = lines.withIndex().flatMap { line ->
            line.value.toCharArray().withIndex().filter { it.value == '#' }.map { c ->
                Point(line.index, c.index)
            }
        }.toSet()

        val start = lines.withIndex().flatMap { line ->
            line.value.toCharArray().withIndex().filter { it.value == 'S' }.map { c ->
                Point(line.index, c.index)
            }
        }.first()

        return Garden(width = width, height = height, rocks = rocks, start = start)
    }

    data class Point(val y: Int, val x: Int)
    data class Garden(val width: Int, val height: Int, val rocks: Set<Point>, val start: Point)


}