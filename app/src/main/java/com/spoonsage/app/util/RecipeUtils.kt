package com.spoonsage.app.util

object RecipeUtils {

    const val EASY = "Easy"
    const val MEDIUM = "Medium"
    const val HARD = "Hard"

    fun difficultyFor(readyInMinutes: Int): String = when {
        readyInMinutes <= 20 -> EASY
        readyInMinutes <= 45 -> MEDIUM
        else -> HARD
    }

    fun stepsFromHtml(html: String?): List<String> {
        if (html.isNullOrBlank()) return emptyList()
        return html
            .replace(Regex("<li>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .split(Regex("\\n|(?<=\\.)\\s+(?=[A-Z])"))
            .map { it.trim().replace(Regex("\\s+"), " ") }
            .filter { it.length > 3 }
    }

    fun imageUrl(id: Int, image: String?): String? = when {
        image?.startsWith("http") == true -> image
        !image.isNullOrBlank() -> "https://spoonacular.com/recipeImages/$image"
        id > 0 -> "https://spoonacular.com/recipeImages/$id-312x231.jpg"
        else -> null
    }
}
