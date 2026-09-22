package com.spoonsage.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.ProgressLog
import com.spoonsage.app.data.RecipeRepository
import com.spoonsage.app.data.UserPreferences
import kotlinx.coroutines.launch

data class WeeklyAverages(val avgCalories: Int, val avgProtein: Int, val avgWater: Float)

/** Progress / Summary tab (Section 3.1.7): weekly averages, goal bars, streaks + badges. */
class ProgressViewModel(
    private val repository: RecipeRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    val recentLogs = repository.getRecentProgress(7)
    val todayProgress = repository.observeTodayProgress()
    val goals = preferences.goals

    fun weeklyAverages(logs: List<ProgressLog>): WeeklyAverages {
        if (logs.isEmpty()) return WeeklyAverages(0, 0, 0f)
        val avgCal = logs.sumOf { it.caloriesConsumed } / logs.size
        val avgProtein = logs.sumOf { it.proteinConsumedG } / logs.size
        val avgWater = logs.sumOf { it.waterConsumedL.toDouble() }.toFloat() / logs.size
        return WeeklyAverages(avgCal, avgProtein, avgWater)
    }

    /** Badges unlocked at 3/7/14-day streaks - the "light gamification" from Section 2. */
    fun badgesFor(streak: Int): List<String> = buildList {
        if (streak >= 3) add("3-Day Streak")
        if (streak >= 7) add("7-Day Streak")
        if (streak >= 14) add("14-Day Streak")
    }

    fun logToday(calories: Int, protein: Int, water: Float) {
        viewModelScope.launch {
            repository.logProgress(calories, protein, water)
        }
    }
}
