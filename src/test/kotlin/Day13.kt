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

    @Test
    fun part2() {
        fun diffs(maps: List<Set<Point>>): Int {
            return maps.map { map ->
                findDiff(map)
            }.sum()
        }
        assertEquals(diffs(parseMaps("day13example.txt")), 400)
        assertEquals(diffs(parseMaps("day13.txt")), 30449)
    }

    private fun findDiff(map: Set<Point>): Int {
        // Record where the mirrors where in part 1; every 1 had exactly 1 mirror
        val co = findMirrors(colChecksums(map))
        val ro = findMirrors(rowChecksums(map))

        // For every point... look for a flip which projects to a map with a new edge
        val width = map.map { it.x }.max()
        val height = map.map { it.y }.max()
        (1..height).forEach { y ->
            (1..width).forEach { x ->
                // Render the new map
                val flipped = map.toMutableSet()
                val pointToFlip = Point(y, x)
                val contains = map.contains(pointToFlip)
                if (contains) {
                    flipped.remove(pointToFlip)
                } else {
                    flipped.add(pointToFlip)
                }

                // Find the mirrors in this new map; there may be more than 1 now!
                val c = findMirrors(colChecksums(flipped)).toMutableList()
                val r = findMirrors(rowChecksums(flipped)).toMutableList()
                // Remove the part1 mirrors so only new olds are left
                // There will be 1 new mirror left
                c.removeAll { co.contains(it) }
                r.removeAll { ro.contains(it) }
                if (c.size + r.size == 1) {
                    // Summarise the new edges
                    val ra = r.getOrElse(0) { 0 }
                    val ca = c.getOrElse(0) { 0 }
                    return summaryScoreFor(ca, ra)
                }
            }
        }
        throw RuntimeException()
    }

    private fun findMirror(checkSums: List<Int>): Int {
        val i = findMirrors(checkSums)
        if (i.isNotEmpty()) {
            return i.first()!!
        } else {
            return 0
        }
    }

    private fun summarise(map: Set<Point>): Int {
        // Reduce row and columns to lists of checksums and look for mirrors in those
        return summaryScoreFor(findMirror(colChecksums(map)), findMirror(rowChecksums(map)))
    }

    private fun summaryScoreFor(columns: Int, rows: Int) = columns + (100 * rows)

    private fun findMirrors(checkSums: List<Int>): List<Int> {
        val width = checkSums.size
        val mirrors = mutableListOf<Int>()
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
                mirrors.add(i + 1)
            }
        }
        return mirrors
    }

    // TODO generalise this reduction?
    private fun colChecksums(map: Set<Point>): List<Int> {
        val width = map.map { it.x }.max()
        val height = map.map { it.y }.max()
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
        val width = map.map { it.x }.max()
        val height = map.map { it.y }.max()
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
            y += 1
        }
        maps.add(map.toSet())
        return maps
    }

    data class Point(val y: Int, val x: Int)
}