package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        apiKey: String,
        model: String,
        systemPrompt: String,
        memories: List<String>,
        history: List<Pair<String, String>>, // role, text
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("API Key অনুপস্থিত। সেটিংস থেকে আপনার Gemini API Key প্রদান করুন।")
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            // Construct contents
            val contentsArray = JSONArray()

            // Add history
            for ((role, text) in history.takeLast(10)) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role == "user") "user" else "model")
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", text))
                turnObj.put("parts", partsArray)
                contentsArray.put(turnObj)
            }

            // Current user message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            // Dynamic system instruction including memories
            val memoryContext = if (memories.isNotEmpty()) {
                "\n\n[USER PERMANENT MEMORIES]:\n" + memories.joinToString("\n- ") { it }
            } else ""

            val fullSystemPrompt = systemPrompt + memoryContext +
                    "\n\n[DEVICE CAPABILITIES]: You are running on an Android smartphone as Archer AI. You can trigger phone calls, SMS, YouTube playback, Google search, torch toggle, camera, alarms, tasks, and system info. If the user asks you to perform an action, answer in natural conversational Bengali, and clearly acknowledge the action."

            val rootJson = JSONObject()
            rootJson.put("contents", contentsArray)

            val systemInstructionObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", fullSystemPrompt))
            systemInstructionObj.put("parts", sysParts)
            rootJson.put("systemInstruction", systemInstructionObj)

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("topP", 0.95)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val err = errJson.optJSONObject("error")
                    err?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception("Gemini API ত্রুটি: $errorMsg"))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("মডেল থেকে কোনো উত্তর পাওয়া যায়নি।"))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        textBuilder.append(p.getString("text"))
                    }
                }
            }

            val finalText = textBuilder.toString().trim()
            if (finalText.isEmpty()) {
                Result.success("স্যার, আদেশ দিন। আমি প্রস্তুত।")
            } else {
                Result.success(finalText)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(apiKey: String, model: String): Result<Long> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key দেওয়া হয়নি।"))
        }
        val startTime = System.currentTimeMillis()
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val pingJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "ping"))
                        })
                    })
                })
            }
            val request = Request.Builder()
                .url(url)
                .post(pingJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(latency)
            } else {
                val errorMsg = try {
                    JSONObject(body).optJSONObject("error")?.optString("message") ?: "Code ${response.code}"
                } catch (e: Exception) {
                    "Code ${response.code}"
                }
                Result.failure(Exception("সার্ভার ত্রুটি: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
