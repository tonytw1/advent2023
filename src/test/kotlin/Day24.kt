import org.testng.Assert.*
import org.testng.annotations.Test

class Day24 : Helpers {

    @Test
    fun part1() {
        assertEquals(countIntersectsInXY(parseStones("day24example.txt"), 7.0..27.0), 2)
        assertEquals(countIntersectsInXY(parseStones("day24.txt"), 200000000000000.0..400000000000000.0), 17776)
    }

    private fun countIntersectsInXY(stones: List<Stone>, range: ClosedFloatingPointRange<Double>): Long {
        var result = 0L
        // Small enough to brute force each pair
        (0..(stones.size - 2)).forEach() { i ->
            ((i + 1)..stones.size - 1).forEach { j ->
                if (intersectsInXY(stones[i], stones[j], range)) {
                    result += 1
                }
            }
        }
        return result
    }

    @Test
    fun testIntersectsInXY() {
        val r = (7.0..27.0)

        val a = Stone(Triple(19, 13, 20), Triple(-2, 1, -2))
        val b = Stone(Triple(18, 19, 22), Triple(-1, -1, -2))
        assertTrue(intersectsInXY(a, b, r))

        val a2 = Stone(Triple(19, 13, 30), Triple(-2, 1, -2))
        val b2 = Stone(Triple(12, 31, 28), Triple(-1, -2, -1))
        assertFalse(intersectsInXY(a2, b2, r))

        val a3 = Stone(Triple(19, 13, 30), Triple(-2, 1, -2))
        val b3 = Stone(Triple(20, 19, 15), Triple(1, -5, -3))
        assertFalse(intersectsInXY(a3, b3, r))
    }

    fun intersectsInXY(a: Stone, b: Stone, range: ClosedFloatingPointRange<Double>): Boolean {
        fun toCoefficients(s: Stone): Pair<Double, Double> {
            val y = (s.vels.y * 1.0) / s.vels.x   // Normalised
            val yInterspect = s.pos.y - (s.pos.x * y)
            return Pair(y, yInterspect)
        }

        val la = toCoefficients(a)
        val lb = toCoefficients(b)

        // Solve for x intersect
        // (la.first * x) + la.second  = (lb.first * x) + lb.second
        // (la.first * x) =  (lb.first * x) + lb.second -  la.second
        // (la.first * x) -  (lb.first * x =  lb.second -  la.second
        // (la.first - (lb.first) * x =  lb.second -  la.second
        // x =  lb.second -  la.second / (la.first - (lb.first)
        val dv = la.first - lb.first
        return if (dv != 0.0) {
            val xi = (lb.second - la.second) * 1.0 / dv
            val yi = (la.first * xi) + la.second

            val crossed = xi in range && yi in range
            if (crossed) {
                fun isInPastOf(s: Stone): Boolean {
                    // Check for if this was in the past?
                    val dy = yi - s.pos.y
                    val tsy = dy / s.vels.y
                    val dx = xi - s.pos.x
                    val tsx = dx / s.vels.x
                    return tsx <= 0 && tsy <= 0
                }
                return !isInPastOf(a) && !isInPastOf(b)
            } else {
                false
            }

        } else {
            false
        }
    }

    private fun parseStones(filename: String) = stringsFromFile(filename).map { line ->
        val split = line.split(" @ ")
        val poss = split[0].split(", ").map { it.trim().toLong() }
        val vels = split[1].split(", ").map { it.trim().toLong() }
        Stone(Triple(poss[0], poss[1], poss[2]), Triple(vels[0], vels[1], vels[2]))
    }

    data class Stone(val pos: Triple, val vels: Triple)
    data class Triple(val x: Long, val y: Long, val z: Long)
}