import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.math.BigDecimal
import kotlin.math.abs

class Day11 : Helpers {

    @Test
    fun part1() {
        assertEquals(sumOfExpandedDistances("day11example.txt", 1), 374)
        assertEquals(sumOfExpandedDistances("day11.txt", 1), 10173804)
    }


    @Test
    fun part2() {
        assertEquals(sumOfExpandedDistances("day11example.txt", 10), 1030)
        assertEquals(sumOfExpandedDistances("day11example.txt", 100), 8410)
        assertEquals(sumOfExpandedDistances("day11.txt", 1000000), 634324905172)
    }

    private fun sumOfExpandedDistances(filename: String, expansion: Long): Long {
        // Read the galaxy,
        // then expand it,
        // them sum the Manhattan distances between them

        val galaxy = stringsFromFile(filename).map {
            it.toCharArray()
        }.toTypedArray()

        // Knowing the expanded rows and columns upfront would be useful
        val expandedRows = galaxy.withIndex().filter {
            it.value.all { it == '.' }
        }.map { it.index }
        val expandedCols = (0..<galaxy[0].size).filter { c ->
            (galaxy.indices).all { r ->
                galaxy[r][c] == '.'
            }
        }

        // Scan the galaxy collecting the points
        val galaxies = mutableListOf<Galaxy>()
        for (r in 0..<galaxy[0].size) {
            for (c in galaxy.indices) {
                if (galaxy[r][c] == '#') {
                    galaxies.add(Galaxy(r.toLong(), c.toLong()))
                }
            }
        }

        // Expand
        val expandedGalaxies = galaxies.map { g ->
            // Coords plus number of expands before
            val yExpansions = expandedRows.filter { it < g.y }.size
            val xExpantions = expandedCols.filter { it < g.x }.size
            val dy = (yExpansions * expansion) - yExpansions
            val dx = (xExpantions * expansion) - xExpantions
            Galaxy(g.y + dy, g.x + dx)
        }

        fun manhattenDist(a: Galaxy, b: Galaxy): Long {
            return abs(b.y - a.y) + abs(b.x - a.x)
        }

        val dists = mutableListOf<Long>()
        for (m in expandedGalaxies.indices) {
            for (n in (m + 1)..<expandedGalaxies.size) {
                dists.add(manhattenDist(expandedGalaxies[m], expandedGalaxies[n]))
            }
        }

        println(dists)
        val r = dists.fold(BigDecimal(0)) { acc, it ->
            acc.add(BigDecimal(it))
        }
        println(r)
        return r.toLong()
    }
}

data class Galaxy(val y: Long, val x: Long)