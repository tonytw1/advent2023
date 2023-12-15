import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.util.stream.Collectors

class Day12 : Helpers {

    @Test
    fun part1() {
        assertEquals(parseRecords("day12example1.txt").map { findValid(it) }.sum(), 21)
        assertEquals(parseRecords("day12.txt").map { findValid(it) }.sum(), 7110)
    }

    @Test
    fun part2() {
        fun countUnfolded(filename: String): Long {
            return parseRecords(filename).map { unfold(it) }.parallelStream().map { findValid(it) }
                .collect(Collectors.toList()).sum()
        }
        assertEquals(countUnfolded("day12example1.txt"), 525152)
        assertEquals(countUnfolded("day12.txt"), 1566786613613)
    }

    private fun findValid(r: ConditionRecord): Long {
        val cache = mutableMapOf<String, Long>()

        // Naive DFS
        fun visit(remaining: String, found: List<Int>): Long {
            val cacheKey = remaining + found.hashCode()
            val l = cache[cacheKey]
            if (l != null) {
                return l
            }
            // Check for exit condition
            if (remaining.isEmpty()) {
                if (found == r.counts) {
                    return 1
                } else {
                    return 0
                }
            }
            // Optimisations; bailing out early
            if (found.size > r.counts.size) {
                return 0
            }
            if (found.isNotEmpty() && found.last() != r.counts[found.indices.last]) {
                return 0
            }

            var left = ""
            var right = remaining
            while (right.isNotEmpty()) {
                var nextChar = right[0]
                right = right.drop(1)
                if (nextChar == '#') {
                    left += '#'
                }

                if (nextChar == '.') {
                    val newFound = found.toMutableList()
                    if (left.isNotEmpty()) {
                        newFound.add(left.length)
                    }
                    return visit(right, newFound)
                }

                if (nextChar == '?') {
                    // The result of a branch is the sum of their individual scores
                    // Dot branch
                    val newFound = found.toMutableList()
                    if (left.isNotEmpty()) {
                        newFound.add(left.length)
                    }
                    val dotBranch = visit(right, newFound)
                    // Hash branch
                    val hashBranch = visit(left + '#' + right, found)

                    val visit = dotBranch + hashBranch
                    // Interestingly this branch is the only one which shows improvement with caching.
                    cache[cacheKey] = visit
                    return visit
                }
            }

            var newFound = mutableListOf<Int>()
            newFound.addAll(found)
            if (left.length > 0) {
                newFound.add(left.length)
            }

            return visit(right, newFound)
        }

        return visit(r.data, emptyList())
    }

    private fun unfold(record: ConditionRecord): ConditionRecord {
        val data = (1..5).fold("") { acc, _ ->
            "$acc${record.data}?"
        }.dropLast(1)
        val counts = (1..5).fold(mutableListOf<Int>()) { acc, _ ->
            acc.addAll(record.counts)
            acc
        }
        return ConditionRecord(data, counts)
    }

    private fun parseRecords(filename: String) = stringsFromFile(filename).map { line ->
        val split = line.split(" ")
        val data = split[0]
        val counts = split[1].split(",").map { it.toInt() }
        ConditionRecord(data, counts)
    }

}

data class ConditionRecord(val data: String, val counts: List<Int>)