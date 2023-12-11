import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import kotlin.math.abs

class Day11 : Helpers {

    @Test
    fun part1() {
        assertEquals(sumOfExpandedDistances("day11example.txt"), 374)
        assertEquals(sumOfExpandedDistances("day11.txt"), 10173804)
    }

    private fun sumOfExpandedDistances(filename: String): Int {
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
                    galaxies.add(Galaxy(r, c))
                }
            }
        }

        // Expand
        val expandedGalaxies = galaxies.map { g ->
            // Coords plus number of expands before
            val y = g.y + expandedRows.filter { it < g.y }.size
            val x = g.x + expandedCols.filter { it < g.x }.size
            Galaxy(y, x)
        }

        fun manhattenDist(a: Galaxy, b: Galaxy): Int {
            return abs(b.y - a.y) + abs(b.x - a.x)
        }

        val dists = mutableListOf<Int>()
        for (m in 0..expandedGalaxies.size - 1) {
            for (n in (m + 1)..<expandedGalaxies.size) {
                dists.add(manhattenDist(expandedGalaxies[m], expandedGalaxies[n]))
            }
        }
        return dists.sum()
    }
}

data class Galaxy(val y: Int, val x: Int)