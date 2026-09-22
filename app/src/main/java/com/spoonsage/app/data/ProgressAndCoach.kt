package com.spoonsage.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One day's logged nutrition (Section 7: Progress Log). date is stored as
 * "yyyy-MM-dd" so there is at most one row per day - logging again the same
 * day just updates it.
 */
@Entity(tableName = "progress_logs")
data class ProgressLog(
    @PrimaryKey val date: String,
    val caloriesConsumed: Int = 0,
    val proteinConsumedG: Int = 0,
    val waterConsumedL: Float = 0f,
    val streakCount: Int = 0
)

/** A meal/water/weigh-in reminder toggle (Section 7: Reminder). */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "meal" | "water" | "weigh-in"
    val time: String, // "HH:mm"
    val isEnabled: Boolean = true
)

/** One message in the AI Coach chat history (Section 7: AI Coach Message). */
@Entity(tableName = "ai_coach_messages")
data class AiCoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" | "assistant"
    val text: String,
    val timestamp: Long,
    /** Optional recipe title suggested by the assistant, addable to the plan. */
    val suggestedRecipeTitle: String? = null
)
