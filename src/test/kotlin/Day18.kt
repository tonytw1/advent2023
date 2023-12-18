import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day18 : Helpers {

    @Test
    fun part1() {
        assertEquals(capacityOf("day18example.txt"), 62)
        assertEquals(capacityOf("day18.txt"), 46394)
    }

    private fun capacityOf(filename: String): Int {
        val directions = mapOf(
            'U' to Pair(-1, 0),
            'D' to Pair(1, 0),
            'R' to Pair(0, 1),
            'L' to Pair(0, -1)
        )

        // Build the wall
        var pos = Point(0, 0)
        val wall = mutableSetOf<Point>()
        stringsFromFile(filename).forEach { line ->
            val split = line.split(" ")
            val dir = directions[split[0][0]]!!
            val delta = split[1].toInt()
            (1..delta).forEach { _ ->
                wall.add(pos)
                pos = Point(pos.y + dir.first, pos.x + dir.second)
            }
        }

        // Ring fence the problem; allow space around the edge
        val minY = wall.map { it.y }.min() - 1
        val maxY = wall.map { it.y }.max() + 1
        val minX = wall.map { it.x }.min() - 1
        val maxX = wall.map { it.x }.max() + 1

        // Flood file from outside
        val outside = mutableSetOf<Point>()
        val queue = ArrayDeque<Point>()
        val seed = Point(minY, minX)
        queue.add(seed)

        while (queue.isNotEmpty()) {
            val p = queue.removeFirst()
            outside.add(p)

            val nearby = listOf(
                Point(p.y - 1, p.x),
                Point(p.y + 1, p.x),
                Point(p.y, p.x - 1),
                Point(p.y, p.x + 1)
            ).filter { p ->
                (p.y in minY..maxY) && (p.x in minX..maxX)
            }

            nearby.forEach {
                if (!wall.contains(it) && !outside.contains(it)) {
                    outside.add(it)
                    queue.add(it)
                }
            }

        }

        val area = ((maxY - minY) + 1) * ((maxX - minX) + 1)
        return area - outside.size
    }

    data class Point(val y: Int, val x: Int)

}