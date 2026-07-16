package com.r1n1os.jetpackcomposetemplateopensource.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.r1n1os.jetpackcomposetemplateopensource.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * OpenRouter AI client using direct REST API (OkHttp + Gson).
 * Replaces the previous Gemini integration.
 */
class GeminiAssistant @Inject constructor() {

    companion object {
        private const val TAG = "GeminiAssistant"
        private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
        // private const val MODEL = "meta-llama/llama-3.2-3b-instruct:free"
        //private const val MODEL = "google/gemma-2-9b-it:free"
        private const val MODEL = "openrouter/free"
        //private const val MODEL = "microsoft/phi-3.5-mini-128k-instruct:free"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val client = OkHttpClient()
    private val gson = Gson()

    // ── Public API ──────────────────────────────────────────────────────

    suspend fun generateShoppingList(input: String): Result<List<String>> {
        val key = BuildConfig.OPENROUTER_API_KEY
        Log.d(TAG, "generateShoppingList | key length=${key.length}")

        if (key.isBlank() || key == "tu_clave_aqui") {
            return Result.failure(Exception(
                "API key de OpenRouter no encontrada. Agregá OPENROUTER_API_KEY=... en local.properties " +
                "y ejecutá Build > Clean Project > Rebuild"
            ))
        }

        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(input)

                val requestBody = mapOf(
                    "model" to MODEL,
                    "messages" to listOf(
                        mapOf("role" to "user", "content" to prompt)
                    ),
                    "max_tokens" to 150,
                    "temperature" to 0.7
                )

                val jsonBody = gson.toJson(requestBody)

                Log.d(TAG, "POST $OPENROUTER_URL")
                Log.d(TAG, "Request body: $jsonBody")

                val request = Request.Builder()
                    .url(OPENROUTER_URL)
                    .addHeader("Authorization", "Bearer $key")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val err = JsonParser.parseString(responseBody).asJsonObject
                        err.get("error")?.asJsonObject?.get("message")?.asString
                            ?: "HTTP ${response.code}"
                    } catch (_: Exception) { "HTTP ${response.code}" }
                    return@withContext Result.failure(Exception("OpenRouter API error: $errorMsg"))
                }

                val jsonResponse = try {
                    JsonParser.parseString(responseBody).asJsonObject
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response JSON", e)
                    return@withContext Result.failure(Exception("Error al parsear respuesta: ${e.message}"))
                }

                val choices = jsonResponse.getAsJsonArray("choices")
                val text = if (choices != null && choices.size() > 0) {
                    val message = choices[0].asJsonObject.getAsJsonObject("message")
                    message.get("content").asString?.trim()
                } else null

                if (text.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("OpenRouter devolvió una respuesta vacía"))
                }

                Log.d(TAG, "Respuesta parseada: $text")
                val items = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                Result.success(items)

            } catch (e: Exception) {
                Log.e(TAG, "Error en llamada a OpenRouter", e)
                Result.failure(e)
            }
        }
    }

    // ── Private helpers ─────────────────────────────────────────────────

    private fun buildPrompt(input: String): String = """
        You are a helpful shopping assistant. Given a meal description, return ONLY a comma-separated list of grocery items.
        Do not include any explanations, numbers, or extra text. Just the items separated by commas.
        Example input: "cena italiana para 4 personas"
        Example output: "pasta, tomates, albahaca, queso parmesano, aceite de oliva, ajo"
        Input: $input
    """.trimIndent()
}
