package com.spoonsage.app.util

import kotlin.math.roundToInt

object TargetCalculator {

    data class Targets(val calories: Int, val proteinG: Int, val waterL: Double)

    fun calculate(
        age: Int,
        gender: String,
        heightCm: Double,
        weightKg: Double,
        activityLevel: String,
        goalType: String
    ): Targets {
        val genderOffset = when (gender) {
            Options.GENDER_MALE -> 5.0
            Options.GENDER_FEMALE -> -161.0
            else -> -78.0
        }
        val bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + genderOffset
        val activityFactor = when (activityLevel) {
            Options.ACTIVITY_HIGH -> 1.725
            Options.ACTIVITY_MEDIUM -> 1.55
            else -> 1.375
        }
        var calories = bmr * activityFactor
        calories += when (goalType) {
            Options.GOAL_LOSE_WEIGHT -> -500.0
            Options.GOAL_BUILD_MUSCLE -> 300.0
            else -> 0.0
        }
        val proteinPerKg = when (goalType) {
            Options.GOAL_BUILD_MUSCLE -> 2.0
            Options.GOAL_LOSE_WEIGHT -> 1.6
            else -> 1.2
        }
        var water = weightKg * 0.033
        if (goalType == Options.GOAL_DRINK_WATER) water += 0.5

        return Targets(
            calories = calories.roundToInt().coerceIn(1200, 4500),
            proteinG = (weightKg * proteinPerKg).roundToInt().coerceIn(40, 300),
            waterL = ((water * 10).roundToInt() / 10.0).coerceIn(1.5, 5.0)
        )
    }
}
