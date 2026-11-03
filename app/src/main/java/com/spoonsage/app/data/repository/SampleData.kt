package com.spoonsage.app.data.repository

import com.spoonsage.app.data.local.Recipe

object SampleData {
    val recipes = listOf(
        Recipe(
            id = -1, title = "Grilled Chicken Salad", imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80", readyInMinutes = 25, servings = 2,
            ingredientsRaw = "2 chicken breasts\n1 head lettuce\n1 cucumber\n2 tomatoes\n1 tbsp olive oil\n1 lemon\nSalt and pepper",
            stepsRaw = "Season the chicken with salt, pepper and lemon juice.\nGrill the chicken for 6-7 minutes per side until cooked through.\nChop the lettuce, cucumber and tomatoes.\nSlice the chicken and place it on the salad.\nDrizzle with olive oil and lemon juice and serve.",
            calories = 420, protein = 45, difficulty = "Medium"
        ),
        Recipe(
            id = -2, title = "Oats with Fruits", imageUrl = "https://images.unsplash.com/photo-1517673400267-0251440c45dc?auto=format&fit=crop&w=600&q=80", readyInMinutes = 10, servings = 1,
            ingredientsRaw = "1/2 cup rolled oats\n1 cup milk\n1 banana\n1/2 cup berries\n1 tsp honey",
            stepsRaw = "Bring the milk to a gentle boil.\nStir in the oats and cook for 5 minutes, stirring often.\nSlice the banana.\nTop the oats with banana, berries and honey.",
            calories = 350, protein = 12, difficulty = "Easy"
        ),
        Recipe(
            id = -3, title = "Berry Smoothie", imageUrl = "https://images.unsplash.com/photo-1553530666-ba11a7da3888?auto=format&fit=crop&w=600&q=80", readyInMinutes = 5, servings = 1,
            ingredientsRaw = "1 cup mixed berries\n1 banana\n1 cup plain yoghurt\n1/2 cup milk",
            stepsRaw = "Add all the ingredients to a blender.\nBlend until smooth.\nPour into a glass and enjoy straight away.",
            calories = 280, protein = 11, difficulty = "Easy"
        ),
        Recipe(
            id = -4, title = "Quinoa & Veg Bowl", imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=600&q=80", readyInMinutes = 30, servings = 2,
            ingredientsRaw = "1 cup quinoa\n1 courgette\n1 red pepper\n1 cup chickpeas\n1 tbsp olive oil\n1 tsp paprika",
            stepsRaw = "Rinse the quinoa and cook it in 2 cups of water for 15 minutes.\nChop the courgette and pepper.\nFry the vegetables and chickpeas in olive oil with paprika for 8 minutes.\nServe the vegetables on top of the quinoa.",
            calories = 480, protein = 18, difficulty = "Medium"
        ),
        Recipe(
            id = -5, title = "Brown Rice with Veg", imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80", readyInMinutes = 40, servings = 2,
            ingredientsRaw = "1 cup brown rice\n1 carrot\n1 cup broccoli\n1 onion\n2 tbsp soy sauce\n1 tbsp oil",
            stepsRaw = "Cook the brown rice in 2 1/2 cups of water for 30 minutes.\nChop the carrot, broccoli and onion.\nStir-fry the vegetables in oil for 6 minutes.\nAdd the rice and soy sauce and stir-fry for 2 more minutes.",
            calories = 450, protein = 10, difficulty = "Medium"
        )
    )
}
