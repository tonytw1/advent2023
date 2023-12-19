import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day18 : Helpers {

    @Test
    fun part1() {
        fun parseSteps(filename: String): List<Step> {
            return stringsFromFile(filename).map { line ->
                val split = line.split(" ")
                val dir = directions[split[0][0]]!!
                val delta = split[1].toLong()
                Step(dir, delta)
            }
        }
        assertEquals(capacityUsingSquares(parseSteps("day18example.txt")), 62)
        assertEquals(capacityUsingSquares(parseSteps("day18.txt")), 46394)
    }

    @Test
    fun part2() {
        fun parseSteps(filename: String): List<Step> {
            return stringsFromFile(filename).map { line ->
                val split = line.split(" ")
                val hex = split[2]
                val delta = hexToInt(hex.drop(2).dropLast(2))
                val dirChar = when (hex.dropLast(1).last()) {
                    '0' -> 'R'
                    '1' -> 'D'
                    '2' -> 'L'
                    '3' -> 'U'
                    else -> {
                        throw RuntimeException()
                    }
                }
                val dir = directions[dirChar]!!
                Step(dir, delta)
            }
        }
        assertEquals(capacityUsingSquares(parseSteps("day18example.txt")), 952408144115)
        assertEquals(capacityUsingSquares(parseSteps("day18.txt")), 201398068194715)
    }

    @Test
    fun testHexToLong() {
        assertEquals(hexToInt("70c71"), 461937)
    }

    private fun capacityUsingSquares(steps: List<Step>): Long {
        // Trace out the wall recording the sequence of corners
        // Corners are easy but we really want the vertices at the corners of the squares
        // If the loop is clockwise the external vertice can be inferred from tangents

        val corners = mutableListOf<Point>()
        val vertices = mutableListOf<Point>()

        var pos = Point(0L, 0L)
        var currentDir: Pair<Long, Long>? = null
        var tangent: Pair<Long, Long>? = null

        (steps + steps.first()).forEach { step ->   // loop onto tail to with known direction
            val dir = step.dir
            val delta = step.delta
            val newPos = Point(pos.y + (dir.first * delta), pos.x + (dir.second * delta))
            val newTangent = Pair(-dir.second, dir.first)
            if (currentDir != null && dir != currentDir) {
                corners.add(pos)
                // Assume clock wise; use tangents before and after turn to throw a vertice in the correct direction
                val dy = Math.max(tangent!!.first + newTangent.first, 0)
                val dx = Math.max(tangent!!.second + newTangent.second, 0)
                val vy = pos.y + dy
                val vx = pos.x + dx
                vertices.add(Point(vy, vx))
            }
            pos = newPos
            currentDir = dir
            tangent = newTangent
        }

        // Extract vertical line segments
        val verticalLines = mutableListOf<Line>()
        var current = vertices.first()
        vertices.drop(1).forEach { v ->
            if (v.x == current.x) {
                val y1 = listOf(current.y, v.y).sorted()
                verticalLines.add(Line(v.x, y1.first(), y1.last()))
            }
            current = v
        }

        // Extract all the grid x and y lines though the vertices
        val ys = vertices.map { it.y }.toSet().toList().sorted()
        val xs = vertices.map { it.x }.toSet().toList().sorted()

        // Render squares to fill the gaps in the grid of vertices
        return (0..ys.size - 2).flatMap { i ->
            (0..xs.size - 2).map { j ->
                val top = ys[i]
                val bottom = ys[i + 1]
                val right = xs[j]
                val left = xs[j + 1]

                val area = ((bottom - top)) * ((left - right))
                // If we ray traced from the left then we can tell if a square is internal by how many vertical lines we have crossed
                val linesToLeftOf = verticalLines.filter {
                    it.x <= right && it.y1 <= top && it.y2 >= bottom
                }
                area * (linesToLeftOf.size % 2)
            }
        }.sum()
    }

    fun hexToInt(hex: String): Long {
        return java.lang.Long.parseLong(hex, 16)
    }

    val directions: Map<Char, Pair<Long, Long>> = mapOf(
        'U' to Pair(-1, 0),
        'R' to Pair(0, 1),
        'D' to Pair(1, 0),
        'L' to Pair(0, -1)
    )

    data class Point(val y: Long, val x: Long)
    data class Step(val dir: Pair<Long, Long>, val delta: Long)
    data class Line(val x: Long, val y1: Long, val y2: Long)
}