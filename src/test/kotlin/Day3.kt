import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day3 : Helpers {

    @Test
    fun part1() {
        // Find the locations of all the numbers
        assertEquals(sumOfParts(stringsFromFile("day3example.txt")), 4361)
        assertEquals(sumOfParts(stringsFromFile("day3.txt")), 539433)
        println("Done")
    }

    @Test
    fun part2() {
        assertEquals(sumOfGearRatios(stringsFromFile("day3example.txt")), 467835)
        assertEquals(sumOfGearRatios(stringsFromFile("day3.txt")), 75847567)
    }
    private fun sumOfParts(lines: List<String>): Int {
        // Then filter for nearby symbols
        val parts = parts(lines, symbolsFrom(lines))
        return parts.sumOf { it.number }
    }

    private fun sumOfGearRatios(lines: List<String>): Int {
        val symbols = symbolsFrom(lines)
        // Only parts; not numbers
        val gears = symbols.filter { it.type == '*' }

        // Filter gears for those with a pair of numbers nearby
        val parts = parts(lines, symbols)
        return gears.sumOf { g ->
            // Find numbers touching the box enclosing the gear
            val ps = parts.filter { p ->
                p.y in (g.y - 1)..(g.y + 1) &&
                        !((p.end < g.x - 1) || (p.start > g.x + 1))
            }
            if (ps.size == 2) {
                ps.first().number * ps.last().number
            } else {
                0
            }
        }
    }

    private fun parts(lines: List<String>, symbolLocations: List<Symbol>): List<Part> {
        val numbers = extractNumbers(lines)
        return numbers.filter { n ->
            // Scan the enclosing range
            val y1 = listOf(n.y - 1, 0).max()
            val y2 = listOf(n.y + 1, lines.size - 1).min();
            val x1 = listOf(n.start - 1, 0).max()
            val x2 = listOf(n.end + 1, lines[0].length - 1).min();
            symbolLocations.any { s ->
                s.y in y1..y2 &&
                s.x >= x1 && s.x <= x2
            }
        }
    }

    private fun symbolsFrom(lines: List<String>): MutableList<Symbol> {
        val symbolLocations = mutableListOf<Symbol>()
        for (y in lines.indices)
            for (x in 0..<lines[0].length) {
                val c = lines[y][x]
                if (c !in '0'..'9' && c != '.') {
                    symbolLocations.add(Symbol(c, x, y))
                }
            }
        return symbolLocations
    }

    private fun extractNumbers(lines: List<String>): List<Part> {
        val numbers = mutableListOf<Part>()
        var y = 0
        lines.forEach { line ->
            var streak = ""
            var start = -1
            for (i in line.indices) {
                val c = line.get(i)
                val isDigit = c in '0'..'9'
                if (isDigit) {
                    if (start < 0) {
                        start = i
                    }
                    streak += c

                } else {
                    if (start >= 0) {
                        val end = i - 1
                        numbers.add(Part(streak.toInt(), y, start, end))
                        streak = ""
                        start = -1
                    }
                }
            }
            if (start >= 0) {
                val end = line.length - 1
                numbers.add(Part(streak.toInt(), y, start, end))
            }
            y += 1
        }
        return numbers.toList()
    }
}

data class Part(val number: Int, val y: Int, val start: Int, val end: Int)
data class Symbol(var type: Char, val x: Int, val y: Int)