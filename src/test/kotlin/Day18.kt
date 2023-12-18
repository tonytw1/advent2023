import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day18 : Helpers {

    @Test
    fun part1() {
        assertEquals(capacityUsingSquares("day18example.txt"), 62)
        //assertEquals(capacityUsingSquares("day18.txt"), 46394)
    }

    private fun capacityUsingSquares(filename: String): Int {
        // Trace out the wall recording the vertices and the vertical line segments we encounter
        val vertices = mutableSetOf<Point>()
        val verticalLines = mutableListOf<Line>()

        fun hexToInt(hex: String): Int {
            hex.reversed().toCharArray().forEach { c ->
                val i = 'f' - c
                println("$c - $i")
            }
            return 0
        }

        var pos = Point(0, 0)
        var currentDir: Pair<Int, Int>? = null

        val lines = stringsFromFile(filename)
        (lines + lines.first()).forEach { line ->
            val split = line.split(" ")
            val dir = directions[split[0][0]]!!
            val delta =split[1].toInt()

            val newPos = Point(pos.y + (dir.first * delta), pos.x + (dir.second * delta))
            val ys = listOf(pos.y, newPos.y).sorted()
            if (ys.first() != ys.last()) {
                verticalLines.add(Line(pos.x, ys.first(), ys.last()))
            }
            pos = newPos
            if (dir != currentDir) {
                vertices.add(pos)
            }
            currentDir = dir
        }

        // Extract all the grid x and y lines though the vertices
        val ys = vertices.map { it.y }.toSet().toList().sorted()
        val xs = vertices.map { it.x }.toSet().toList().sorted()
        // Render squares to fill the gaps in the grid of vertices
        var oy = 0
        return (0..ys.size - 2).flatMap { i ->
            println("------------")
            var overlapForFirstSegment = 1
            val x = (0..xs.size - 2).map { j ->
                // If we ray traced from the left then we can tell if a square is internal by how many vertical lines we have crossed
                val top = ys[i]
                val bottom = ys[i + 1]

                val linesToLeftOf = verticalLines.filter { it ->
                    it.x <= xs[j] && it.y1 <= top && it.y2 >= bottom
                }

                val isInternal = (linesToLeftOf.size % 2) > 0
                if (isInternal) {
                    val area = ((bottom - top) + oy) * ((xs[i + 1] - xs[i]) + overlapForFirstSegment)
                    overlapForFirstSegment = 0
                    println(area)
                    area
                } else {
                    overlapForFirstSegment = 1

                    0
                }
            }
            oy = 1
            x
        }.sum()
    }

    data class Point(val y: Int, val x: Int)

    val directions = mapOf(
        'U' to Pair(-1, 0),
        'D' to Pair(1, 0),
        'R' to Pair(0, 1),
        'L' to Pair(0, -1)
    )

    data class Line(val x: Int, val y1: Int, val y2: Int)
}