package com.spoonsage.app.util

object Options {
    const val GENDER_MALE = "Male"
    const val GENDER_FEMALE = "Female"
    const val GENDER_NONE = "Prefer not to say"
    val GENDERS = listOf(GENDER_MALE, GENDER_FEMALE, GENDER_NONE)

    const val ACTIVITY_LOW = "Low"
    const val ACTIVITY_MEDIUM = "Medium"
    const val ACTIVITY_HIGH = "High"
    val ACTIVITY_LEVELS = listOf(ACTIVITY_LOW, ACTIVITY_MEDIUM, ACTIVITY_HIGH)

    const val GOAL_LOSE_WEIGHT = "Lose weight"
    const val GOAL_BUILD_MUSCLE = "Build muscle"
    const val GOAL_EAT_HEALTHIER = "Eat healthier"
    const val GOAL_DRINK_WATER = "Drink more water"
    val GOALS = listOf(GOAL_LOSE_WEIGHT, GOAL_BUILD_MUSCLE, GOAL_EAT_HEALTHIER, GOAL_DRINK_WATER)

    val DIETS = listOf("Classic", "Vegetarian", "Vegan", "Pescatarian", "Keto", "Paleo", "Halal", "Kosher")

    val DISLIKES = listOf(
        "Avocado", "Beef", "Beets", "Bell peppers", "Blue cheese", "Brussels sprouts", "Cauliflower",
        "Eggplant", "Eggs", "Goat cheese", "Lamb", "Mushrooms", "Olives", "Pork", "Quinoa",
        "Shrimp", "Tofu", "Turnips"
    )

    val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val MEAL_TYPES = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    const val REMINDER_WATER = "Water"
    const val REMINDER_MEAL = "Meal"
    const val REMINDER_LOG = "Log progress"
    val REMINDER_TYPES = listOf(REMINDER_WATER, REMINDER_MEAL, REMINDER_LOG)

    fun spoonacularDiet(diet: String): String? = when (diet) {
        "Vegetarian" -> "vegetarian"
        "Vegan" -> "vegan"
        "Pescatarian" -> "pescetarian"
        "Keto" -> "ketogenic"
        "Paleo" -> "paleo"
        else -> null
    }
}
