package com.spoonsage.app.util

object CoachReplyParser {

    data class Parsed(val text: String, val suggestion: String?)

    private val SUGGEST_REGEX = Regex("^\\s*SUGGEST:\\s*(.+)$", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))

    fun parse(raw: String): Parsed {
        val match = SUGGEST_REGEX.find(raw)
        val suggestion = match?.groupValues?.get(1)?.trim()?.trim('*', '"', '.')?.takeIf { it.isNotBlank() }
        val text = raw.replace(SUGGEST_REGEX, "").trim()
        return Parsed(text.ifBlank { raw.trim() }, suggestion)
    }
}
