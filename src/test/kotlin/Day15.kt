import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day15 : Helpers {

    @Test
    fun part1() {
        fun sumOfHashes(filename: String): Int {
            val line = stringsFromFile(filename).first()!!
            val r = line.split(",").map { step ->
                hash(step)
            }.sum()
            return r
        }
        assertEquals(hash("HASH"), 52)
        assertEquals(sumOfHashes("day15example.txt"), 1320)
        assertEquals(sumOfHashes("day15.txt"), 513172)
    }

    fun hash(step: String): Int {
        var v = 0
        val chars = step.toCharArray().iterator()
        while (chars.hasNext()) {
            val c = chars.next()
            val a = c.code
            v += a
            v *= 17
            v %= 256
        }
        return v
    }


}