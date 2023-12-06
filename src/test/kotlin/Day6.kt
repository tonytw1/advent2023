import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day6 : Helpers {

    @Test
    fun part1() {
        assertEquals(marginOfWinning(parseRaces("day6example.txt")), 288)
        assertEquals(marginOfWinning(parseRaces("day6.txt")), 2344708)
    }

    @Test
    fun part2() {
        assertEquals(kernedWins(parseRaces("day6example.txt")), 71503)
        assertEquals(kernedWins(parseRaces("day6.txt")), 30125202)
    }

    private fun marginOfWinning(races: List<Pair<Long, Long>>) = races.map { race ->
        numberOfWaysToWin(race)
    }.reduce { acc, i -> acc * i }

    private fun kernedWins(races: List<Pair<Long, Long>>): Int {
        val time = StringBuffer()
        val distance = StringBuffer()
        races.forEach { race ->
            time.append(race.first)
            distance.append(race.second)
        }
        val kerned = Pair(time.toString().toLong(), distance.toString().toLong())
        return numberOfWaysToWin(kerned)
    }

    private fun numberOfWaysToWin(race: Pair<Long, Long>): Int {
        val duration = race.first
        val recordDistance = race.second
        return (0..duration).map {
            val v = it
            val runningTime = duration - it
            v * runningTime
        }.filter { it > recordDistance }.size
    }

    private fun parseRaces(filename: String): List<Pair<Long, Long>> {
        val lines = stringsFromFile(filename)
        val times = lines.first().split(":")[1].replace(multipleBlankSpace, " ").replaceFirst(" ", "").split(" ")
            .map { it.toLong() }
        val distances = lines.last().split(":")[1].replace(multipleBlankSpace, " ").replaceFirst(" ", "").split(" ")
            .map { it.toLong() }
        return times.zip(distances)
    }
}
