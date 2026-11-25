package com.edumind.ai.data

/** A single message in an AI chat conversation. */
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val role: Role,
    val text: String,
    val timestamp: Long,
    val subject: String? = null,
) {
    enum class Role { USER, AI }
}

/** A saved AI answer (persisted locally). */
data class SavedAnswer(
    val id: String,
    val question: String,
    val answer: String,
    val subject: String?,
    val source: String, // "ask" | "tool" | "scanner" | "document"
    val timestamp: Long,
    val isFavorite: Boolean = false,
)

/** A study planner task. */
data class PlannerTask(
    val id: String,
    val title: String,
    val subject: String,
    val dateMillis: Long,
    val durationMinutes: Int,
    val done: Boolean = false,
)

/** The structured answer format every AI response must follow. */
data class AnswerSections(
    val directAnswer: String = "",
    val explanation: String = "",
    val steps: String = "",
    val formulas: String = "",
    val examples: String = "",
    val importantPoints: String = "",
    val conclusion: String = "",
    val raw: String = "",
)

/** Result of an AI request from the UI layer. */
sealed class AiResult {
    data class Success(val answer: String, val subject: String? = null) : AiResult()
    data class NotConfigured(val message: String) : AiResult()
    data class Error(val message: String) : AiResult()
}
