import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.lang.RuntimeException

class Day21 : Helpers {

    @Test
    fun part1() {
        assertEquals(walk(parseGarden("day21example.txt"), 6).size, 16)
        assertEquals(walk(parseGarden("day21.txt"), 64).size, 3503)
    }

    @Test
    fun part2() {
        val garden = parseGarden("day21.txt")
        // Scale out the garden to experiment with
        val scaledGarden = scaleOut(garden, 4)
        // Is the number of steps taken interesting?
        val z = 26501365

        val i =  garden.width() / 2  // Number of steps to span first page is 65 steps with reminder 1; the start point

        // After reaching the edge of the first garden; is the number of steps to z a multiple of full garden widths?
        val stepsAfterFirstGarden = z - i
        val reminder = ((stepsAfterFirstGarden % garden.width()))
        if (reminder !=0) {
            throw RuntimeException()
        }

        val extraGardensToCover = stepsAfterFirstGarden / garden.width()
        //println(extraGardensToCover) //= 202300

        // So we can now express the problem in terms of how may extra garden widths to span
        // And we need th result for numOfExtraGardenWidths = 202300

        // We can brute force the first few in a reaonable amout of time to gain information.
        // Part 1 is our north star
       (0..3).forEach { numOfExtraGardenWidths ->
            //0 - 3606
            //1 - 32258
            //2 - 89460
            //3 - 175212
            //4 - 289514
           val width = garden.width()
           println("!!! W $width")
           println("$numOfExtraGardenWidths: " + walk(scaledGarden, i + (numOfExtraGardenWidths * width)).size)
        }

        var n = 1L
        (0L..5).forEach { i ->
            val d = (i * 4)
            println("NL $i: $n $d")
            n += d
        }



        // As we scale out, we get muliples of the first dimamond and the filler diamonds
        // 0: 1, 0  (0)             1 + n * 4)
        // 1: 5, 4  (0 + 4)
        // 2: 13, 12    (0 + 4 + 8)
        // 3  25 = 29, 28   (0 + 4 + 8 + 16)
        // l = 0 and l = 1 tell us about the area of the different diamonds
        val areaOfFirstDiamond = walkForToExtraGardens(scaledGarden, garden, 0)
        println("D1A: " + areaOfFirstDiamond)

        val areaOf5MainDiamondsAnd4SecondardDiamonds = walkForToExtraGardens(scaledGarden, garden, 1)
        val areaOf4SecondaryDiamonds = areaOf5MainDiamondsAnd4SecondardDiamonds - (areaOfFirstDiamond * 5)

        val areaOfSecondaryDiamond = areaOf4SecondaryDiamonds / 4
        if (areaOf4SecondaryDiamonds % 4 != 0) {
            //throw RuntimeException()
        }
        println("D2A: " + areaOfSecondaryDiamond)


        // Cross check with l=2
        println("N1 " + ((areaOfFirstDiamond * 5) + (areaOf4SecondaryDiamonds * 1)))
        println("N2 " + ((areaOfFirstDiamond * 13) + (areaOf4SecondaryDiamonds * 3)))
        println("N3 " + ((areaOfFirstDiamond * 25) + (areaOf4SecondaryDiamonds * 7)))
    }



    private fun walkForToExtraGardens(
        scaledGarden: Garden,
        garden: Garden,
        numOfExtraGardenWidths: Int
    ): Int {
        val firstGardenBoundary = garden.width() / 2
        return walk(scaledGarden, (firstGardenBoundary) + ((garden.width()) * numOfExtraGardenWidths) -1).size
    }

    private fun walk(garden: Garden, range: Int): Set<Point> {
        fun neighboursOf(p: Point): Set<Point> {
            val neighbours = mutableListOf<Point>()

            for (y in listOf(p.y - 1, garden.minY).max()..listOf(p.y + 1, garden.maxY).min()) {
                val n = Point(y, p.x)
                if (!garden.rocks.contains(n)) {
                    neighbours.add(n)
                }
            }
            for (x in listOf(p.x - 1, garden.minX).max()..listOf(p.x + 1, garden.maxX).min()) {
                val n = Point(p.y, x)
                if (!garden.rocks.contains(n)) {
                    neighbours.add(n)
                }
            }
            return neighbours.filter { it != p }.toSet()
        }

        val endPoints = mutableSetOf<Point>()

        // There is alot of revisting here; so remember paths we have already exhausted
        val cache = mutableSetOf<Pair<Point, Int>>()

        // DFS not BFS because we are allowed to back track!
        fun visit(p: Point, rangeRemaining: Int) {
            val cacheKey = Pair(p, rangeRemaining)
            if (cache.contains(cacheKey)) {
                return
            }
            cache.add(cacheKey)

            val ns = neighboursOf(p)
            if (rangeRemaining - 1 == 0) {
                endPoints.addAll(ns)
            } else {
                ns.forEach {
                    visit(it, rangeRemaining - 1)
                }
            }
        }

        visit(garden.start, range)
        return endPoints
    }

    fun scaleOut(garden: Garden, layers: Int): Garden {
        val d = layers
        val g = garden.copy()
        val map = (-d..d).flatMap { iy ->
            val dy = iy * garden.height()
            (-d..d).flatMap { ix ->
                val dx = ix * garden.width()
                garden.rocks.map { r ->
                    Point(r.y + dy, r.x + dx)
                }
            }
        }

        return g.copy(
            minY = g.minX - (d * garden.height()) , maxY =  g.maxX + (d * garden.height()),
            minX = g.minX - (d * garden.width()) , maxX =  g.maxX + (d * garden.width()),
            rocks =  map.toSet())
    }

    private fun printGarden(garden: Garden, endPoints: Set<Point>) {
        // Render the garden with set of end points
        (garden.minY..garden.maxY).forEach { y ->
            val l = (garden.minX..garden.maxX).map { x ->
                val p = Point(y, x)
                if (endPoints.contains(p)) {
                    'O'
                } else if (garden.rocks.contains(p)) {
                    '#'
                } else {
                    '.'
                }
            }.toCharArray()
            println(l)
        }
    }

    private fun parseGarden(filename: String): Garden {
        // Need to know the bounds of the garden and the location of the rocks and the starting point
        val lines: List<String> = stringsFromFile(filename)
        val height = lines.size
        val width = lines[0].length

        val rocks = lines.withIndex().flatMap { line ->
            line.value.toCharArray().withIndex().filter { it.value == '#' }.map { c ->
                Point(line.index, c.index)
            }
        }.toSet()

        val start = lines.withIndex().flatMap { line ->
            line.value.toCharArray().withIndex().filter { it.value == 'S' }.map { c ->
                Point(line.index, c.index)
            }
        }.first()

        // Recenter the garden around the start point; makes scaling out easier
        val recenteredRocks = rocks.map { r ->
            Point(r.y - start.y, r.x - start.x)
        }.toSet()

        val minY = 0 - start.y
        val minX = 0 - start.x
        return Garden(minY = minY, maxY = minY + height -1, minX = minX, maxX = minX + width - 1, rocks = recenteredRocks, start = Point(0, 0))
    }

    data class Point(val y: Int, val x: Int)
    data class Garden(val minY: Int, val maxY: Int, val minX: Int, val maxX: Int, val rocks: Set<Point>, val start: Point) {
        fun width() = (maxX - minX) + 1
        fun height() = (maxY - minY) + 1
    }

}