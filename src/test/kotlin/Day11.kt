import org.testng.Assert.assertEquals
import org.testng.annotations.Test
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
        // Read the map, then expand it, them sum the Manhattan distances between the galaxies

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
            val xExpansions = expandedCols.filter { it < g.x }.size
            // With a correction which needs to be explained
            val dy = (yExpansions * expansion) - (if (expansion == 1L) 0 else yExpansions)
            val dx = (xExpansions * expansion) - (if (expansion == 1L) 0 else xExpansions)
            Galaxy(g.y + dy, g.x + dx)
        }

        val distances = expandedGalaxies.indices.flatMap { m ->
            (m + 1..<expandedGalaxies.size).map { n ->
                manhattanDist(expandedGalaxies[m], expandedGalaxies[n])
            }
        }
        return distances.sum()
    }

}

fun manhattanDist(a: Galaxy, b: Galaxy): Long {
    return abs(b.y - a.y) + abs(b.x - a.x)
}

data class Galaxy(val y: Long, val x: Long)