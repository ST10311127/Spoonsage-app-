package com.spoonsage.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoonsage.app.data.Goals
import com.spoonsage.app.data.MealPlanEntry
import com.spoonsage.app.data.PersonalInfo
import com.spoonsage.app.data.ProgressLog
import com.spoonsage.app.data.RecipeRepository
import com.spoonsage.app.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Home dashboard (Section 3.1.3): welcome message, today's totals vs goals, AI banner, today's meals. */
class HomeDashboardViewModel(
    private val repository: RecipeRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    val personalInfo = preferences.personalInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PersonalInfo())

    val goals = preferences.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Goals())

    val todayProgress = repository.observeTodayProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null as ProgressLog?)

    val todayMeals = repository.getWeekPlan()
        .map { entries: List<MealPlanEntry> -> entries.filter { it.dayOfWeek == todayDayName() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isGuest by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            preferences.isGuest.collect { isGuest = it }
        }
    }

    companion object {
        fun todayDayName(): String {
            val sdf = SimpleDateFormat("EEEE", Locale.US)
            return sdf.format(Date())
        }
    }
}
