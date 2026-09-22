package com.spoonsage.app.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Verifies the daily-logging streak rules in [StreakCalculator]
 * (Section 7: Progress Log) without needing a real Room database.
 */
class StreakCalculatorTest {

    private fun log(streak: Int) = ProgressLog(date = "irrelevant", streakCount = streak)

    @Test
    fun `first ever log starts the streak at 1`() {
        val streak = StreakCalculator.calculate(existingToday = null, yesterday = null)

        assertThat(streak).isEqualTo(1)
    }

    @Test
    fun `logging again the same day keeps the existing streak unchanged`() {
        val streak = StreakCalculator.calculate(existingToday = log(streak = 4), yesterday = log(streak = 3))

        assertThat(streak).isEqualTo(4)
    }

    @Test
    fun `logging the day after a previous entry extends the streak by one`() {
        val streak = StreakCalculator.calculate(existingToday = null, yesterday = log(streak = 5))

        assertThat(streak).isEqualTo(6)
    }

    @Test
    fun `logging with a gap since the last entry resets the streak to 1`() {
        // No entry for today or yesterday (e.g. the user skipped several days).
        val streak = StreakCalculator.calculate(existingToday = null, yesterday = null)

        assertThat(streak).isEqualTo(1)
    }
}
