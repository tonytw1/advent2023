import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day7 : Helpers {

    @Test
    fun part1() {
        val charStrengths = listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')
        assertEquals(totalWinnings(parseHands("day7example.txt"), charStrengths, jokers = false), 6440)
        assertEquals(totalWinnings(parseHands("day7.txt"), charStrengths, jokers = false), 247961593)
    }

    @Test
    fun part2() {
        val charStrengths = listOf('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J') // Is different in part2!
        assertEquals(totalWinnings(parseHands("day7example.txt"), charStrengths, jokers = true), 5905)
        assertEquals(totalWinnings(parseHands("day7.txt"), charStrengths, jokers = true), 248750699)
    }

    private fun totalWinnings(hands: List<Hand>, charStrengths: List<Char>, jokers: Boolean): Long {
        val sorted = HandsSorter.sortHands(hands, charStrengths, jokers)
        var t = 0L  // TODO reduction?
        for (i in 1..sorted.size) {
            val hand = sorted[i - 1]
            t += hand.rank * i
        }
        return t
    }

    private fun parseHands(filename: String): List<Hand> {
        return stringsFromFile(filename).map { line ->
            val cards = line.split(" ")[0]
            val rank = line.split(" ")[1].toInt()
            Hand(cards, rank)
        }
    }
}

object HandsSorter {

    class TypeComparator(val jokers: Boolean) : Comparator<Hand> {
        override fun compare(a: Hand, b: Hand): Int {
            return b.type(jokers).compareTo(a.type(jokers))
        }
    }

    class StrengthOfFirstUniqueChar(val charStrengths: List<Char>) : Comparator<Hand> {
        override fun compare(a: Hand, b: Hand): Int {
            for (i in 0..<a.cards.length) {
                val indexOfA = charStrengths.indexOf(a.cards.toCharArray()[i])
                val indexOfB = charStrengths.indexOf(b.cards.toCharArray()[i])
                val cmp = indexOfB.compareTo(indexOfA)
                if (cmp != 0) {
                    return cmp
                }
            }
            return 0
        }
    }

    fun sortHands(hands: List<Hand>, charStrengths: List<Char>, jokers: Boolean): List<Hand> {
        val handsSorterWithTieBreaking = object : Comparator<Hand> {
            val typeComparator = TypeComparator(jokers)
            val strengthOfFirstUniqueChar = StrengthOfFirstUniqueChar(charStrengths)
            override fun compare(a: Hand, b: Hand): Int {
                val typeCmp = typeComparator.compare(a, b)
                if (typeCmp != 0) {
                    return typeCmp
                }
                // Else have same type and we need to tie break
                return strengthOfFirstUniqueChar.compare(a, b)
            }
        }
        return hands.sortedWith(handsSorterWithTieBreaking)
    }

    private fun charCounts(cardsString: String): MutableMap<Char, Int> {
        val counts = mutableMapOf<Char, Int>()
        cardsString.toCharArray().forEach { c ->
            val count = counts.getOrDefault(c, 0)
            counts[c] = count + 1
        }
        return counts
    }

    fun typeOf(cardsString: String): Int {
        val counts = charCounts(cardsString)
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

data class Hand(val cards: String, val rank: Int) {

    fun best(): String {
        // Accounting for jokers try to find the strongest sub available
        // Foreach possible sub; create it than sort then
        if (!cards.contains('J')) {
            return cards
        }
        val subs = listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')
        val subbedHands = subs.map { c ->
            Hand(cards.replace('J', c), 0)
        }
        val sortHands = subbedHands.sortedBy { HandsSorter.typeOf(it.cards) }
        return sortHands.first().cards
    }

    fun type(jokers: Boolean): Int {
        if (jokers) {
            return HandsSorter.typeOf(best())
        }
        return HandsSorter.typeOf(cards)
    }

}