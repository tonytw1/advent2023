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
        joined.addAll(trace.first.drop(1).dropLast(1).reversed())
        val loop = joined.toList()
        val loopPoints = loop.map { it.point }.toSet()

        // Trace out the loop; for every straight segment touch the ground to the left and right
        // Keep a heading so that left and right can be worked out.
        // Flood fill from each touched region.
        // Collect unique regions and if they were discovered to the left or right.
        // At the end just the left or right regions should be touching the border.
        // This will tell us if left or right was inside or outside the loop.
        // We can then sum up the number of points in the outside regions

        var nextRegionNumber = 1
        val regions = mutableMapOf<Point, Int>()
        val regionTypes = mutableMapOf<Int, Boolean>()

        fun fillNeighboursOf(point: Point, map: Array<CharArray>): List<Point> {
            return listOf(
                Point(point.y - 1, point.x),
                Point(point.y + 1, point.x),
                Point(point.y, point.x + 1),
                Point(point.y, point.x - 1)
            ).filter { p ->
                p.y in map.indices && p.x in 0..<map[0].size
            }
        }

        fun examine(point: Point, left: Boolean) {
            // Given a point to the left or right of the loop, examine it and try to build connected regions of adjacent points
            if (point.y in map.indices && point.x in 0..<map[0].size) {
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
                            fillNeighboursOf(p, map).forEach { nc ->
                                val isEnclosable = !loopPoints.contains(nc)
                                if (isEnclosable) {
                                    if (!regions.contains(nc) && !fill.contains(nc)) {
                                        fill.add(nc)
                                    } else {
                                        if (regions[nc] != regionId) {
                                            // Remap the region we just touched to join it to us
                                            val oldRegionId = regions[nc]
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
            // We can always determine our direction from the location of the previous
            val dx = c.point.x - previous.point.x
            val dy = c.point.y - previous.point.y

            // This means left and right can be determined as well
            // We don't know if left or right is inside the loop yet
            fun examineLeftAndRightOf(p: Point) {
                var leftAndRight = if (dy == 0) {
                    Pair(
                        Point(p.y + dx, p.x),
                        Point(p.y - dx, p.x)
                    )
                } else {
                    Pair(
                        Point(p.y, p.x - dy),
                        Point(p.y, p.x + dy)
                    )
                }
                examine(leftAndRight.first, false)
                examine(leftAndRight.second, true)
            }

            // Examine to the left and right of this point
            examineLeftAndRightOf(c.point)
            // Also do it for previous as corners have 2 lefts!!!
            examineLeftAndRightOf(previous.point)

            previous = c
        }

        // So we now have connected regions. We know if they were to the left of right of the loop.
        // Need to determine if left or right is outside, as defined by touching the border.
        val regionsWhichTouchBorder = regionTypes.keys.filter { regionId ->
            val isOutside = regions.filter { it ->
                it.value == regionId
            }.any {
                it.key.y == 0 || it.key.y == map.size - 1 ||
                        it.key.x == 0 || it.key.x == map[0].size - 1

            }
            isOutside
        }

        val leftOrRightnessOfRegionsWhichTouchBorder = regionsWhichTouchBorder.map { regionId ->
            regionTypes[regionId]!!
        }

        // Knowing if left or right is the outside side helps us find outside regions which don't directly touch the border
        val outsideRegionSide = if (leftOrRightnessOfRegionsWhichTouchBorder.isNotEmpty()) {
            leftOrRightnessOfRegionsWhichTouchBorder.first()  // TODO check all the same
        } else {
            !regionTypes.values.first()  // TODO check all the same
        }

        val internalRegions = regionTypes.filter { it.value != outsideRegionSide }.keys

        return internalRegions.sumOf { internalRegion ->
            regions.filter { it.value == internalRegion }.size
        }
    }

    private fun trace(map: Array<CharArray>): Pair<List<Cell>, List<Cell>> {
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
            val visitedLeft = mutableListOf(startCell)  // Part 2 - 40ms to 1 sec perf hit for using a list
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
        return results.maxByOrNull { it.first.size }!!
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
