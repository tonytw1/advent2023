import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.lang.RuntimeException

class Day19 : Helpers {

    @Test
    fun part1() {
        assertEquals(runAndCountAccepted(parseSystem("day19example.txt")), 19114)
        assertEquals(runAndCountAccepted(parseSystem("day19.txt")), 333263)
    }

    private fun runAndCountAccepted(system: System): Int {
        val workFlowsByName = system.workflows.associate {
            Pair(it.id, it)
        }

        fun exercise(part: Part, workflow: Workflow): Boolean {
            var w = workflow
            while (true) {
                val next = eval(w, part)
                if (next == "A") {
                    return true
                }
                if (next == "R") {
                    return false
                }
                w = workFlowsByName[next]!!
            }
        }

        // Drop each part into the first workflow
        val startWorkflow = workFlowsByName["in"]!!
        val acceptedParts = system.parts.filter { part ->
            exercise(part, startWorkflow)
        }

        return acceptedParts.map { part ->
            part.scores.values.sum()
        }.sum()
    }

    private fun eval(workflow: Workflow, part: Part): String {
        // Evaluate a part against a workflow return it's next destination
        // Evoke each rule in turn until this workflow returns a decision.
        val rules = workflow.rules.iterator()
        while (rules.hasNext()) {
            val rule: Rule = rules.next()
            val condition = rule.condition
            if (condition == null) {
                return rule.nextDestination
            } else {
                val v = part.scores[condition.field]!!
                val isMatch = if (condition.op == '<') {
                    v < condition.value
                } else {
                    v > condition.value
                }
                if (isMatch) {
                    return rule.nextDestination
                }
            }
        }
        throw RuntimeException()
    }

    private fun parseSystem(filename: String): System {
        val workflows = mutableListOf<Workflow>()
        val parts = mutableListOf<Part>()

        val lines = stringsFromFile(filename).iterator()
        var rulesCompleted = false
        while (lines.hasNext()) {
            val line = lines.next()
            if (line.isEmpty()) {
                rulesCompleted = true
            } else {
                if (!rulesCompleted) {
                    val split = line.split("{")
                    val id = split[0]
                    val rrs = split[1].dropLast(1).split(",").map { ruleString ->
                        val ps = ruleString.split(":")
                        val nextRule = ps.last()
                        val r = if (ps.size > 1) {
                            val condString = ps.first()
                            val fid = condString[0]
                            val op = condString[1]
                            val value = condString.drop(2).toInt()
                            Condition(fid, op, value)

                        } else {
                            null
                        }
                        Rule(r, nextRule)
                    }
                    Workflow(id, rrs)
                    workflows.add(Workflow(id, rrs))
                } else {
                    val fs = mutableMapOf<Char, Int>()
                    line.drop(1).dropLast(1).split(",").map { f ->
                        val split = f.split("=")
                        fs[split[0].first()] = split[1].toInt()
                    }
                    parts.add(Part(fs.toMap()))
                }
            }
        }
        return System(workflows, parts)
    }

    data class System(val workflows: List<Workflow>, val parts: List<Part>)
    data class Workflow(val id: String, val rules: List<Rule>)
    data class Rule(val condition: Condition?, val nextDestination: String)
    data class Condition(val field: Char, val op: Char, val value: Int)

    data class Part(val scores: Map<Char, Int>)

}