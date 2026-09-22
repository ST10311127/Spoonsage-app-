package com.spoonsage.app.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "SpoonSage/Repository"

/**
 * Single source of truth for recipes, the meal plan, the grocery list,
 * daily progress, reminders and the AI Coach chat. Follows the
 * "offline-first + repository" pattern recommended in the research report
 * and mirrors the design document's SpoonSage API responsibilities
 * (Figure 3) - just running on-device instead of on a Node/Express server.
 *
 * All network calls (Spoonacular, Gemini) are wrapped in try/catch and
 * returned as [Result] rather than thrown, so a lost connection or bad
 * response surfaces as an error state in the UI instead of crashing it.
 */
class RecipeRepository(
    private val api: SpoonacularApi,
    private val geminiClient: GeminiCoachClient,
    private val recipeDao: RecipeDao,
    private val mealPlanDao: MealPlanDao,
    private val groceryDao: GroceryDao,
    private val progressDao: ProgressDao,
    private val reminderDao: ReminderDao,
    private val aiCoachDao: AiCoachDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private fun today(): String = dateFormat.format(Date())

    // ---------- Discovery ----------

    /** Search by ingredients the user already has, BigOven/SideChef style. */
    suspend fun searchByIngredients(ingredients: List<String>): Result<List<ComplexSearchResult>> {
        return try {
            val joined = ingredients.joinToString(",")
            val results = api.searchByIngredients(joined).map {
                ComplexSearchResult(id = it.id, title = it.title, image = it.image)
            }
            Log.d(TAG, "searchByIngredients: ${results.size} results for [$joined]")
            Result.success(results)
        } catch (e: Exception) {
            Log.e(TAG, "searchByIngredients: failed for [$ingredients]", e)
            Result.failure(e)
        }
    }

    /** Search by name/cuisine, optionally filtered by the user's saved diet. Includes calories + difficulty (Section 3.1.4). */
    suspend fun searchByQuery(query: String, diet: String?, intolerances: String?): Result<List<ComplexSearchResult>> {
        return try {
            val response = api.searchByQuery(
                query = query,
                diet = diet?.takeIf { it.isNotBlank() && it != "Classic" },
                intolerances = intolerances?.takeIf { it.isNotBlank() }
            )
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads full recipe detail. Tries the network first and caches the
     * result in Room; falls back to the Room cache if offline so a
     * previously opened recipe can still be cooked without a connection.
     */
    suspend fun getRecipeDetail(id: Int): Result<Recipe> {
        return try {
            val info = api.getRecipeInformation(id)
            val calories = info.nutrition?.nutrients
                ?.firstOrNull { it.name.equals("Calories", ignoreCase = true) }
                ?.amount?.toInt() ?: 0
            val recipe = Recipe(
                id = info.id,
                title = info.title,
                imageUrl = info.image,
                readyInMinutes = info.readyInMinutes,
                servings = info.servings,
                ingredientsRaw = info.extendedIngredients?.joinToString("|") { it.original } ?: "",
                stepsRaw = info.analyzedInstructions
                    ?.firstOrNull()
                    ?.steps
                    ?.sortedBy { it.number }
                    ?.joinToString("|") { it.step }
                    ?: "",
                isFavorite = recipeDao.getById(id)?.isFavorite ?: false,
                calories = calories,
                difficulty = difficultyFor(info.readyInMinutes)
            )
            recipeDao.upsert(recipe)
            Result.success(recipe)
        } catch (e: Exception) {
            val cached = recipeDao.getById(id)
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    fun getFavorites(): Flow<List<Recipe>> = recipeDao.getFavorites()

    suspend fun toggleFavorite(recipe: Recipe) {
        recipeDao.update(recipe.copy(isFavorite = !recipe.isFavorite))
    }

    // ---------- Meal plan ----------

    fun getWeekPlan(): Flow<List<MealPlanEntry>> = mealPlanDao.getWeekPlan()

    suspend fun addToMealPlan(recipe: Recipe, dayOfWeek: String) {
        mealPlanDao.insert(
            MealPlanEntry(
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                recipeImageUrl = recipe.imageUrl,
                dayOfWeek = dayOfWeek
            )
        )
        // Automatically add this recipe's ingredients to the grocery list,
        // merging duplicates - Mealime-style automatic grocery list.
        recipe.ingredients.forEach { addGroceryItem(it) }
    }

    suspend fun removeFromMealPlan(entry: MealPlanEntry) = mealPlanDao.delete(entry)

    suspend fun clearMealPlan() = mealPlanDao.clearAll()

    // ---------- Grocery list ----------

    fun getGroceryList(): Flow<List<GroceryItem>> = groceryDao.getAll()

    suspend fun addGroceryItem(name: String) {
        val cleaned = name.trim()
        if (cleaned.isEmpty()) return
        val existing = groceryDao.findByName(cleaned)
        if (existing == null) {
            groceryDao.insert(GroceryItem(name = cleaned))
        }
        // If it already exists we simply don't duplicate it - this is the
        // "merge repeated ingredients" behaviour from Section 6, point 2.
    }

    suspend fun setGroceryChecked(item: GroceryItem, checked: Boolean) {
        groceryDao.update(item.copy(isChecked = checked))
    }

    suspend fun deleteGroceryItem(item: GroceryItem) = groceryDao.delete(item)

    suspend fun clearCheckedGroceryItems() = groceryDao.clearChecked()

    // ---------- Progress (Section 3.1.7 / Figure - Progress Log) ----------

    fun observeTodayProgress(): Flow<ProgressLog?> = progressDao.observeByDate(today())

    fun getRecentProgress(limit: Int = 7): Flow<List<ProgressLog>> = progressDao.getRecent(limit)

    /** Adds to today's totals and recalculates the daily streak. */
    suspend fun logProgress(caloriesDelta: Int, proteinDelta: Int, waterDelta: Float) {
        val date = today()
        val existing = progressDao.getByDate(date)
        val yesterday = dateFormat.format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)
        val yesterdayLog = progressDao.getByDate(yesterday)
        val streak = StreakCalculator.calculate(existing, yesterdayLog)
        val updated = ProgressLog(
            date = date,
            caloriesConsumed = (existing?.caloriesConsumed ?: 0) + caloriesDelta,
            proteinConsumedG = (existing?.proteinConsumedG ?: 0) + proteinDelta,
            waterConsumedL = (existing?.waterConsumedL ?: 0f) + waterDelta,
            streakCount = streak
        )
        progressDao.upsert(updated)
        Log.d(TAG, "logProgress: date=$date streak=$streak")
    }

    // ---------- Reminders ----------

    fun getReminders(): Flow<List<Reminder>> = reminderDao.getAll()

    suspend fun addReminder(type: String, time: String) =
        reminderDao.insert(Reminder(type = type, time = time))

    suspend fun toggleReminder(reminder: Reminder) =
        reminderDao.update(reminder.copy(isEnabled = !reminder.isEnabled))

    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)

    // ---------- AI Coach (Section 5.1 - Gemini via Firebase AI Logic) ----------

    fun getCoachMessages(): Flow<List<AiCoachMessage>> = aiCoachDao.getAll()

    /**
     * Sends the user's message plus a short context string to Gemini and
     * stores both the user message and the assistant's reply. Mirrors the
     * design document's POST /coach/message endpoint, just called directly
     * from the client instead of through a custom backend.
     */
    suspend fun sendCoachMessage(userText: String, contextSummary: String): Result<AiCoachMessage> {
        aiCoachDao.insert(AiCoachMessage(sender = "user", text = userText, timestamp = System.currentTimeMillis()))
        return try {
            val systemPrompt = """
                You are the SpoonSage AI Coach, a friendly nutrition assistant inside a meal-planning app.
                Context about the user: $contextSummary
                Keep replies short (2-4 sentences), practical and encouraging.
                If a specific dish would help, end your reply on its own final line in exactly this format:
                SUGGEST: <short recipe name>
                Only include a SUGGEST line when a concrete dish recommendation is genuinely useful.
            """.trimIndent()

            val fullText = geminiClient.sendMessage(systemPrompt, userText)
            val parsed = CoachReplyParser.parse(fullText)
            Log.d(TAG, "sendCoachMessage: reply received, suggestion=${parsed.suggestedTitle != null}")

            val assistantMessage = AiCoachMessage(
                sender = "assistant",
                text = parsed.text,
                timestamp = System.currentTimeMillis(),
                suggestedRecipeTitle = parsed.suggestedTitle
            )
            aiCoachDao.insert(assistantMessage)
            Result.success(assistantMessage)
        } catch (e: Exception) {
            Log.e(TAG, "sendCoachMessage: failed to reach Gemini", e)
            val errorMessage = AiCoachMessage(
                sender = "assistant",
                text = "Sorry, I couldn't reach the AI Coach right now (${e.message ?: "network error"}). Check your Firebase AI Logic setup and connection.",
                timestamp = System.currentTimeMillis()
            )
            aiCoachDao.insert(errorMessage)
            Result.failure(e)
        }
    }

    /** Looks up a real Spoonacular recipe matching an AI Coach suggestion, so it can be added to the plan. */
    suspend fun findRecipeForSuggestion(title: String): Result<ComplexSearchResult> {
        return searchByQuery(title, diet = null, intolerances = null).mapCatching {
            it.firstOrNull() ?: throw NoSuchElementException("No matching recipe found for \"$title\".")
        }
    }
}
