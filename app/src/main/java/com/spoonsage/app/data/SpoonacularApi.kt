package com.spoonsage.app.data

import com.spoonsage.app.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Minimal Spoonacular endpoints - just enough for ingredient search,
 * complex search with diet filters, and full recipe detail with steps.
 * Docs: https://spoonacular.com/food-api/docs
 */
interface SpoonacularApi {

    @GET("recipes/findByIngredients")
    suspend fun searchByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 15,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): List<IngredientSearchResult>

    @GET("recipes/complexSearch")
    suspend fun searchByQuery(
        @Query("query") query: String,
        @Query("diet") diet: String? = null,
        @Query("intolerances") intolerances: String? = null,
        @Query("number") number: Int = 15,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true,
        @Query("addRecipeNutrition") addRecipeNutrition: Boolean = true,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): ComplexSearchResponse

    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @Path("id") id: Int,
        @Query("includeNutrition") includeNutrition: Boolean = true,
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY
    ): RecipeInformation
}

data class IngredientSearchResult(
    val id: Int,
    val title: String,
    val image: String?
)

data class ComplexSearchResponse(
    val results: List<ComplexSearchResult>
)

data class ComplexSearchResult(
    val id: Int,
    val title: String,
    val image: String?,
    val readyInMinutes: Int? = null,
    val nutrition: NutritionInfo? = null
) {
    val calories: Int?
        get() = nutrition?.nutrients?.firstOrNull { it.name.equals("Calories", ignoreCase = true) }?.amount?.toInt()

    val difficulty: String
        get() = difficultyFor(readyInMinutes)
}

data class RecipeInformation(
    val id: Int,
    val title: String,
    val image: String?,
    val readyInMinutes: Int,
    val servings: Int,
    val extendedIngredients: List<ExtendedIngredient>?,
    val analyzedInstructions: List<AnalyzedInstruction>?,
    val nutrition: NutritionInfo? = null
)

data class NutritionInfo(
    val nutrients: List<Nutrient>?
)

data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String
)

/** Simple heuristic - Spoonacular has no explicit difficulty field. */
fun difficultyFor(readyInMinutes: Int?): String = when {
    readyInMinutes == null -> "Easy"
    readyInMinutes <= 20 -> "Easy"
    readyInMinutes <= 45 -> "Medium"
    else -> "Hard"
}

data class ExtendedIngredient(
    val original: String
)

data class AnalyzedInstruction(
    val steps: List<InstructionStep>
)

data class InstructionStep(
    val number: Int,
    val step: String
)
