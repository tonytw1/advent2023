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
        val charStrengths =
            listOf('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J') // Is different in part2!
        assertEquals(totalWinnings(parseHands("day7example.txt"), charStrengths, jokers = true), 5905)
        assertEquals(totalWinnings(parseHands("day7.txt"), charStrengths, jokers = true), 248750699)
    }

    private fun totalWinnings(hands: List<Hand>, charStrengths: List<Char>, jokers: Boolean): Long {
        return sortHands(hands, charStrengths, jokers).withIndex().sumOf { it ->
            it.value.rank.toLong() * (it.index + 1)
        }
    }

    private fun sortHands(hands: List<Hand>, charStrengths: List<Char>, jokers: Boolean): List<Hand> {
        val handsSorterWithTieBreaking = object : Comparator<Hand> {
            val typeComparator = TypeComparator(jokers)
            val strengthOfFirstUniqueChar = StrengthOfFirstUniqueChar(charStrengths)
            override fun compare(a: Hand, b: Hand): Int {
                val typeCmp = typeComparator.compare(a, b)
                if (typeCmp != 0) {
                    return typeCmp
                }
                // Else have same type and we need to tie break
                return strengthOfFirstUniqueChar.compare(a.cards, b.cards)
            }
        }
        return hands.sortedWith(handsSorterWithTieBreaking)
    }

    class TypeComparator(private val jokers: Boolean) : Comparator<Hand> {
        override fun compare(a: Hand, b: Hand): Int {
            return b.type(jokers).compareTo(a.type(jokers))
        }
    }

    class StrengthOfFirstUniqueChar(private val charStrengths: List<Char>) : Comparator<String> {
        override fun compare(a: String, b: String): Int {
            for (i in a.indices) {
                val indexOfA = charStrengths.indexOf(a.toCharArray()[i])
                val indexOfB = charStrengths.indexOf(b.toCharArray()[i])
                val cmp = indexOfB.compareTo(indexOfA)
                if (cmp != 0) {
                    return cmp
                }
            }
            return 0
        }
    }

    private fun parseHands(filename: String): List<Hand> {
        fun best(cards: String): String {
            // Accounting for jokers try to find the strongest sub available
            // Foreach possible sub; create it than sort then
            if (!cards.contains('J')) {
                return cards
            }
            val subs = listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2')
            val subbedHands = subs.map { c ->
                Hand(cards.replace('J', c), 0, "", 0,0)  // TODO this is abit nasty
            }
            return subbedHands.sortedBy { typeOf(it.cards) }.first().cards
        }

        return stringsFromFile(filename).map { line ->
            val cards = line.split(" ")[0]
            val rank = line.split(" ")[1].toInt()
            Hand(cards, rank, best(cards), typeOf(cards), typeOf(best(cards)))
        }
    }
}

fun typeOf(cardsString: String): Int {
    fun charCounts(cardsString: String): MutableMap<Char, Int> {
        val counts = mutableMapOf<Char, Int>()
        cardsString.toCharArray().forEach { c ->
            counts[c] = counts.getOrDefault(c, 0) + 1
        }
        return counts
    }

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

data class Hand(val cards: String, val rank: Int, val best: String, val type: Int, val bestType: Int) {
    fun type(jokers: Boolean): Int {
        if (jokers) {
            return bestType
        }
        return type
    }
}