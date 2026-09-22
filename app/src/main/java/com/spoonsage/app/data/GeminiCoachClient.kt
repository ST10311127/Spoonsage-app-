package com.spoonsage.app.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

private const val TAG = "SpoonSage/Gemini"

/**
 * AI Coach client powered by Gemini via Firebase AI Logic
 * (Section 5.1 / Figure 3 - "AI Coach Orchestrator" -> was "Claude API").
 *
 * Uses GenerativeBackend.googleAI() - the Gemini Developer API backend -
 * which has a free tier and needs no Cloud billing account, unlike
 * GenerativeBackend.vertexAI() which requires the paid Blaze plan.
 * No local API key is needed: the model is reached through your Firebase
 * project (see google-services.json / SETUP_FIREBASE.md).
 */
class GeminiCoachClient {

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.6-flash")
    }

    /** Sends a system prompt + the user's message and returns the raw reply text. */
    suspend fun sendMessage(systemPrompt: String, userText: String): String {
        Log.d(TAG, "sendMessage: calling Gemini (userText length=${userText.length})")
        val prompt = "$systemPrompt\n\nUser: $userText"
        val response = model.generateContent(prompt)
        Log.d(TAG, "sendMessage: response received")
        return response.text ?: ""
    }
}
