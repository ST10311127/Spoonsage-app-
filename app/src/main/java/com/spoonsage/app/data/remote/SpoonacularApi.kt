package com.spoonsage.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularApi {

    @GET("recipes/complexSearch")
    suspend fun complexSearch(
        @Query("query") query: String,
        @Query("diet") diet: String? = null,
        @Query("excludeIngredients") excludeIngredients: String? = null,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true,
        @Query("addRecipeNutrition") addRecipeNutrition: Boolean = true,
        @Query("number") number: Int = 20
    ): ComplexSearchResponse

    @GET("recipes/findByIngredients")
    suspend fun findByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 20,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true
    ): List<IngredientSearchResult>

    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @Path("id") id: Int,
        @Query("includeNutrition") includeNutrition: Boolean = true
    ): RecipeInformation
}

data class ComplexSearchResponse(
    val results: List<ComplexSearchResult> = emptyList(),
    val totalResults: Int = 0
)

data class ComplexSearchResult(
    val id: Int,
    val title: String,
    val image: String?,
    val readyInMinutes: Int?,
    val servings: Int?,
    val nutrition: NutritionInfo?,
    val extendedIngredients: List<ExtendedIngredient>?,
    val analyzedInstructions: List<AnalyzedInstruction>?
)

data class IngredientSearchResult(
    val id: Int,
    val title: String,
    val image: String?,
    val usedIngredientCount: Int,
    val missedIngredientCount: Int
)

data class RecipeInformation(
    val id: Int,
    val title: String,
    val image: String?,
    val readyInMinutes: Int?,
    val servings: Int?,
    val extendedIngredients: List<ExtendedIngredient>?,
    val analyzedInstructions: List<AnalyzedInstruction>?,
    val instructions: String?,
    val nutrition: NutritionInfo?
)

data class ExtendedIngredient(
    val name: String?,
    val original: String?,
    val amount: Double?,
    val unit: String?
)

data class AnalyzedInstruction(
    val steps: List<InstructionStep> = emptyList()
)

data class InstructionStep(
    val number: Int,
    val step: String
)

data class NutritionInfo(
    val nutrients: List<Nutrient> = emptyList()
)

data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String?
)

fun NutritionInfo?.amountOf(name: String): Int =
    this?.nutrients?.firstOrNull { it.name.equals(name, ignoreCase = true) }?.amount?.toInt() ?: 0
