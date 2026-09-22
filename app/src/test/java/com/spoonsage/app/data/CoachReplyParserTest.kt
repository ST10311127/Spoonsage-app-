package com.spoonsage.app.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Verifies that [CoachReplyParser] correctly separates the chat text from
 * an optional "SUGGEST: <dish>" line in Gemini's raw reply.
 */
class CoachReplyParserTest {

    @Test
    fun `reply with a suggestion line extracts the dish name and strips it from the text`() {
        val raw = "Great goal! Protein-rich dinners help with that.\nSUGGEST: Grilled Chicken Salad"

        val result = CoachReplyParser.parse(raw)

        assertThat(result.suggestedTitle).isEqualTo("Grilled Chicken Salad")
        assertThat(result.text).isEqualTo("Great goal! Protein-rich dinners help with that.")
    }

    @Test
    fun `reply with no suggestion line returns the full text and a null suggestion`() {
        val raw = "Drinking more water throughout the day is a great start!"

        val result = CoachReplyParser.parse(raw)

        assertThat(result.suggestedTitle).isNull()
        assertThat(result.text).isEqualTo(raw)
    }

    @Test
    fun `blank suggestion line is treated as no suggestion`() {
        val raw = "Here's some general advice.\nSUGGEST:   "

        val result = CoachReplyParser.parse(raw)

        assertThat(result.suggestedTitle).isNull()
    }

    @Test
    fun `multi-line reply keeps line breaks in the text portion`() {
        val raw = "Line one.\nLine two.\nSUGGEST: Veggie Stir Fry"

        val result = CoachReplyParser.parse(raw)

        assertThat(result.text).isEqualTo("Line one.\nLine two.")
        assertThat(result.suggestedTitle).isEqualTo("Veggie Stir Fry")
    }
}
