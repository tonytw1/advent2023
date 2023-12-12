import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.math.BigDecimal

class Day12 : Helpers {

    @Test
    fun part1() {
        assertEquals(parseRecords("day12example1.txt").map { findValid(it) }.sum(), 21)
        assertEquals(parseRecords("day12.txt").map { findValid(it) }.sum(), 7110)
    }

    @Test
    fun part2() {
        val unfolded = parseRecords("day12example1.txt").map { unfold(it) }

        var i =0
        val scores = unfolded.reversed().map{ it ->
            val findValid = findValid(it)
            println("$i: $findValid")
            i += 1
            findValid

        }

        val total = scores.fold(BigDecimal(0)) { acc, it ->
            acc.add(BigDecimal(it))
        }

        assertEquals(total, BigDecimal(525152))
    }

    @Test
    fun testIsValid() {
        assertTrue(isValid("#.#.###", listOf(1, 1, 3)))
        assertTrue(isValid("..#..#....###.", listOf(1, 1, 3)))
    }

    @Test
    fun testFindValid() {
        val records = parseRecords("day12example1.txt")
        assertEquals(findValid(records[1]), 4)
        assertEquals(findValid(records[5]), 10)
    }

    @Test
    fun testUnfold() {
        assertEquals(
            unfold(ConditionRecord(".#", listOf((1)))),
            ConditionRecord(".#?.#?.#?.#?.#", listOf(1, 1, 1, 1, 1))
        )
        assertEquals(
            unfold(ConditionRecord("???.###", listOf(1, 1, 3))), ConditionRecord(
                "???.###????.###????.###????.###????.###", listOf(
                    1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3
                )
            )
        )
    }

    fun unfold(record: ConditionRecord): ConditionRecord {
        val data = (1..5).fold("") { acc, _ ->
            "$acc${record.data}?"
        }.dropLast(1)
        val counts = (1..5).fold(mutableListOf<Int>()) { acc, _ ->
            acc.addAll(record.counts)
            acc
        }
        return ConditionRecord(data, counts)
    }

    private fun findValid(r: ConditionRecord): Int {
        var v = 0

        // Naive DFS
        fun visit(candidate: String) {
            val depth = candidate.length

            // Optimise by bailing on paths with incorrect steaks
            if (depth > 0 && candidate.last() == '.') {
                val currentStreaks: List<Int> = streaksFor(candidate)
                if (r.counts.take(currentStreaks.size) != currentStreaks) {
                    return
                }
            }

            if (depth == r.data.length) {
                val valid = isValid(candidate, r.counts)
                if (valid) {
                    v += 1
                }
            } else {
                val c = r.data[depth]
                if (c == '?') {
                    visit("$candidate.")
                    visit("$candidate#")
                } else {
                    visit(candidate + c)
                }

            }
        }
        visit("")
        return v
    }

    fun isValid(data: String, counts: List<Int>): Boolean {
        val streaks = streaksFor(data)
        return streaks == counts
    }

    private fun streaksFor(data: String): MutableList<Int> {
        val streaks = mutableListOf<Int>()
        var streak = 0
        for (i in data.indices) {
            val c = data.get(i)
            if (c == '#') {
                streak += 1
            } else {
                if (streak > 0) {
                    streaks.add(streak)
                    streak = 0
                }
            }
        }
        if (streak > 0) {
            streaks.add(streak)
        }
        return streaks
    }

    private fun parseRecords(filename: String) = stringsFromFile(filename).map { line ->
        val split = line.split(" ")
        val data = split[0]
        val counts = split[1].split(",").map { it.toInt() }
        ConditionRecord(data, counts)
    }

}

data class ConditionRecord(val data: String, val counts: List<Int>)