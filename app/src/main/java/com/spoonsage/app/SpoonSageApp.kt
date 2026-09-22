package com.spoonsage.app

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.spoonsage.app.data.AppDatabase
import com.spoonsage.app.data.FirestoreUserRepository
import com.spoonsage.app.data.GeminiCoachClient
import com.spoonsage.app.data.RecipeRepository

/**
 * Application class. Holds simple manual "dependency injection" -
 * one shared database, repository, and Firebase instances for the whole
 * app (no Hilt/Dagger, kept intentionally simple as requested).
 *
 * Firebase Auth handles sign up / log in / guest (anonymous) accounts,
 * Firestore syncs profile + goals + diet to the cloud, and Gemini (via
 * Firebase AI Logic) powers the AI Coach - see SETUP_FIREBASE.md for the
 * one-time console setup this needs.
 */
class SpoonSageApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: RecipeRepository
        private set

    lateinit var auth: FirebaseAuth
        private set

    lateinit var firestoreUserRepository: FirestoreUserRepository
        private set

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)
        auth = Firebase.auth
        firestoreUserRepository = FirestoreUserRepository(Firebase.firestore, auth)

        database = AppDatabase.getInstance(this)
        repository = RecipeRepository(
            api = ApiClient.spoonacularApi,
            geminiClient = GeminiCoachClient(),
            recipeDao = database.recipeDao(),
            mealPlanDao = database.mealPlanDao(),
            groceryDao = database.groceryDao(),
            progressDao = database.progressDao(),
            reminderDao = database.reminderDao(),
            aiCoachDao = database.aiCoachDao()
        )
    }
}
