import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day10 : Helpers {

    val jointTypes = listOf(
        Joint('|', listOf('N', 'S')),
        Joint('-', listOf('E', 'W')),
        Joint('L', listOf('N', 'E')),
        Joint('J', listOf('N', 'W')),
        Joint('7', listOf('S', 'W')),
        Joint('F', listOf('S', 'E'))
    )

    val mapOfJointTypes = jointTypes.associateBy { j ->
        j.c
    }

    @Test
    fun part1() {
        assertEquals(trace(parseMap("day10example1.txt")).first.size - 1, 4)
        assertEquals(trace(parseMap("day10example2.txt")).first.size - 1, 8)
        assertEquals(trace(parseMap("day10.txt")).first.size - 1, 6714)
    }

    @Test
    fun part2() {
        // Check that the part2 examples are valid
        assertEquals(trace(parseMap("day10part2example1.txt")).first.size - 1, 23)
        assertEquals(trace(parseMap("day10part2example2.txt")).first.size - 1, 22)
        assertEquals(trace(parseMap("day10part2example3.txt")).first.size - 1, 70)
        assertEquals(trace(parseMap("day10part2example4.txt")).first.size - 1, 80)

        // Obtain the outline of the loop
        assertEquals(internallyEnclosed("day10part2example1.txt"), 4)
        assertEquals(internallyEnclosed("day10part2example2.txt"), 4)
        assertEquals(internallyEnclosed("day10part2example3.txt"), 8)
        assertEquals(internallyEnclosed("day10part2example4.txt"), 10)
        assertEquals(internallyEnclosed("day10.txt"), 429)

    }

    private fun internallyEnclosed(filename: String): Int {
        val map = parseMap(filename)
        val trace = trace(map)

        val joined = trace.second.toMutableList()
        joined.addAll(trace.first.dropLast(1).reversed())
        val loop = joined.toList()
        println(loop)
        val loopPoints = loop.map { it.point }.toSet()

        // Trace the loop; for every straight segment touch the ground to the left and right
        // Keep a heading so that left and right is maintained.
        // Flood fill from each touched region.
        // Maintain unique regions and if they are left or right inited.
        // At the end only left or right should be touching the border.
        // This will tell us if left or right was inside our outside.

        var nextRegionNumber = 1
        val regions = mutableMapOf<Point, Int>()
        val regionTypes = mutableMapOf<Int, Boolean>()

        fun fillNeighboursOf(point: Point, map: Array<CharArray>): List<Point> {

            val possibles: List<Point> = listOf(
                Point(point.y - 1, point.x),
                Point(point.y + 1, point.x),
                Point(point.y, point.x + 1),
                Point(point.y, point.x - 1)
            )

            return possibles.filter { p ->
                p.y in 0..map.size - 1 && p.x in 0..map[0].size - 1
            }
        }

        fun enqueue(point: Point, left: Boolean) {
            if (point.y in 0..map.size - 1 && point.x in 0..map[0].size - 1) {
                if (!loopPoints.contains(point)) {
                    if (!regions.contains(point)) {
                        // New point
                        val regionId = nextRegionNumber
                        regionTypes[regionId] = left

                        nextRegionNumber += 1

                        val fill = ArrayDeque<Point>()
                        fill.add(point)
                        while (fill.isNotEmpty()) {
                            val p = fill.removeFirst()

                            regions[p] = regionId
                            val neighboursOf = fillNeighboursOf(p, map)
                            neighboursOf.forEach { nc ->
                                val countsAsBeenEnclosed = !loopPoints.contains(nc)
                                if (countsAsBeenEnclosed) {
                                    if (!regions.contains(nc) && !fill.contains(nc)) {
                                        fill.add(nc)
                                    } else {
                                        if (regions[nc] != regionId) {
                                            // Remap the regions we just touched.
                                            val oldRegionId = regions[nc]
                                            println("remapping " + oldRegionId + " to " + regionId)
                                            regions.filter { it.value == oldRegionId }.forEach {
                                                regions[it.key] = regionId
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        var previous = loop.first()
        loop.drop(1).forEach { c ->
            println("CCCC " + c)
            // We can always determine or direction
            // Which means left and right
            val dx = c.point.x - previous.point.x
            val dy = c.point.y - previous.point.y
            println("" + dx + ", " + dy)

            // Determine left and right
            var leftAndRight = if (dy == 0) {
                Pair(
                    Point(c.point.y + dx, c.point.x),
                    Point(c.point.y - dx, c.point.x)
                )
            } else {
                Pair(
                    Point(c.point.y, c.point.x - dy),
                    Point(c.point.y, c.point.x + dy)
                )
            }
            enqueue(leftAndRight.first, false)
            enqueue(leftAndRight.second, true)

            // Also do it for previous as corners have 2 lefts!
            leftAndRight = if (dy == 0) {
                Pair(
                    Point(previous.point.y + dx, previous.point.x),
                    Point(previous.point.y - dx, previous.point.x)
                )
            } else {
                Pair(
                    Point(previous.point.y, previous.point.x - dy),
                    Point(previous.point.y, previous.point.x + dy)
                )
            }
            enqueue(leftAndRight.first, false)
            enqueue(leftAndRight.second, true)
            previous = c
        }

        // So we now have connected regions and we know if they were left of right.
        // Need to determine of left or right is outside, as defined by touching the border.
        println(regions.size)
        println(regions.values)
        println(regionTypes)

        regionTypes.keys.forEach { r ->
            println("" + r + ": " + regions.filter { it.value == r }.size)
        }

        val regionsWhichTouchBorder = regionTypes.keys.filter { regionId ->
            val isOutside = regions.filter { it ->
                it.value == regionId
            }.any {
                val isEdge = it.key.y == 0 || it.key.y == map.size - 1 ||
                        it.key.x == 0 || it.key.x == map[0].size - 1
                isEdge

            }
            isOutside
        }

        println("Regions which touch border: " + regionsWhichTouchBorder)
        val map1 = regionsWhichTouchBorder.map { regionId ->
            regionTypes[regionId]
        }
        println(map1)

        val outsideType = if (map1.isNotEmpty()) {
            map1.first()  // TODO check all the same
        } else {
            !regionTypes.values.first()  // TODO check all the same
        }

        val internalRegions = regionTypes.filter { it.value != outsideType }.keys
        println("Internal: " + internalRegions)

        val r = internalRegions.map { internalRegion ->
            regions.filter { it: Map.Entry<Point, Int> -> it.value == internalRegion }.size
        }.sum()
        return r
    }

    fun trace(map: Array<CharArray>): Pair<List<Cell>, List<Cell>> {
        // Locate the starting point
        val start = findStartingPoint(map)
        // Based on the pipe connecting into start
        // Determine the viable pairs and dual BFS away down them.
        // If they are loop then they will return to start with the same length and visited

        fun move(c: Cell, dir: Char): Cell? {
            val p = c.point
            val newPoint = when (dir) {
                'N' -> Point(p.y - 1, p.x)
                'S' -> Point(p.y + 1, p.x)
                'W' -> Point(p.y, p.x - 1)
                'E' -> Point(p.y, p.x + 1)
                else -> throw java.lang.RuntimeException()
            }

            return if (newPoint.y in map.indices && newPoint.x in 0..<map[0].size) {
                Cell(point = newPoint, c = map[newPoint.y][newPoint.x])
            } else {
                null
            }
        }

        fun connected(c: Cell): List<Pair<Cell, Char>> {
            return mapOfJointTypes[c.c]!!.dirs.map { dir ->
                val newPoint = move(c, dir)
                if (newPoint != null) {
                    Pair(newPoint, dir)
                } else {
                    null
                }
            }.filterNotNull()
        }

        fun validJointsFor(
            point: Point,
            mapOfJoints: Map<Char, Joint>,
            map: Array<CharArray>
        ): List<Joint> {
            val validJoints = mutableListOf<Joint>()

            val neighbours = neighboursOf(point, map)
            for (m in neighbours.indices) {
                for (n in m + 1..<neighbours.size) {
                    val a = neighbours[m]
                    val b = neighbours[n]
                    mapOfJoints.values.forEach { joint ->
                        val connected: Set<Cell> = connected(Cell(point, joint.c)).map { it.first }.toSet()
                        if (connected == setOf(a, b)) {
                            validJoints.add(joint)
                        }
                    }
                }
            }
            return validJoints
        }

        fun traceWithStartingJoint(startJoint: Joint): Pair<List<Cell>, List<Cell>> {
            map[start.point.y][start.point.x] = startJoint.c
            val startCell = start.copy(c = startJoint.c)
            // From the start, BSF left and right in lock step
            // We stop with either arm revisits a node of has no next step of the two searches meet at the same point

            // It's abit easier of are establise the direction of travel by taking the first step for it
            var depth = 1
            var left = move(startCell, mapOfJointTypes[startCell.c]!!.dirs.first())!!
            val visitedLeft = mutableListOf(startCell)  // Part 2 - 40s to 1 sec perf hit for using a list
            var right = move(startCell, mapOfJointTypes[startCell.c]!!.dirs.last())!!
            val visitedRight = mutableListOf(startCell)

            var done = false
            while (!done) {
                // Move left and right
                val nextLeftDir = connected(left).find { c -> !visitedLeft.contains(c.first) }
                if (nextLeftDir != null) {
                    visitedLeft.add(left)
                    left = nextLeftDir.first
                } else {
                    done = true
                }

                val nextRightDir = connected(right).find { c -> !visitedRight.contains(c.first) }
                if (nextRightDir != null) {
                    visitedRight.add(right)
                    right = nextRightDir.first
                } else {
                    done = true
                }

                depth += 1
                if (left == right) {
                    visitedLeft.add(left)
                    visitedRight.add(right)
                    return Pair(visitedLeft.toList(), visitedRight.toList())
                }
            }
            return Pair(emptyList(), emptyList())
        }

        // Determine possible values of start point to explore
        // For each possible joint type; find those which connect 2 neighbours
        val validJointsFor = validJointsFor(start.point, mapOfJointTypes, map)
        val results = validJointsFor.map { startJoint ->
            traceWithStartingJoint(startJoint)
        }
        return results.sortedBy { it.first.size }.last()
    }

    private fun parseMap(filename: String): Array<CharArray> {
        val map = stringsFromFile(filename).map {
            it.toCharArray()
        }.toTypedArray()
        return map
    }

    private fun findStartingPoint(map: Array<CharArray>): Cell {
        for (y in map.indices) {
            for (x in 0..<map[0].size) {
                if (map[y][x] == 'S') {
                    return Cell(Point(y, x), 'S')
                }
            }
        }
        throw RuntimeException()
    }

    private fun neighboursOf(start: Point, map: Array<CharArray>): List<Cell> {
        val neighbours = mutableListOf<Cell>()
        for (y in listOf(start.y - 1, 0).max()..listOf(start.y + 1, map.size - 1).min()) {
            for (x in listOf(start.x - 1, 0).max()..listOf(start.x + 1, map[0].size - 1).min()) {
                if (map[y][x] != '.' && !(start.y == y && start.x == x)) {
                    neighbours.add(Cell(Point(y, x), map[y][x]))
                }
            }
        }
        return neighbours
    }

}

data class Point(val y: Int, val x: Int)
data class Cell(val point: Point, val c: Char)
data class Joint(val c: Char, val dirs: List<Char>)
