import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day7 : Helpers {

    private val charStrengths = listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')

    @Test
    fun part1() {
        assertEquals(totalWinnings(parseHands("day7example.txt")), 6440)
        assertEquals(totalWinnings(parseHands("day7.txt")), 247961593)
    }

    private fun totalWinnings(hands: List<Hand>): Long {
        val byPrimary = hands.groupBy { it.type() }

        val sorted = mutableListOf<Hand>()
        for (i in byPrimary.keys.max() downTo byPrimary.keys.min()) {
            val hands = byPrimary.getOrDefault(i, emptyList())
            val strengthOfFirstUniqueChar = object : Comparator<Hand> {
                override fun compare(a: Hand, b: Hand): Int {
                    for (i in 0..a.cards.length) {
                        val cb = b.cards.toCharArray()[i]
                        val ca = a.cards.toCharArray()[i]
                        val cmp = charStrengths.indexOf(cb).compareTo(charStrengths.indexOf(ca))
                        if (cmp != 0) {
                            return cmp
                        }
                    }
                    return 0
                }
            }
            hands.sortedWith(strengthOfFirstUniqueChar).forEach {
                sorted.add(it)
            }
        }

        var t = 0L
        for (i in 1..sorted.size) {
            val hand = sorted[i - 1]
            t += hand.rank * i
        }
        return t
    }

    private fun parseHands(filename: String): List<Hand> {
        val hands = stringsFromFile(filename).map { line ->
            val cards = line.split(" ")[0]
            val rank = line.split(" ")[1].toInt()
            Hand(cards, rank)
        }
        return hands
    }

}

data class Hand(val cards: String, val rank: Int) {

    fun type(): Int {
        val counts: MutableMap<Char, Int> = mutableMapOf<Char, Int>()
        cards.toCharArray().forEach { c ->
            val count = counts.getOrDefault(c, 0)
            counts[c] = count + 1
        }
        val type = counts.values.max()

        return when (type) {
            5 -> 1
            4 -> 2
            3 -> if (counts.values.contains(2)) {
                // Full house
                3
            } else {
                // Three of a kind
                4
            }
            2 -> if (counts.filter { it.value == 2 }.size == 2) {
                // Two pair
                5
            } else {
                // One pair
                6
            }
            1 -> 7
            else -> {
                throw RuntimeException()
            }
        }
    }
}