import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day14 : Helpers {

    @Test
    fun part1() {
        assertEquals(loadOf(rollNorth(parsePlatform("day14example.txt"))), 136)
        assertEquals(loadOf(rollNorth(parsePlatform("day14.txt"))), 109596)
    }

    @Test
    fun part2() {
        assertEquals(loadAtCycle(parsePlatform("day14example.txt"), 1000000000), 64)
        assertEquals(loadAtCycle(parsePlatform("day14.txt"), 1000000000), 96105)
    }

    private fun loadAtCycle(platform: Platform, end: Int): Int {
        var p = platform
        val repeatStartAndPeriod = findRepeatPeriod(p, end)
        val period = repeatStartAndPeriod.second - repeatStartAndPeriod.first
        val cyclesFromSecondToEnd = end - repeatStartAndPeriod.first
        val finishStepInPeriod = cyclesFromSecondToEnd % period

        val cycleWhichIsSameAsEnd = repeatStartAndPeriod.first + finishStepInPeriod
        // Run up to this cycle and load measure it
        (1..cycleWhichIsSameAsEnd).forEach { i ->
            (1..4).forEach {
                p = rotate(rollNorth(p))
            }
        }
        return loadOf(p)
    }

    private fun findRepeatPeriod(platform: Platform, end: Int): Pair<Int, Int> {
        var p = platform
        val seen = mutableMapOf<Int, Int>()
        (1..end).forEach { i ->
            (1..4).forEach {
                p = rotate(rollNorth(p))
            }
            // Record when we first saw a given version
            val h = p.rocks.hashCode()
            if (seen.contains(h)) {
                return Pair(seen[h]!!, i)
            }
            seen[h] = i
        }
        throw RuntimeException()
    }

    private fun rotate(platform: Platform): Platform {
        val movedRocks = platform.rocks.map { rock ->
            val p = rock.point
            rock.copy(point = p.copy(y = p.x, x = -p.y))
        }
        return Platform(movedRocks.toSet())
    }

    private fun loadOf(rolledNorth: Platform): Int {
        val heightOfSouthEdge = rolledNorth.rocks.map { it.point.y }.max() + 1
        return rolledNorth.rocks.sumOf { rock ->
            if (rock.type == 'O') {
                heightOfSouthEdge - rock.point.y
            } else {
                0
            }
        }
    }

    private fun rollNorth(platform: Platform): Platform {
        // Split the map into columns
        val minX = platform.rocks.map { it.point.x }.min()
        val maxX = platform.rocks.map { it.point.x }.max()

        val movedRocks = mutableSetOf<Rock>()

        (minX..maxX).map { x ->
            val column = platform.rocks.filter { it.point.x == x }.sortedBy { it.point.y }
            // Redraw this column
            var clearSlot = platform.rocks.map { it.point.y }.min()
            column.forEach { rock ->
                if (rock.type == 'O' && rock.point.y >= clearSlot) {
                    val movedRock = rock.copy(point = rock.point.copy(y = clearSlot))
                    movedRocks.add(movedRock)
                    clearSlot = movedRock.point.y + 1
                }
                if (rock.type == '#') {
                    movedRocks.add(rock)
                    clearSlot = rock.point.y + 1
                }
            }
        }
        return Platform(rocks = movedRocks)
    }

    private fun parsePlatform(filename: String): Platform {
        // Parse the map into sets of rocks and blocks for flexiblity
        val rocks = mutableSetOf<Rock>()
        stringsFromFile(filename).withIndex().forEach() { line ->
            val y = line.index
            line.value.toCharArray().withIndex().forEach { it ->
                val x = it.index
                val point = Point(y, x)
                val c = it.value
                if (c != '.') {
                    rocks.add(Rock(c, point))
                }
            }

        }
        return Platform(rocks)
    }

    data class Platform(val rocks: Set<Rock>)
    data class Rock(val type: Char, val point: Point)
    data class Point(val y: Int, val x: Int)
}
