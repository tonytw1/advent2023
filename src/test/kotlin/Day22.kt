import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.stream.Collectors

class Day22 : Helpers {

    @Test
    fun part1() {
        assertEquals(findNonLoadBearing("day22example.txt").first, 5)
        assertEquals(findNonLoadBearing("day22.txt").first, 505)
    }

    @Test
    fun part2() {
        assertEquals(findNonLoadBearing("day22example.txt").second, 7)
        assertEquals(findNonLoadBearing("day22.txt").second, 71002)
    }

    private fun findNonLoadBearing(filename: String): Pair<Int, Int> {
        // Load the shapes
        val shapes = parseShapes(filename)

        // Let the shapes settle
        val settled = settle(shapes).first

        // Find the load bearing shapes.
        // Remove each one in turn; and settle; look for stacks which move
        settled.withIndex().filter { shape ->
            val without = settled.toMutableList()
            without.remove(shape.value)
            val settled = settle(without)
            without == settled
        }.size

        val results = settled.parallelStream().map { shape ->
            val without = settled.toMutableList()
            without.remove(shape)
            settle(without)
        }.collect(Collectors.toList())

        val a = results.filter { it.second == 0 }.size
        val b = results.sumOf { it.second }
        return Pair(a, b)
    }

    private fun settle(shapes: List<Shape>): Pair<List<Shape>, Int> {
        val moved = mutableSetOf<Int>()

        // While any shape has clear air under it, move it down
        var falling = shapes
        var done = false

        while (!done) {
            val occupied = falling.flatMap { it.points }.toSet()
            val canFall = falling.parallelStream().filter { shape ->
                hasSpaceBelow(occupied, shape)
            }.collect(Collectors.toList())
            done = canFall.isEmpty()

            val fixed = falling.toMutableList()
            fixed.removeAll(canFall)

            val dropped = canFall.map { shape ->
                moved.add(shape.id)
                val dropped = shape.points.map { p ->
                    p.copy(z = p.z - 1) // Need to be more agressive about dropping as far as possible in 1 go
                }.toSet()
                shape.copy(points = dropped)
            }
            falling = dropped + fixed
        }
        return Pair(falling, moved.size)
    }

    private fun hasSpaceBelow(
        occupied: Set<Point>,
        shape: Shape
    ): Boolean {
        // Footprint of this shape
        val footprint = shape.points.map { p ->
            Pair(p.x, p.y)
        }.toSet()
        return footprint.all { xy ->
            val lowestPoint = shape.points.filter { it.x == xy.first && it.y == xy.second }.minBy { it.z }
            val belowLowestPoint = lowestPoint.copy(z = lowestPoint.z - 1)
            belowLowestPoint.z > 0 && !occupied.contains(belowLowestPoint)
        }
    }

    private fun parseShapes(filename: String): List<Shape> {
        val map = stringsFromFile(filename).withIndex().map { line ->
            val fromAndTo = line.value.split("~").map { p ->
                val coords = p.split(",").map { it.toInt() }
                Point(coords[0], coords[1], coords[2])
            }
            val from = fromAndTo.first()
            val to = fromAndTo.last()
            val shape = (from.x..to.x).flatMap { x ->
                (from.y..to.y).flatMap { y ->
                    (from.z..to.z).map { z ->
                        Point(x, y, z)
                    }
                }
            }.toSet()
            Shape(line.index, shape)
        }
        return map
    }

    data class Shape(val id: Int, val points: Set<Point>)
    data class Point(val x: Int, val y: Int, val z: Int)
}