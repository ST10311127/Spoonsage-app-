package com.spoonsage.app

import com.spoonsage.app.util.Options
import com.spoonsage.app.util.TargetCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetCalculatorTest {

    private fun calc(goal: String) =
        TargetCalculator.calculate(25, Options.GENDER_MALE, 180.0, 80.0, Options.ACTIVITY_MEDIUM, goal)

    @Test
    fun maintenanceMatchesMifflinStJeor() {

        assertEquals(2798, calc(Options.GOAL_EAT_HEALTHIER).calories)
    }

    @Test
    fun loseWeightIsLowerThanBuildMuscle() {
        assertTrue(calc(Options.GOAL_LOSE_WEIGHT).calories < calc(Options.GOAL_BUILD_MUSCLE).calories)
    }

    @Test
    fun buildMuscleNeedsMoreProtein() {
        assertEquals(160, calc(Options.GOAL_BUILD_MUSCLE).proteinG)
    }

    @Test
    fun waterGoalAddsHalfLitre() {
        val normal = calc(Options.GOAL_EAT_HEALTHIER).waterL
        val water = calc(Options.GOAL_DRINK_WATER).waterL
        assertEquals(0.5, water - normal, 0.11)
    }
}
