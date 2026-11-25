package com.edumind.ai.util

import com.edumind.ai.data.AnswerSections

/**
 * Parses a raw AI answer into the 7-part structured format:
 * Direct Answer, Detailed Explanation, Step-by-Step Solution, Formulas,
 * Examples, Important Points, Final Conclusion. Falls back to the raw text
 * when headings are not present.
 */
object AnswerFormatter {

    private val sectionPatterns = listOf(
        "direct answer" to { s: AnswerSections, v: String -> s.copy(directAnswer = v) },
        "detailed explanation" to { s: AnswerSections, v: String -> s.copy(explanation = v) },
        "explanation" to { s: AnswerSections, v: String -> s.copy(explanation = v) },
        "step-by-step solution" to { s: AnswerSections, v: String -> s.copy(steps = v) },
        "step by step solution" to { s: AnswerSections, v: String -> s.copy(steps = v) },
        "solution" to { s: AnswerSections, v: String -> s.copy(steps = v) },
        "formulas" to { s: AnswerSections, v: String -> s.copy(formulas = v) },
        "formula" to { s: AnswerSections, v: String -> s.copy(formulas = v) },
        "examples" to { s: AnswerSections, v: String -> s.copy(examples = v) },
        "example" to { s: AnswerSections, v: String -> s.copy(examples = v) },
        "important points" to { s: AnswerSections, v: String -> s.copy(importantPoints = v) },
        "key points" to { s: AnswerSections, v: String -> s.copy(importantPoints = v) },
        "final conclusion" to { s: AnswerSections, v: String -> s.copy(conclusion = v) },
        "conclusion" to { s: AnswerSections, v: String -> s.copy(conclusion = v) },
    )

    fun parse(raw: String): AnswerSections {
        var sections = AnswerSections(raw = raw)
        // Split on numbered or bolded headings like "1. Direct Answer" or "**Direct Answer**".
        val headingRegex = Regex(
            """(?im)^\s*(?:\d+[.)]\s*)?(?:\*\*)?\s*(direct answer|detailed explanation|explanation|step-by-step solution|step by step solution|solution|formulas?|examples?|important points|key points|final conclusion|conclusion)\s*(?:\*\*)?\s*:?\s*$""",
        )
        val matches = headingRegex.findAll(raw).toList()
        if (matches.isEmpty()) return sections
        for (i in matches.indices) {
            val heading = matches[i].groupValues[1].lowercase()
            val start = matches[i].range.last + 1
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else raw.length
            val body = raw.substring(start, end).trim()
            val setter = sectionPatterns.firstOrNull { it.first == heading }?.second
                ?: sectionPatterns.firstOrNull { heading.startsWith(it.first) }?.second
            if (setter != null && body.isNotEmpty()) {
                sections = setter(sections, body)
            }
        }
        return sections
    }

    /** Ordered, non-empty sections for display. */
    fun displaySections(sections: AnswerSections): List<Pair<String, String>> {
        val ordered = listOf(
            "Direct Answer" to sections.directAnswer,
            "Detailed Explanation" to sections.explanation,
            "Step-by-Step Solution" to sections.steps,
            "Formulas" to sections.formulas,
            "Examples" to sections.examples,
            "Important Points" to sections.importantPoints,
            "Final Conclusion" to sections.conclusion,
        ).filter { it.second.isNotBlank() }
        return if (ordered.isEmpty() && sections.raw.isNotBlank()) {
            listOf("Answer" to sections.raw)
        } else {
            ordered
        }
    }
}
