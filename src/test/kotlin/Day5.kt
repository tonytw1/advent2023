import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day5 : Helpers {

    @Test
    fun part1() {
        assertEquals(lowestLocation(parseAlmanac("day5example.txt")), 35)
        assertEquals(lowestLocation(parseAlmanac("day5.txt")), 579439039)
    }

    @Test
    fun part2() {
        assertEquals(lowestLocationByRange(parseAlmanac("day5example.txt")), 46)
        assertEquals(lowestLocationByRange(parseAlmanac("day5.txt")), 7873084)
    }

    fun lowestLocation(almanac: Almanac): Long {
        return locationsFrom(almanac.seeds, almanac.mappings).min()
    }

    private fun lowestLocationByRange(almanac: Almanac): Long {
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
        seedRanges.forEach { r ->
            val iterator = r.iterator()
            while (iterator.hasNext()) {
                val location = locationsFrom(listOf(iterator.next()), almanac.mappings).first()
                if (location < min) {
                    min = location
                }
            }
        }
        return min
    }

    private fun locationsFrom(seeds: List<Long>, mappings: List<Mapping>): List<Long> {
        return seeds.map { seed ->
            // Foreach seed, transform it through each mapping
            mappings.fold(seed) { s: Long, mapping: Mapping ->
                // Is there a mapping which catches this input
                val matchingRange = mapping.ranges.find { r ->
                    s in r.source
                }
                if (matchingRange != null) {
                    s + matchingRange.delta
                } else {
                    s
                }
            }
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