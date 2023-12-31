import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day2 : Helpers {

    @Test
    fun part1() {
        assertEquals(possible(stringsFromFile("day2example.txt")), 8)
        assertEquals(possible(stringsFromFile("day2.txt")), 1931)
    }

    @Test
    fun part2() {
        assertEquals(minimum(stringsFromFile("day2example.txt")), 2286)
        assertEquals(minimum(stringsFromFile("day2.txt")), 83105)
    }

    private val limits = mapOf(
        "red" to 12,
        "green" to 13,
        "blue" to 14,
    )

    fun possible(lines: List<String>): Int {
        return parseGames(lines).filter { game ->
            game.counts.all { it.all { e -> e.value <= limits[e.key]!! } }
        }.sumOf { it.id }
    }

    fun minimum(lines: List<String>): Int {
        return parseGames(lines).sumOf { game ->
            limits.keys.map { colour ->
                game.counts.map { it.getOrDefault(colour, 0) }.max()
            }.reduce { a, i -> a * i }
        }
    }

    private fun parseGames(lines: List<String>): List<Game> {
        return lines.map {
            val id = it.split(":")[0].split("Game ")[1].toInt()
            val cols = it.split(":")[1].split(";").map { col ->
                col.split(",").map { cell ->
                    val split = cell.trim().split(" ")
                    val count = split[0].replace(Regex("\\D"), "").toInt()
                    val colour = split[1].trim()
                    Pair(colour, count)
                }.toMap()
            }
            Game(id, cols)
        }
    }
}

data class Game(val id: Int, val counts: List<Map<String, Int>>)