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
        val example = parseAlmanac("day5.txt")

        val seedRanges = mutableListOf<LongRange>()
        var i = 0
        while (i < example.seeds.size) {
            val first = example.seeds[i]
            val length = example.seeds[i + 1]
            seedRanges.add(first..first + length)
            i +=2
        }

        println(seedRanges)
        seedRanges.sortBy { it.first }
            seedRanges.forEach {
            println("" + it.first + " -> " + it.last  + ": " + (it.last - it.first))
        }

        val total = seedRanges.map {
            it.last - it.first
        }.sum()
        println(total)

        var min = Long.MAX_VALUE
        var c = 0L
        seedRanges.forEach { r ->
            val iterator = r.iterator()
            while(iterator.hasNext()) {
                val element = iterator.next()
                val location = locationsFrom(listOf(element), example.mappings).first()
                if (location <  min) {
                    println("" + element + ": " + location)
                    min = location
                }
                c++
                if (c % 1000000L == 0L) {
                    println((c * 1.0 / total) * 100)
                }
            }
        }
        println("MIN: " + min)
    }

    private fun locationsFrom(seeds: List<Long>, mappings: List<Mapping>): List<Long> {
        val locations = seeds.map { seed ->
            // Foreach seed, transform it through each mapping
            var s = seed
            mappings.forEach { mapping ->
                // Is there a mapping which catches this input
                val matchingRange = mapping.ranges.find { r ->
                    val inputRange = r.source..r.source + r.size
                    s in inputRange
                }
                if (matchingRange != null) {
                    val delta = matchingRange.destination - matchingRange.source
                    val a = s + delta
                    if (delta > 0 && a < s) {
                        throw RuntimeException()
                    }
                    s = a
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