import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.stream.Collectors

class Day22 : Helpers {

    @Test
    fun part1() {
        assertEquals(findNonLoadBearing("day22example.txt"), 5)
        assertEquals(findNonLoadBearing("day22.txt"), 505)
    }

    private fun findNonLoadBearing(filename: String): Int {
        // Load the shapes
        val shapes = parseShapes(filename)

        // Let the shapes settle
        val settled = settle(shapes)

        // Find the load bearing shapes.
        // Remove each one in turn; and settle; look for stacks which move
        settled.withIndex().filter { shape ->
            val without = settled.toMutableList()
            without.remove(shape.value)
            val settled = settle(without)
            without == settled
        }.size

        val result = settled.parallelStream().filter { shape ->
            val without = settled.toMutableList()
            without.remove(shape)
            val settled = settle(without)
            without == settled
        }.collect(Collectors.toList()).size
        return result
    }

    private fun settle(shapes: List<Set<Point>>): List<Set<Point>> {
        // While any shape has clear air under it, move it down
        var falling = shapes
        var done = false

        while (!done) {
            val occupied = falling.flatMap { it }.toSet()
            val canFall = falling.parallelStream().filter { shape ->
                hasSpaceBelow(occupied, shape)
            }.collect(Collectors.toList())
            done = canFall.isEmpty()

            val fixed = falling.toMutableList()
            fixed.removeAll(canFall)

            val dropped = canFall.map { shape ->
                val dropped = shape.map { p ->
                    p.copy(z = p.z - 1) // Need to be more agressive about dropping as far as possible in 1 go
                }.toSet()
                dropped
            }
            falling = dropped + fixed
        }
        return falling
    }

    private fun hasSpaceBelow(
        occupied: Set<Point>,
        shape: Set<Point>
    ): Boolean {
        // Footprint of this shape
        val footprint = shape.map { p ->
            Pair(p.x, p.y)
        }.toSet()
        return footprint.all { xy ->
            val lowestPoint = shape.filter { it.x == xy.first && it.y == xy.second }.minBy { it.z }
            val belowLowestPoint = lowestPoint.copy(z = lowestPoint.z - 1)
            belowLowestPoint.z > 0 && !occupied.contains(belowLowestPoint)
        }
    }

    private fun parseShapes(filename: String): List<Set<Point>> {
        val map = stringsFromFile(filename).map { line ->
            val fromAndTo = line.split("~").map { p ->
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
            shape
        }
        return map
    }

    data class Point(val x: Int, val y: Int, val z: Int)
}