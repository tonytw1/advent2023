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

        // Find a block which is inside the wall; no hint about of the loop is clock wise or on though
        // Is the pool continuous or are there pitch points?
        // Probably best to fill all regions and then decide if there are internal or external after the fact.

        // Ring fence the problem; allow space around the edge
        val minY = wall.map { it.y }.min() - 1
        val maxY = wall.map { it.y }.max() + 1
        val minX = wall.map { it.x }.min() - 1
        val maxX = wall.map { it.x }.max() + 1

        // Determine all the available points
        val unfilled = mutableSetOf<Point>()
        (minY..maxY).forEach { y ->
            (minX..maxX).forEach { x ->
                val p = Point(y, x)
                if (!wall.contains(p)) {
                    unfilled.add(p)
                }
            }
        }

        // Take an unfilled point and flood fill from it.
        // Repeat until all points are filled.
        val regions = mutableSetOf<Set<Point>>()

        while (unfilled.isNotEmpty()) {
            val region = mutableSetOf<Point>()
            val queue = ArrayDeque<Point>()

            val seed = unfilled.first()
            queue.add(seed)

            while (queue.isNotEmpty()) {
                val p = queue.removeFirst()

                region.add(p)
                unfilled.remove(p)

                val nearby = listOf(
                    Point(p.y - 1, p.x),
                    Point(p.y + 1, p.x),
                    Point(p.y, p.x - 1),
                    Point(p.y, p.x + 1)
                )

                nearby.forEach {
                    if (unfilled.contains(it)) {
                        queue.add(it)
                        unfilled.remove(it)
                    }
                }
                // Queue all of out unfilled neighbours
                queue.addAll(
                    nearby.filter {
                        unfilled.contains(it)
                    })
            }
            regions.add(region)
        }

        // Filter the regions to find internal regions; external regions touch the border of the game
        val internalRegions = regions.filter { region: Set<Point> ->
            val none = region.none { p: Point ->
                listOf(minY, maxY).contains(p.y) || listOf(minX, maxX).contains(p.x)
            }
            none
        }

        val enclosed = internalRegions.sumOf { it.size }
        return enclosed + wall.size
    }

    data class Point(val y: Int, val x: Int)

}