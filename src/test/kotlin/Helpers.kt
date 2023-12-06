import java.io.File

interface Helpers {
    fun stringsFromFile(filename: String): List<String> {
        val input = File(javaClass.classLoader.getResource(filename).toURI())
        return input.readLines()
    }

    fun intsFromFile(filename: String): List<Int> {
        return stringsFromFile(filename).map { it.toInt() }
    }

    fun longsFromFile(filename: String): List<Long> {
        return stringsFromFile(filename).map { it.toLong() }
    }

    val multipleBlankSpace: Regex
        get() = Regex("\\s+")

}