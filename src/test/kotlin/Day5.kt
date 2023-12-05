import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day5 : Helpers {

    @Test
    fun part1() {
        val example = parseAlmanac("day5example.txt")
        assertEquals(locationsFrom(example.seeds, example.mappings).min(), 35)
        val actual = parseAlmanac("day5.txt")
        assertEquals(locationsFrom(actual.seeds, actual.mappings).min(), 579439039)
    }

    @Test
    fun part2() {
        val almanac = parseAlmanac("day5example.txt")

        // Build seed ranges
        // There are no obvious overlaps; no wins from merging
        val seedRanges = mutableListOf<LongRange>()
        var i = 0
        while (i < almanac.seeds.size) {
            val first = almanac.seeds[i]
            val length = almanac.seeds[i + 1]
            seedRanges.add(first..first + length)
            i += 2
        }

        // Only 100s of millions so just brute force it
        var min = Long.MAX_VALUE
        var c = 0L
        seedRanges.forEach { r ->
            val iterator = r.iterator()
            while (iterator.hasNext()) {
                val element = iterator.next()
                val location = locationsFrom(listOf(element), almanac.mappings).first()
                if (location < min) {
                    min = location
                }
                c++
            }
        }
        assertEquals(min, 46)
    }

    private fun locationsFrom(seeds: List<Long>, mappings: List<Mapping>): List<Long> {
        return seeds.map { seed ->
            // Foreach seed, transform it through each mapping
            var s = seed
            mappings.forEach { mapping ->
                // Is there a mapping which catches this input
                val matchingRange = mapping.ranges.find { r ->
                    s in r.source
                }
                if (matchingRange != null) {
                    s += matchingRange.delta
                }
            }
            s
        }
    }

    private fun parseAlmanac(filename: String): Almanac {
        val lines = stringsFromFile(filename).iterator()
        val seeds = lines.next().split(": ")[1].split(" ").map { it.toLong() }
        lines.next()

        val mappings = mutableListOf<Mapping>()
        while (lines.hasNext()) {
            val name = lines.next().split(":")[0]
            val ranges = mutableListOf<Range>()
            var rangeLine = lines.next()
            while (rangeLine.isNotBlank() && lines.hasNext()) {
                val fields = rangeLine.split(" ").map { it.toLong() }
                val size = fields[2]
                val destStart = fields[0]
                val sourceStart = fields[1]
                val delta = destStart - sourceStart
                // inclusive vs exclusive is important in part2
                ranges.add(Range(delta, sourceStart..<(sourceStart + size)))
                rangeLine = lines.next()
            }
            mappings.add(Mapping(name, ranges.toList()))
        }
        return Almanac(seeds, mappings)
    }
}

data class Almanac(val seeds: List<Long>, val mappings: List<Mapping>)
data class Mapping(val name: String, val ranges: List<Range>)
data class Range(val delta: Long, val source: LongRange)