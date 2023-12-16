import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class Day15 : Helpers {

    @Test
    fun part1() {
        fun sumOfHashes(filename: String): Int {
            return parseStepsFrom(filename).sumOf { step ->
                hash(step)
            }
        }
        assertEquals(hash("HASH"), 52)
        assertEquals(sumOfHashes("day15example.txt"), 1320)
        assertEquals(sumOfHashes("day15.txt"), 513172)
    }

    @Test
    fun part2() {
        assertEquals(focalPowerOf(parseStepsFrom("day15example.txt")), 145)
        assertEquals(focalPowerOf(parseStepsFrom("day15.txt")), 237806)
    }

    private fun focalPowerOf(steps: List<String>): Int {
        // Boxes to lens
        val boxes = mutableMapOf<Int, MutableList<String>>()
        val lensByLabel = mutableMapOf<String, Int>()

        steps.forEach { step ->
            // Convert the step into instructions
            val split = step.split('-', '=')
            val label = split[0]
            val operation = step.substring(label.length, label.length + 1)

            val box = boxes.getOrDefault(hash(label), mutableListOf())

            if (operation == "=") {
                val focalLengthToInsert = split[1].toInt()

                if (box.contains(label)) {
                    // If this label already exists replace the registered focal length
                    // This probably only works because labels are unique at a point in time; they could have made us scope this to boxes
                    lensByLabel[label] = focalLengthToInsert

                } else {
                    box.add(label)
                    boxes[hash(label)] = box
                    lensByLabel[label] = focalLengthToInsert
                }
            }
            if (operation == "-") {
                box.removeAll { it == label }
            }
        }

        return boxes.map { box ->
            box.value.withIndex().sumOf { it ->
                (1 + box.key) * (it.index + 1) * (lensByLabel[it.value]!!)
            }
        }.sum()
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

    fun parseStepsFrom(filename: String): List<String> {
        return stringsFromFile(filename).first()!!.split(",")
    }

}