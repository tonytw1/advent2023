import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day1 : Helpers {

    @Test
    fun part1() {
        assertEquals(sumOfCalibrationValues(stringsFromFile("day1example.txt")), 142)
        assertEquals(sumOfCalibrationValues(stringsFromFile("day1.txt")), 54634)
    }

    @Test
    fun part2() {
        assertEquals(sumOfCalibrationValuesWithWords("day1example2.txt"), 281)
        assertEquals(sumOfCalibrationValuesWithWords("day1.txt"), 53855)
    }

    private val numberWords = arrayOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

    private fun sumOfCalibrationValues(lines: List<String>): Long = lines.sumOf { line -> calibrationValueOf(line) }

    private fun sumOfCalibrationValuesWithWords(filename: String): Long {
        // Replace words with digits then delegate to part1
        return sumOfCalibrationValues(stringsFromFile(filename).map { resolveWordsLeftToRight(it) })
    }

    private fun calibrationValueOf(line: String): Long {
        val digits = line.toCharArray().filter {
            it in '0'..'9'
        }.map { it.digitToInt().toLong() }
        return digits.first() * 10 + digits.last()
    }

    private fun resolveWordsLeftToRight(line: String): String {
        val output = StringBuffer()
        val chars = line.toCharArray()
        var i = 0
        while (i < chars.size) {
            // Check to see if any word starts at this index
            val substring = line.substring(i)
            val match = numberWords.find { word ->
                substring.startsWith(word)
            }
            if (match != null) {
                // Output this resolved word
                output.append(numberWords.indexOf(match) + 1)
                // Rewind enough for oneight to consume the second number! Don't just skip the word
            } else {
                output.append(chars[i])
            }
            i += 1
        }
        return output.toString()
    }




}