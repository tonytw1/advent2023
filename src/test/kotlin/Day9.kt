import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day9 : Helpers {

    @Test
    fun part1() {
        assertEquals(sumOfRightHistory("day9example.txt"), 114)
        assertEquals(sumOfRightHistory("day9.txt"), 1980437560)
    }

    private fun sumOfRightHistory(filename: String): Long {
        return stringsFromFile(filename).sumOf {
            historyValueOf(spaceSeperatedLongs(it))
        }
    }

    @Test
    fun historyValue() {
        assertEquals(historyValueOf(listOf(0, 3, 6, 9, 12, 15).map { it.toLong() }), 18)
        assertEquals(historyValueOf(listOf(1, 3, 6, 10, 15, 21).map { it.toLong() }), 28)
    }

    private fun historyValueOf(input: List<Long>): Long {
        var lasts = mutableListOf<Long>()

        var row = input
        while (!row.all { it == 0L }) {
            var diffs = mutableListOf<Long>()
            for (i in 0..(row.size - 2)) {
                diffs.add(row[i + 1] - row[i])
            }
            lasts.add(row.last())
            row = diffs
        }

        return lasts.fold(0L) { acc, it ->
            acc + it
        }
    }

    private fun spaceSeperatedLongs(line: String) = line.split(" ").map { it.toLong() }

}