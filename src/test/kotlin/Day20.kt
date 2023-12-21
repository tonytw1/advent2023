import org.testng.Assert.assertEquals
import org.testng.annotations.Test

private const val HIGH = "high"
private const val LOW = "low"

class Day20 : Helpers {

    @Test
    fun part1() {
        assertEquals(runSimulation("day20example.txt"), 32000000)
        assertEquals(runSimulation("day20example2.txt"), 11687500)
        assertEquals(runSimulation("day20example2.txt"), 919383692)
    }

    private fun runSimulation(filename: String): Int {
        val modules = parseModulesFrom(filename)

        var lowCount = 0
        var highCount = 0
        val queue = ArrayDeque<Message>()

        fun enqueueMessage(it: Message) {
            if (it.value == HIGH) {
                highCount += 1
            } else {
                lowCount += 1
            }
            queue.add(it)
        }

        fun drainQueue() {
            while (queue.isNotEmpty()) {
                val message = queue.removeFirst()
                val module = modules[message.to]
                module?.relay(message)?.forEach { enqueueMessage(it) }
            }
        }

        // Button is pressed and sends a low pulse to broadcast to set off the chain
        // We repeat this 1000 times
        val cycles = 1000L
        (1..cycles).forEach { _ ->
            enqueueMessage(Message("broadcaster", LOW, "button"))
            drainQueue()
        }

        return lowCount * highCount
    }

    private fun parseModulesFrom(filename: String): Map<String, Module> {
        val withoutUpstream = stringsFromFile(filename).map { line ->
            val split = line.split(" ")
            val typeAndName = split[0]
            println(line)
            val s = line.split(" -> ")[1]
            val downStream = s.split(", ")
            if (typeAndName.startsWith('%') || typeAndName.startsWith('&')) {
                Module(typeAndName.drop(1), typeAndName[0], downStream, false, mutableMapOf(), emptyList())
            } else {
                Module(typeAndName, 'B', downStream, false, mutableMapOf(), emptyList())
            }
        }.associateBy { it.id }

        // Resolve upstreams
        return withoutUpstream.values.map { module ->
            val upStream = withoutUpstream.values.filter { it.downStream.contains(module.id) }.map { it.id }
            module.copy(upStream = upStream)
        }.associateBy { it.id }
    }

    data class Message(val to: String, val value: String, val from: String)

    data class Module(
        val id: String,
        val type: Char,
        val downStream: List<String>,
        var state: Boolean,
        val lastReceived: MutableMap<String, String>,
        val upStream: List<String>
    ) {

        fun relay(message: Message): List<Message> {
            when (type) {
                'B' -> {
                    return downStream.map { dsm ->
                        Message(dsm, message.value, this.id)
                    }
                }

                '%' -> {
                    if (message.value == HIGH) {
                        return emptyList()
                    }
                    //println("$id $state")
                    return if (!state) {
                        state = true
                        downStream.map { dsm ->
                            Message(dsm, HIGH, this.id)
                        }
                    } else {
                        state = false
                        downStream.map { dsm ->
                            Message(dsm, LOW, this.id)
                        }
                    }
                }

                '&' -> {
                    lastReceived[message.from] = message.value
                    val lastReceivedStates = upStream.map {
                        lastReceived.getOrDefault(it, LOW)
                    }
                    val toSend = if (lastReceivedStates.all { it == HIGH }) {
                        LOW
                    } else {
                        HIGH
                    }
                    return downStream.map { dsm ->
                        Message(dsm, toSend, this.id)
                    }
                }
            }
            return emptyList()
        }
    }

}