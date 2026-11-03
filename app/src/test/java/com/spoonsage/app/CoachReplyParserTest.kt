package com.spoonsage.app

import com.spoonsage.app.util.CoachReplyParser
import com.spoonsage.app.util.RecipeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoachReplyParserTest {

    @Test
    fun extractsSuggestion() {
        val parsed = CoachReplyParser.parse("Try a protein-packed bowl tonight.\nSUGGEST: Chicken Quinoa Bowl")
        assertEquals("Try a protein-packed bowl tonight.", parsed.text)
        assertEquals("Chicken Quinoa Bowl", parsed.suggestion)
    }

    @Test
    fun noSuggestion() {
        val parsed = CoachReplyParser.parse("Drink a glass of water with every meal.")
        assertNull(parsed.suggestion)
        assertEquals("Drink a glass of water with every meal.", parsed.text)
    }

    @Test
    fun difficultyFromTime() {
        assertEquals(RecipeUtils.EASY, RecipeUtils.difficultyFor(15))
        assertEquals(RecipeUtils.MEDIUM, RecipeUtils.difficultyFor(30))
        assertEquals(RecipeUtils.HARD, RecipeUtils.difficultyFor(90))
    }

    @Test
    fun htmlInstructionsBecomeSteps() {
        val steps = RecipeUtils.stepsFromHtml("<ol><li>Boil water.</li><li>Add pasta.</li></ol>")
        assertEquals(listOf("Boil water.", "Add pasta."), steps)
    }

    @Test
    fun imageUrlFromFileName() {
        assertEquals("https://spoonacular.com/recipeImages/abc.jpg", RecipeUtils.imageUrl(1, "abc.jpg"))
    }
}
