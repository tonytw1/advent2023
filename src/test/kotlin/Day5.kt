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
        val example = parseAlmanac("day5example.txt")

        val seedRanges = mutableListOf<LongRange>()
        var i = 0
        while (i < example.seeds.size) {
            val first = example.seeds[i]
            val length = example.seeds[i + 1]
            seedRanges.add(first..first + length)
            i +=2
        }

        val seeds = seedRanges.map { r ->
            r.toList()
        }.flatten()

        val locationsFrom = locationsFrom(seeds, example.mappings)
        assertEquals(locationsFrom.min(), 46)
    }

    private fun locationsFrom(seeds: List<Long>, mappings: List<Mapping>): List<Long> {
        val locations = seeds.map { seed ->
            // Foreach seed, transform it through each mapping
            var s = seed
            mappings.forEach { mapping ->
                // Is there a mapping which catches this inpout
                val matchingRange = mapping.ranges.find { r ->
                    val inputRange = r.source..r.source + r.size
                    s in inputRange
                }
                if (matchingRange != null) {
                    val delta = matchingRange.destination - matchingRange.source
                    s += delta
                }
            }
            s
        }
        return locations
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
                ranges.add(Range(fields[0], fields[1], fields[2]))
                rangeLine = lines.next()
            }
            mappings.add(Mapping(name, ranges.toList()))
        }

        return Almanac(seeds, mappings)
    }
}

data class Almanac(val seeds: List<Long>, val mappings: List<Mapping>)
data class Mapping(val name: String, val ranges: List<Range>)
data class Range(val destination: Long, val source: Long, val size: Long)