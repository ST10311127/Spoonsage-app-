package com.spoonsage.app.data.repository

import com.spoonsage.app.data.local.ingredients
import com.spoonsage.app.data.local.steps
import android.util.Log
import androidx.lifecycle.LiveData
import com.spoonsage.app.data.UserPrefs
import com.spoonsage.app.data.firebase.GeminiCoachClient
import com.spoonsage.app.data.local.AiCoachMessage
import com.spoonsage.app.data.local.AppDatabase
import com.spoonsage.app.data.local.GroceryItem
import com.spoonsage.app.data.local.MealPlanEntry
import com.spoonsage.app.data.local.ProgressLog
import com.spoonsage.app.data.local.Recipe
import com.spoonsage.app.data.local.Reminder
import com.spoonsage.app.data.remote.ComplexSearchResult
import com.spoonsage.app.data.remote.RecipeInformation
import com.spoonsage.app.data.remote.SpoonacularApi
import com.spoonsage.app.data.remote.amountOf
import com.spoonsage.app.util.CoachReplyParser
import com.spoonsage.app.util.DateUtils
import com.spoonsage.app.util.Options
import com.spoonsage.app.util.RecipeUtils
import com.spoonsage.app.util.StreakCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SearchResult(val recipes: List<Recipe>, val fromCache: Boolean, val apiError: Boolean)

class SpoonSageRepository(
    private val db: AppDatabase,
    private val api: SpoonacularApi,
    private val prefs: UserPrefs,
    private val gemini: GeminiCoachClient,
    private val syncManager: SyncManager,
    private val appScope: CoroutineScope,
    private val isOnline: () -> Boolean
) {
    private val recipeDao = db.recipeDao()
    private val mealPlanDao = db.mealPlanDao()
    private val groceryDao = db.groceryDao()
    private val progressDao = db.progressDao()
    private val reminderDao = db.reminderDao()
    private val coachDao = db.aiCoachDao()

    fun requestSync() {
        if (!prefs.autoSync || prefs.isGuest) return
        if (!isOnline()) {
            Log.d(TAG, "Offline - change saved locally and will sync on reconnect")
            return
        }
        appScope.launch {
            try { syncManager.syncAll() } catch (e: Exception) { Log.w(TAG, "Background sync failed", e) }
        }
    }

    suspend fun searchByName(query: String): SearchResult {
        return try {
            val diet = Options.spoonacularDiet(prefs.diet)
            val exclude = prefs.dislikes.joinToString(",").ifBlank { null }
            val response = api.complexSearch(query = query, diet = diet, excludeIngredients = exclude)
            val recipes = response.results.map { it.toRecipe() }
            cacheKeepingFavourites(recipes)
            Log.d(TAG, "API search '$query' returned ${recipes.size} recipes")
            SearchResult(recipeDao.getByIds(recipes.map { it.id }).sortedBy { r -> recipes.indexOfFirst { it.id == r.id } }, fromCache = false, apiError = false)
        } catch (e: Exception) {
            Log.w(TAG, "API search failed, using cached recipes", e)
            SearchResult(recipeDao.searchCached(query), fromCache = true, apiError = true)
        }
    }

    suspend fun searchByIngredients(ingredientsCsv: String): SearchResult {
        return try {
            val results = api.findByIngredients(ingredientsCsv)
            val recipes = results.map {
                Recipe(
                    id = it.id, title = it.title, imageUrl = RecipeUtils.imageUrl(it.id, it.image),
                    readyInMinutes = 0, servings = 0, ingredientsRaw = "", stepsRaw = "",
                    calories = 0, difficulty = ""
                )
            }
            cacheKeepingFavourites(recipes)
            Log.d(TAG, "Ingredient search returned ${recipes.size} recipes")
            SearchResult(recipeDao.getByIds(recipes.map { it.id }).sortedBy { r -> recipes.indexOfFirst { it.id == r.id } }, fromCache = false, apiError = false)
        } catch (e: Exception) {
            Log.w(TAG, "Ingredient search failed, using cache", e)
            val first = ingredientsCsv.split(",").firstOrNull()?.trim().orEmpty()
            SearchResult(recipeDao.searchCachedByIngredient(first), fromCache = true, apiError = true)
        }
    }

    suspend fun getRecipeDetail(id: Int): Recipe? {
        val cached = recipeDao.getById(id)
        if (cached != null && cached.ingredients.isNotEmpty() && cached.steps.isNotEmpty()) return cached
        if (id < 0) return cached
        return try {
            val info = api.getRecipeInformation(id)
            val recipe = info.toRecipe().copy(isFavorite = cached?.isFavorite ?: false)
            recipeDao.upsert(recipe)
            Log.d(TAG, "Loaded recipe $id from API")
            recipe
        } catch (e: Exception) {
            Log.w(TAG, "Could not load recipe $id from API", e)
            cached
        }
    }

    suspend fun toggleFavorite(recipe: Recipe): Boolean {
        val newValue = !recipe.isFavorite
        recipeDao.setFavorite(recipe.id, newValue)
        return newValue
    }

    fun observeFavorites(): LiveData<List<Recipe>> = recipeDao.observeFavorites()

    suspend fun suggestedRecipes(): List<Recipe> = recipeDao.getSome(6)

    suspend fun findRecipeForSuggestion(title: String): Int? {
        recipeDao.searchCached(title).firstOrNull()?.let { return it.id }
        return try {
            api.complexSearch(query = title, number = 1).results.firstOrNull()?.let {
                val recipe = it.toRecipe()
                cacheKeepingFavourites(listOf(recipe))
                recipe.id
            }
        } catch (e: Exception) {
            Log.w(TAG, "Suggestion lookup failed", e)
            null
        }
    }

    private suspend fun cacheKeepingFavourites(recipes: List<Recipe>) {
        val existing = recipeDao.getByIds(recipes.map { it.id }).associateBy { it.id }
        recipeDao.upsertAll(recipes.map { new ->
            val old = existing[new.id]
            if (old == null) new else new.copy(
                isFavorite = old.isFavorite,

                ingredientsRaw = new.ingredientsRaw.ifBlank { old.ingredientsRaw },
                stepsRaw = new.stepsRaw.ifBlank { old.stepsRaw },
                calories = if (new.calories == 0) old.calories else new.calories,
                readyInMinutes = if (new.readyInMinutes == 0) old.readyInMinutes else new.readyInMinutes,
                servings = if (new.servings == 0) old.servings else new.servings,
                difficulty = new.difficulty.ifBlank { old.difficulty }
            )
        })
    }

    fun observeMealPlan(): LiveData<List<MealPlanEntry>> = mealPlanDao.observeAll()
    fun observeMealsForDay(day: String): LiveData<List<MealPlanEntry>> = mealPlanDao.observeForDay(day)

    suspend fun addToMealPlan(recipe: Recipe, day: String, mealType: String) {
        mealPlanDao.upsert(
            MealPlanEntry(
                recipeId = recipe.id, recipeTitle = recipe.title, recipeImageUrl = recipe.imageUrl,
                dayOfWeek = day, mealType = mealType, calories = recipe.calories
            )
        )
        recipe.ingredients.forEach { addGroceryItem(it, syncNow = false) }
        Log.d(TAG, "Added '${recipe.title}' to $day $mealType with ${recipe.ingredients.size} ingredients")
        requestSync()
    }

    suspend fun removeFromMealPlan(entry: MealPlanEntry) {
        if (prefs.isGuest) mealPlanDao.hardDelete(entry.id) else mealPlanDao.markDeleted(entry.id)
        requestSync()
    }

    suspend fun clearWeek() {
        if (prefs.isGuest) mealPlanDao.clear() else mealPlanDao.markAllDeleted()
        requestSync()
    }

    fun observeGroceries(): LiveData<List<GroceryItem>> = groceryDao.observeAll()

    suspend fun addGroceryItem(name: String, syncNow: Boolean = true): Boolean {
        val clean = name.trim()
        if (clean.isBlank() || groceryDao.findByName(clean) != null) return false
        groceryDao.upsert(GroceryItem(name = clean))
        if (syncNow) requestSync()
        return true
    }

    suspend fun toggleGrocery(item: GroceryItem) {
        groceryDao.upsert(item.copy(isChecked = !item.isChecked, synced = false, updatedAt = System.currentTimeMillis()))
        requestSync()
    }

    suspend fun deleteGrocery(item: GroceryItem) {
        if (prefs.isGuest) groceryDao.hardDelete(item.id) else groceryDao.markDeleted(item.id)
        requestSync()
    }

    suspend fun clearCheckedGroceries() {
        groceryDao.getChecked().forEach {
            if (prefs.isGuest) groceryDao.hardDelete(it.id) else groceryDao.markDeleted(it.id)
        }
        requestSync()
    }

    fun observeToday(): LiveData<ProgressLog?> = progressDao.observeByDate(DateUtils.today())
    fun observeRecentLogs(days: Int): LiveData<List<ProgressLog>> = progressDao.observeRecent(days)

    suspend fun logToday(calories: Int, proteinG: Int, waterL: Double) {
        val today = DateUtils.today()
        val existing = progressDao.getByDate(today)
        val previous = progressDao.getLatestBefore(today)
        val streak = StreakCalculator.nextStreak(
            previousDate = previous?.date?.let { LocalDate.parse(it) },
            previousStreak = previous?.streakCount ?: 0,
            today = LocalDate.parse(today)
        )
        val updated = ProgressLog(
            date = today,
            caloriesConsumed = (existing?.caloriesConsumed ?: 0) + calories,
            proteinConsumedG = (existing?.proteinConsumedG ?: 0) + proteinG,
            waterConsumedL = (existing?.waterConsumedL ?: 0.0) + waterL,
            streakCount = streak,
            synced = false
        )
        progressDao.upsert(updated)
        Log.d(TAG, "Logged progress for $today: $updated")
        requestSync()
    }

    fun observeReminders(): LiveData<List<Reminder>> = reminderDao.observeAll()
    suspend fun addReminder(type: String, time: String): Reminder {
        val r = Reminder(type = type, time = time)
        val id = reminderDao.insert(r)
        return r.copy(id = id.toInt())
    }
    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)
    suspend fun allReminders(): List<Reminder> = reminderDao.getAll()

    fun observeCoachMessages(): LiveData<List<AiCoachMessage>> = coachDao.observeAll()

    suspend fun sendCoachMessage(text: String, offlineReply: String, errorReply: String): Boolean {
        val history = coachDao.getRecent(10).reversed()
        coachDao.insert(AiCoachMessage(sender = AiCoachMessage.SENDER_USER, text = text))
        if (!isOnline()) {
            coachDao.insert(AiCoachMessage(sender = AiCoachMessage.SENDER_COACH, text = offlineReply))
            return false
        }
        return try {
            val raw = gemini.ask(prefs.coachContext(), history, text)
            val parsed = CoachReplyParser.parse(raw)
            coachDao.insert(
                AiCoachMessage(
                    sender = AiCoachMessage.SENDER_COACH,
                    text = parsed.text,
                    suggestedRecipeTitle = parsed.suggestion
                )
            )
            Log.d(TAG, "Coach replied (suggestion=${parsed.suggestion})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "AI Coach request failed", e)
            coachDao.insert(AiCoachMessage(sender = AiCoachMessage.SENDER_COACH, text = errorReply))
            false
        }
    }

    suspend fun clearCoachChat() = coachDao.clear()

    suspend fun clearUserData() {
        mealPlanDao.clear()
        groceryDao.clear()
        progressDao.clear()
        coachDao.clear()
        reminderDao.clear()
    }

    suspend fun prepopulateSampleData() {
        if (prefs.sampleDataLoaded && recipeDao.count() > 0) return
        recipeDao.upsertAll(SampleData.recipes)
        prefs.sampleDataLoaded = true
        Log.d(TAG, "Sample recipes added")
    }

    private fun ComplexSearchResult.toRecipe(): Recipe {
        val minutes = readyInMinutes ?: 0
        return Recipe(
            id = id,
            title = title,
            imageUrl = RecipeUtils.imageUrl(id, image),
            readyInMinutes = minutes,
            servings = servings ?: 0,
            ingredientsRaw = extendedIngredients.orEmpty().mapNotNull { it.original ?: it.name }.joinToString("\n"),
            stepsRaw = analyzedInstructions.orEmpty().flatMap { it.steps }.joinToString("\n") { it.step },
            calories = nutrition.amountOf("Calories"),
            protein = nutrition.amountOf("Protein"),
            difficulty = if (minutes > 0) RecipeUtils.difficultyFor(minutes) else ""
        )
    }

    private fun RecipeInformation.toRecipe(): Recipe {
        val minutes = readyInMinutes ?: 0
        var steps = analyzedInstructions.orEmpty().flatMap { it.steps }.map { it.step }
        if (steps.isEmpty()) steps = RecipeUtils.stepsFromHtml(instructions)
        return Recipe(
            id = id,
            title = title,
            imageUrl = RecipeUtils.imageUrl(id, image),
            readyInMinutes = minutes,
            servings = servings ?: 0,
            ingredientsRaw = extendedIngredients.orEmpty().mapNotNull { it.original ?: it.name }.distinct().joinToString("\n"),
            stepsRaw = steps.joinToString("\n"),
            calories = nutrition.amountOf("Calories"),
            protein = nutrition.amountOf("Protein"),
            difficulty = RecipeUtils.difficultyFor(minutes)
        )
    }

    companion object {
        private const val TAG = "SpoonSageRepository"
    }
}
