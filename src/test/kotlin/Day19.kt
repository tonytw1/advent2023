import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.lang.RuntimeException

class Day19 : Helpers {

    @Test
    fun part1() {
        assertEquals(runAndCountAccepted(parseSystem("day19example.txt")), 19114)
        assertEquals(runAndCountAccepted(parseSystem("day19.txt")), 333263)
    }

    @Test
    fun part2() {
        assertEquals(numberOfValidParts(parseSystem("day19example.txt")), 167409079868000L)
        assertEquals(numberOfValidParts(parseSystem("day19.txt")), 130745440937650L)
    }

    private fun runAndCountAccepted(system: System): Int {
        val workFlowsByName = system.workflows.associateBy { it.id }

        fun eval(workflow: Workflow, part: Part): String {
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

    private fun numberOfValidParts(system: System): Long {
        // Seems like the conditions ang the max value of 4000 probably fence off the valid regions in 4d.
        // If they don't overlap then we're looking for the sum of the volumes.

        // We can discard the parts list; this is all about the workflows
        val workflows = system.workflows

        val workFlowsByName = workflows.associateBy { it.id }
        // We still have to start at the 'in' workflow
        val startWorkflow = workflows.find { it.id == "in" }!!

        // DFS Explore the tree of conditions; it doesn't look very deep
        // Collect chains of conditions followed and the outcome; we may have to filter out invalid paths later
        fun visit(
            workflowName: String,
            boundaryConditions: List<Condition>,
            path: List<String>
        ): List<Pair<List<Condition>, Boolean>> {
            val p = path.toMutableList()
            p.add(workflowName)
            if (workflowName == "A") {
                return listOf(Pair(boundaryConditions, true))
            } else if (workflowName == "R") {
                return listOf(Pair(boundaryConditions, false))
            }

            // Foreach rule in this workflow's chain we can either follow it, or move to the next rule in the chain.
            // Following imposes a boundary on the values; moving to the next rule opposes the opposite condition on the values!
            val w = workFlowsByName[workflowName]!!

            val results = mutableListOf<Pair<List<Condition>, Boolean>>()
            val bc = boundaryConditions.toMutableList()

            w.rules.forEach { rule: Rule ->
                if (rule.condition == null) {
                    // A rule with no condition tells us nothing about the set of allowed values.
                    // Follow it and collect it's results
                    results.addAll(visit(rule.nextDestination, bc, p))

                } else {
                    // Accept this condition and branch
                    val u = bc.toMutableList()
                    u.add(rule.condition)
                    results.addAll(visit(rule.nextDestination, u, p))

                    // If we didn't follow this rules condition then we must have narrowed the space by the opposite condition!
                    bc.add(rule.condition.invert())
                }
            }

            return results
        }

        val ranges = visit("in", emptyList(), emptyList())

        val acceptedRanges = ranges.filter { it.second }.map { it.first }

        // Is it as simple as just slicing down to max and mins to get the sides of the enclosed volume?
        // Hope that one of these overlap!

        fun boundaryConditionsToBoundingBoxSideLengths(conditions: List<Condition>): List<Long> {
            val minValue = 1L
            val maxValue = 4000L
            val dims = listOf('x', 'm', 'a', 's')
            val map = dims.map { d ->
                val effectingTheDim = conditions.filter { it.field == d }
                val dimLeft =
                    listOf(effectingTheDim.filter { it.op == '>' }.map { it.value + 1 }, listOf(minValue)).flatten()
                        .max()
                val dimRight =
                    listOf(effectingTheDim.filter { it.op == '<' }.map { it.value - 1 }, listOf(maxValue)).flatten()
                        .min()
                val l = dimRight - dimLeft
                val dimLength = if (l == 0L) {
                    0 + 1
                } else {
                    l + 1
                }
                dimLength
            }
            return map
        }

        val boundingBoxSizes = acceptedRanges.map { conditions ->
            boundaryConditionsToBoundingBoxSideLengths(conditions)
        }

        // Sum the volumes of the enclosed 4d boxes to fing number of valid parts
        return boundingBoxSizes.map { r ->
            r.reduce { acc, it -> acc * it }
        }.sum();
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
                            Condition(ps.first()[0], ps.first()[1], ps.first().drop(2).toLong())

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
    data class Condition(val field: Char, val op: Char, val value: Long) {
        // null conditions can be used anywhere in the chain!
        fun invert(): Condition {
            return if (op == '>') {
                // > 3 is an 4,5,6... so inverse is 3,2,1 hence '> 3' -> '< 4'
                this.copy(op = '<', value = this.value + 1, field = this.field)
            } else {
                // < 3 is ...1,2 so inverse is > 2
                this.copy(op = '>', value = this.value - 1, field = this.field)
            }
        }
    }

    data class Part(val scores: Map<Char, Int>)

}