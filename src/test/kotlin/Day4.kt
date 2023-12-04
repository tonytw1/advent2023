import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import kotlin.math.pow

class Day4 : Helpers {

    @Test
    fun part1() {
        assertEquals(winningPoints(parseCards("day4example.txt")), 13)
        assertEquals(winningPoints(parseCards("day4.txt")), 26346)
    }

    @Test
    fun part2() {
        assertEquals(countWon(parseCards("day4example.txt")), 30)
        assertEquals(countWon(parseCards("day4.txt")), 8467762)
    }

    private fun winningPoints(cards: List<Card>): Int {
        return cards.map { numberOfWins(it) }.sumOf { n ->
            if (n > 0) {
                2.0.pow(n - 1).toInt()
            } else {
                0
            }
        }
    }

    private fun countWon(cards: List<Card>): Int {
        // We start with 1 copy of each card; but this can change as we work through the deck
        val counts = cards.associate {
            Pair(it.id, 1)
        }.toMutableMap()

        // We will visit each card number in order.
        for (i in cards.indices) {
            val card = cards[i]
            val wins = numberOfWins(card)
            val copiesOfCard = counts[card.id]!!
            val nextCards = (card.id + 1)..card.id + wins
            // Increment the number of copies of the won cards
            for (w in nextCards) {
                counts[w] = counts[w]!! + copiesOfCard
            }
        }

        return counts.values.sum()
    }

    private fun numberOfWins(c: Card) = c.ours.filter { c.winning.contains(it) }.size

    private fun parseCards(filename: String): List<Card> {
        val cards = stringsFromFile(filename).map { line ->
            val splits = line.replace(Regex("\\s+"), " ").split(": ")
            val id = splits[0].replace("Card ", "").toInt()
            val results = splits[1].split(" | ")
            val winning = results[0].split(" ").map { it.toInt() }
            val ours = results[1].split(" ").map { it.toInt() }
            Card(id, winning, ours)
        }
        return cards
    }
}

data class Card(val id: Int, val winning: List<Int>, val ours: List<Int>)