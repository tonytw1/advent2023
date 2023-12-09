import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day9 : Helpers {

    @Test
    fun part1() {
        assertEquals(sumOfHistory("day9example.txt"), 114)
        assertEquals(sumOfHistory("day9.txt"), 1980437560)
    }

    @Test
    fun part2() {
        assertEquals(sumOfHistory("day9example.txt", first = true), 2)
        assertEquals(sumOfHistory("day9.txt", first = true), 977)
    }

    private fun sumOfHistory(filename: String, first: Boolean = false): Long {
        return stringsFromFile(filename).sumOf {
            historyValueOf(spaceSeperatedLongs(it), first)
        }
    }

    @Test
    fun historyValue() {
        assertEquals(historyValueOf(listOf(0, 3, 6, 9, 12, 15).map { it.toLong() }, first = false), 18)
        assertEquals(historyValueOf(listOf(1, 3, 6, 10, 15, 21).map { it.toLong() }, first = false), 28)
        assertEquals(historyValueOf(listOf(10, 13, 16, 21, 30, 45).map { it.toLong() }, first = false), 68)
        assertEquals(historyValueOf(listOf(10, 13, 16, 21, 30, 45).map { it.toLong() }, first = true), 5)
    }

    private fun historyValueOf(input: List<Long>, first: Boolean): Long {
        val firstsAndLastsFor = firstsAndLastsFor(input)
        val rollUp = if (first) {
            Pair(firstsAndLastsFor.first) { acc, it ->
                acc - it
            }
        } else {
            Pair(firstsAndLastsFor.second) { acc: Long, it: Long ->
                acc + it
            }
        }
        return rollUp.first.foldRight(0L) { acc, it ->
            rollUp.second(acc, it)
        }
    }

    private fun firstsAndLastsFor(input: List<Long>): Pair<List<Long>, List<Long>> {
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
        return Pair(firsts, lasts)
    }

    private fun spaceSeperatedLongs(line: String) = line.split(" ").map { it.toLong() }

}