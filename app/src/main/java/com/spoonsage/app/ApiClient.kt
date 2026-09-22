package com.spoonsage.app

import com.spoonsage.app.data.SpoonacularApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builds the Retrofit client used to talk to Spoonacular for recipes
 * (Figure 3). The AI Coach no longer goes through Retrofit/Anthropic -
 * see GeminiCoachClient, which calls Gemini via Firebase AI Logic instead.
 */
object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // ---------- Spoonacular ----------

    private val spoonacularClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val spoonacularRetrofit = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .client(spoonacularClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val spoonacularApi: SpoonacularApi by lazy {
        spoonacularRetrofit.create(SpoonacularApi::class.java)
    }
}
