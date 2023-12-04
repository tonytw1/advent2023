import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import kotlin.math.pow

class Day4 : Helpers {

    @Test
    fun part1() {
        assertEquals(winningPoints(parseCards("day4example.txt")), 13)
        assertEquals(winningPoints(parseCards("day4.txt")), 26346)
    }

    private fun parseCards(filename: String): List<Card> {
        val lines = stringsFromFile(filename)
        val cards = lines.map { line ->
            val splits = line.split(":")
            val id = splits[0].replace(Regex("\\s+"), " ").replace("Card ", "").toInt()
            val results = splits[1].split("|")
            val winning = results[0].trim().replace(Regex("\\s+"), " ").split(" ").map { it.toInt() }
            val ours = results[1].trim().replace(Regex("\\s+"), " ").split(" ").map { it.trim().toInt() }
            Card(id, winning, ours)
        }
        return cards
    }

    private fun winningPoints(cards: List<Card>): Int {
        return cards.map { c ->
            c.ours.filter { i ->
                c.winning.contains(i)
            }.size
        }.sumOf { n ->
            if (n > 0) {
                2.0.pow(n - 1).toInt()
            } else {
                0
            }
        }
    }
}

data class Card(val id: Int, val winning: List<Int>, val ours: List<Int>)