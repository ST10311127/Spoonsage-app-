package com.spoonsage.app.util

import java.time.LocalDate

object StreakCalculator {

    fun nextStreak(previousDate: LocalDate?, previousStreak: Int, today: LocalDate): Int = when {
        previousDate == null -> 1
        previousDate == today -> previousStreak.coerceAtLeast(1)
        previousDate == today.minusDays(1) -> previousStreak + 1
        else -> 1
    }

    fun badgesFor(streak: Int): List<Int> = listOf(3, 7, 14).filter { streak >= it }
}
