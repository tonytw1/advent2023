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
        assertEquals(trace("day10example1.txt"), 4)
        assertEquals(trace("day10example2.txt"), 8)
        assertEquals(trace("day10.txt"), 6714)
    }

    fun trace(filename: String): Int {
        // Parse the map
        val map = parseMap(filename)

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

        fun traceWithStartingJoint(startJoint: Joint): Int {
            map[start.point.y][start.point.x] = startJoint.c
            val startCell = start.copy(c = startJoint.c)
            // From the start, BSF left and right in lock step
            // We stop with either arm revisits a node of has no next step of the two searches meet at the same point

            // It's abit easier of are establise the direction of travel by taking the first step for it
            var depth = 1
            var left = move(startCell, mapOfJointTypes[startCell.c]!!.dirs.first())!!
            val visitedLeft = mutableSetOf<Cell>(startCell)
            var right = move(startCell, mapOfJointTypes[startCell.c]!!.dirs.last())!!
            val visitedRight = mutableSetOf<Cell>(startCell)

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
                    return depth
                }
            }
            return -1
        }

        // Determine possible values of start point to explore
        // For each possible joint type; find those which connect 2 neighbours
        val results = validJointsFor(start.point, mapOfJointTypes, map).map { startJoint ->
            traceWithStartingJoint(startJoint)
        }
        return results.max()
    }

    private fun parseMap(filename: String): Array<CharArray> {
        val map = stringsFromFile(filename).map {
            it.toCharArray()
        }.toTypedArray()
        return map
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

}

data class Point(val y: Int, val x: Int)
data class Cell(val point: Point, val c: Char)
data class Joint(val c: Char, val dirs: List<Char>)
