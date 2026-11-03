package com.spoonsage.app.data.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.spoonsage.app.data.local.AiCoachMessage
import com.spoonsage.app.data.local.isFromUser

class GeminiCoachClient {

    private var activeModelName: String? = null

    private fun createGenerativeModel(modelName: String) =
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = modelName,
                systemInstruction = content { text(SYSTEM_PROMPT) }
            )

    suspend fun ask(userContext: String, history: List<AiCoachMessage>, question: String): String {
        val modelsToTry = if (activeModelName != null) {
            listOf(activeModelName!!) + CANDIDATE_MODELS.filter { it != activeModelName }
        } else {
            CANDIDATE_MODELS
        }

        var lastException: Exception? = null
        for (modelName in modelsToTry) {
            try {
                Log.d(TAG, "Attempting Gemini message using model '$modelName'")
                val model = createGenerativeModel(modelName)
                val chat = model.startChat(
                    history = history.map { msg ->
                        content(role = if (msg.isFromUser) "user" else "model") { text(msg.text) }
                    }
                )
                val response = chat.sendMessage("Context about the user: $userContext\n\nUser: $question")
                val text = response.text?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    activeModelName = modelName
                    Log.d(TAG, "Gemini call successful with model '$modelName'")
                    return text
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini model '$modelName' failed: ${e.message}", e)
                lastException = e
            }
        }
        throw lastException ?: IllegalStateException("All Gemini candidate models failed to generate a reply.")
    }

    companion object {
        private const val TAG = "GeminiCoachClient"
        const val MODEL_NAME = "gemini-3.5-flash-lite"
        val CANDIDATE_MODELS = listOf("gemini-3.5-flash-lite", "gemini-3.8-flash")

        private val SYSTEM_PROMPT = """
            You are the SpoonSage AI Coach, a friendly nutrition assistant inside a meal-planning app.
            Keep replies short (2-4 sentences), practical and encouraging.
            Respect the user's diet and the ingredients they avoid.
            If a specific dish would help, end your reply on its own final line in exactly this format:
            SUGGEST: <short recipe name>
            Only include a SUGGEST line when a concrete dish recommendation is genuinely useful.
        """.trimIndent()
    }
}
