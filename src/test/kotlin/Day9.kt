import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day9 : Helpers {

    @Test
    fun part1() {
        assertEquals(sumOfRightHistory("day9example.txt"), 114)
        assertEquals(sumOfRightHistory("day9.txt"), 1980437560)
    }

    @Test
    fun part2() {
        assertEquals(sumOfRightHistory("day9example.txt"), 2)
        assertEquals(sumOfRightHistory("day9.txt"), 977)
    }

    private fun sumOfRightHistory(filename: String): Long {
        return stringsFromFile(filename).sumOf {
            historyValueOf(spaceSeperatedLongs(it))
        }
    }

    @Test
    fun historyValue() {
        // assertEquals(historyValueOf(listOf(0, 3, 6, 9, 12, 15).map { it.toLong() }), 18)
        //assertEquals(historyValueOf(listOf(1, 3, 6, 10, 15, 21).map { it.toLong() }), 28)
        //assertEquals(historyValueOf(listOf(10, 13, 16, 21,  30, 45).map { it.toLong() }), 68)
        assertEquals(historyValueOf(listOf(10, 13, 16, 21, 30, 45).map { it.toLong() }), 5)
    }

    private fun historyValueOf(input: List<Long>): Long {
        val lasts = mutableListOf<Long>()
        val firsts = mutableListOf<Long>()

        var row = input
        while (!row.all { it == 0L }) {
            val diffs = mutableListOf<Long>()
            for (i in 0..(row.size - 2)) {
                diffs.add(row[i + 1] - row[i])
            }
            lasts.add(row.last())
            firsts.add(row.first())
            row = diffs
        }

        firsts.reverse()
        return firsts.fold(0L) { acc, it ->
            it - acc
        }
    }

    private fun spaceSeperatedLongs(line: String) = line.split(" ").map { it.toLong() }

}