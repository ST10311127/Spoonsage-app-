package com.spoonsage.app.data

/**
 * Works out the user's daily-logging streak (Section 7: Progress Log).
 *
 * Rules: logging again on a day that already has an entry keeps the same
 * streak; logging the day after a previous entry extends it by one;
 * logging with a gap (or for the first time ever) resets it to 1.
 *
 * Pulled out of [RecipeRepository.logProgress] as a pure function so the
 * streak logic can be unit tested without touching Room/a real database.
 */
object StreakCalculator {

    fun calculate(existingToday: ProgressLog?, yesterday: ProgressLog?): Int = when {
        existingToday != null -> existingToday.streakCount
        yesterday != null -> yesterday.streakCount + 1
        else -> 1
    }
}
