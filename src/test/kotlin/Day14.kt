import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day14 : Helpers {

    @Test
    fun part1() {
        assertEquals(rolledNorthLoad(parsePlatform("day14example.txt")), 136)
        assertEquals(rolledNorthLoad(parsePlatform("day14.txt")), 109596)
    }

    private fun rolledNorthLoad(rolledNorth: Platform): Int {
        // Roll all of the rocks to the north
        val rolledNorth = rollNorth(rolledNorth)

        // Calculate load
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
        val maxX = platform.rocks.map { it.point.x }.max()

        val movedRocks = mutableSetOf<Rock>()

        (0..maxX).map { x ->
            val column = platform.rocks.filter { it.point.x == x }.sortedBy { it.point.y }
            // Redraw this column
            var clearSlot = 0
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
