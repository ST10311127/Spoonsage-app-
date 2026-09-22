package com.spoonsage.app.data

/** Parsed result of an AI Coach reply: the chat text, plus an optional dish suggestion. */
data class CoachReply(val text: String, val suggestedTitle: String?)

/**
 * Parses the raw text Gemini returns for an AI Coach message.
 *
 * The system prompt asks the model to end its reply with a line in the
 * exact format `SUGGEST: <dish name>` when a concrete recipe would help.
 * This is pulled out into its own object (rather than left inline in
 * [RecipeRepository]) purely so it can be unit tested without needing to
 * mock Firebase/Gemini or a database.
 */
object CoachReplyParser {

    private const val SUGGEST_PREFIX = "SUGGEST:"

    fun parse(fullText: String): CoachReply {
        val suggestLine = fullText.lineSequence().firstOrNull { it.startsWith(SUGGEST_PREFIX) }
        val suggestedTitle = suggestLine?.removePrefix(SUGGEST_PREFIX)?.trim()?.ifEmpty { null }

        val replyText = fullText.lineSequence()
            .filterNot { it.startsWith(SUGGEST_PREFIX) }
            .joinToString("\n")
            .trim()
            .ifEmpty { fullText.trim() }

        return CoachReply(text = replyText, suggestedTitle = suggestedTitle)
    }
}
