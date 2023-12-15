import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day13 : Helpers {

    @Test
    fun part1() {
        fun summaries(maps: List<Set<Point>>): Int {
            return maps.sumOf { map ->
                summarise(map)
            }
        }
        assertEquals(summaries(parseMaps("day13example.txt")), 405)
        assertEquals(summaries(parseMaps("day13.txt")), 37113)
    }

    private fun summarise(map: Set<Point>): Int {
        // Reduce row and columns to lists of checksums and look for mirrors in those
        val c = findMirror(colChecksums(map))
        val r = findMirror(rowChecksums(map))
        return c + (100 * r)
    }

    private fun findMirror(checkSums: List<Int>): Int{
        val width = checkSums.size
        (0..<width).forEach { i ->
            var good = true
            var d = 0
            var l = i
            var r = i + 1
            while (l >= 0 && r < width && good) {
                val vl = checkSums[l]
                val vr = checkSums[r]
                good = vl == vr
                d += 1
                l = i - d
                r = i + 1 + d
            }
            if (good && d > 0) {
                return i + 1
            }
        }
        return 0
    }

    // TODO generalise this reduction?
    private fun colChecksums(map: Set<Point>): List<Int> {
        var width = map.map { it.x }.max()
        var height = map.map { it.y }.max()
        return (1..width).map { x ->
            (1..height).sumOf { y ->
                if (map.contains(Point(y, x))) {
                    Math.pow(2.0, y.toDouble()).toInt()
                } else {
                    0
                }
            }
        }
    }
    private fun rowChecksums(map: Set<Point>): List<Int> {
        var width = map.map { it.x }.max()
        var height = map.map { it.y }.max()
        return (1..height).map { y ->
            (1..width).sumOf { x ->
                if (map.contains(Point(y, x))) {
                    Math.pow(2.0, x.toDouble()).toInt()
                } else {
                    0
                }
            }
        }
    }

    private fun parseMaps(filename: String): MutableList<Set<Point>> {
        val maps = mutableListOf<Set<Point>>()
        var map = mutableSetOf<Point>()
        var y = 1
        stringsFromFile(filename).forEach { line ->
            if (line.isEmpty()) {
                maps.add(map.toSet())
                map = mutableSetOf()
                y = 0
            }
            for (x in line.indices) {
                val c = line[x]
                if (c == '#') {
                    map.add(Point(y, x + 1))
                }
            }
            y +=1
        }
        maps.add(map.toSet())
        return maps
    }

    data class Point(val y: Int, val x: Int)
}

