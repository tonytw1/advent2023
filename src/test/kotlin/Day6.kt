import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day6 : Helpers {

    @Test
    fun part1() {
        assertEquals(marginOfWinning(parseRaces("day6example.txt")), 288)
        assertEquals(marginOfWinning(parseRaces("day6.txt")), 2344708)
    }

    private fun marginOfWinning(races: List<Pair<Int, Int>>) = races.map { race ->
        val duration = race.first
        val recordDistance = race.second
        (0..duration).map {
            val v = it
            val runningTime = duration - it
            v * runningTime
        }.filter { it > recordDistance }.size
    }.reduce { acc, i -> acc * i }

    private fun parseRaces(filename: String): List<Pair<Int, Int>> {
        val lines = stringsFromFile(filename)
        val times = lines.first().split(":")[1].replace(multipleBlankSpace, " ").replaceFirst(" ", "").split(" ").map { it.toInt() }
        val distances = lines.last().split(":")[1].replace(multipleBlankSpace, " ").replaceFirst(" ", "").split(" ").map { it.toInt() }
        return times.zip(distances)
    }
}