package com.spoonsage.app

import com.spoonsage.app.util.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {
    private val today = LocalDate.of(2026, 11, 3)

    @Test fun firstEverLog() = assertEquals(1, StreakCalculator.nextStreak(null, 0, today))
    @Test fun consecutiveDay() = assertEquals(5, StreakCalculator.nextStreak(today.minusDays(1), 4, today))
    @Test fun missedADayResets() = assertEquals(1, StreakCalculator.nextStreak(today.minusDays(2), 9, today))
    @Test fun badges() = assertEquals(listOf(3, 7), StreakCalculator.badgesFor(8))
}
