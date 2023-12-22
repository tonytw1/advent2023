import org.testng.Assert.assertEquals
import org.testng.annotations.Test

private const val HIGH = "high"
private const val LOW = "low"

class Day20 : Helpers {

    @Test
    fun part1() {
        assertEquals(runSimulation(parseModulesFrom("day20example.txt")), 32000000)
        assertEquals(runSimulation(parseModulesFrom("day20example2.txt")), 11687500)
        assertEquals(runSimulation(parseModulesFrom("day20.txt")), 919383692)
    }

    @Test
    fun part2() {
        val filename = "day20.txt"
        val modules = parseModulesFrom(filename)

        // Have to manually inspect the graph to see that 4 loops converge before rx.
        // Use this to split the problem and find the periods of each loop.
        // Had to look this up on the reddit as a general approach wasn't working
        val aboveRx = modules.values.find { it.downStream.contains("rx") }!!
        // Have to assume that these all need all of these to send a high pulse at the same time
        val triggers = aboveRx.upStream

        val periods = triggers.map {
            extracted(it, parseModulesFrom(filename))          // Reset state!
        }
        assertEquals(leastCommonMultipleOf(periods), 247702167614647)
    }

    private fun extracted(first: String, modules: Map<String, Module>): Long {
        val queue = ArrayDeque<Message>()

        // Button is pressed and sends a low pulse to broadcast to set off the chain
        // We repeat this 1000 times
        val cycles = 10000000000L
        (1..cycles).forEach { c ->

            fun enqueueMessage(it: Message): Long {
                return if (it.from == first && it.value == HIGH) {
                    println(c)
                    c
                } else {
                    queue.add(it)
                    -1L
                }
            }

            fun drainQueue(): Long {
                while (queue.isNotEmpty()) {
                    val message = queue.removeFirst()
                    val module = modules[message.to]
                    module?.relay(message)?.forEach {
                        val enqueueMessage = enqueueMessage(it)
                        if (enqueueMessage > 0) {
                            return enqueueMessage
                        }

                    }
                }

                return -1L

            }
            enqueueMessage(Message("broadcaster", LOW, "button"))
            val j = drainQueue()
            if (j > 0) {
                return j
            }
        }
        return -1L
    }

    private fun runSimulation(modules: Map<String, Module>): Int {
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

    private fun primesUpToo(n: Long) = (2..n).filter { num -> (2..<num).none { num % it == 0L } }

    private fun leastCommonMultipleOf(periods: List<Long>): Long {
        // By table method https://en.wikipedia.org/wiki/Least_common_multiple
        val headers = mutableListOf<Long>()
        val table = periods.associateWith { n ->
            mutableListOf(n)
        }

        val primes = primesUpToo(periods.max())

        fun iterate(numbers: List<Long>, lowestPrime: Long): Pair<Long, List<Long>> {
            val primesToTry = primes.filter { it -> it >= lowestPrime }
            val primeToUse = primesToTry.find { prime ->
                numbers.any { it % prime == 0L }    // dividesAnyOfTheNumbersEvenly
            }!!
            val rows = numbers.map { n ->
                if (n % primeToUse == 0L) {
                    n / primeToUse
                } else {
                    n
                }
            }
            return Pair(primeToUse, rows)
        }

        var lowestPrime = 2L
        var isFinished = false
        while (!isFinished) {
            val rightMostColumn = periods.map { i ->
                table[i]!!.last()
            }
            val result = iterate(rightMostColumn, lowestPrime)

            // Append row to tables
            headers.add(result.first)
            periods.forEach { n ->
                table[n]!!.add(result.second.get(periods.indexOf(n)))
            }
            // Check if finished
            isFinished = result.second.all { it == 1L }
        }
        return headers.reduce { acc, it -> acc * it }
    }

}