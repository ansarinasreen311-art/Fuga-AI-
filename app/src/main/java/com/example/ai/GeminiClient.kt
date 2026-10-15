package com.example.ai

import com.example.builder.CodeTemplates
import com.example.data.model.ActionCommand
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.CustomAgent
import com.example.data.model.MemoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiAssistantResult(
    val replyText: String,
    val detectedAction: ActionCommand? = null
)

class GeminiClient {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"
    }

    suspend fun testApiKey(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty"))
        }
        try {
            val url = "$BASE_URL?key=$apiKey"
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", "Respond with 'OK' only."))
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody).optJSONObject("error")
                    errJson?.optString("message") ?: "HTTP error ${response.code}"
                } catch (e: Exception) {
                    "HTTP error ${response.code}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateResponse(
        userInput: String,
        history: List<ChatMessage>,
        memories: List<MemoryItem>,
        apiKey: String,
        customAgent: CustomAgent? = null
    ): Result<GeminiAssistantResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            // Local fallback for offline/no-key testing
            val localAction = detectLocalAction(userInput)
            return@withContext Result.success(
                GeminiAssistantResult(
                    replyText = generateLocalReply(userInput, localAction),
                    detectedAction = localAction
                )
            )
        }

        try {
            val systemInstruction = buildString {
                if (customAgent != null) {
                    append("You are operating as specialized agent '${customAgent.name}' (${customAgent.role}). ")
                    append("${customAgent.systemPrompt} ")
                    append("Capabilities: ${customAgent.capabilities}. ")
                } else {
                    append("You are Fuga AI, a sophisticated, privacy-first personal assistant. ")
                }
                append("Be concise, direct, helpful, and polite. ")
                append("You can assist with voice commands, web searches, app launches, contacts, alarms, flashlight, screen reading, building apps and websites, and payments. ")
                if (memories.isNotEmpty()) {
                    append("\nUser context/memories:\n")
                    memories.take(5).forEach {
                        append("- ${it.category}: ${it.content}\n")
                    }
                }
                append("\nIf the user is asking to perform a device action or payment, include an ACTION marker at the end of your response formatted like:\n")
                append("<!--ACTION: {\"type\": \"OPEN_APP\"|\"CALL_PHONE\"|\"SEND_MESSAGE\"|\"SET_ALARM\"|\"TOGGLE_FLASHLIGHT\"|\"WEB_SEARCH\"|\"READ_SCREEN\"|\"PAYMENT\", \"target\": \"value\", \"param\": \"optional\", \"isSensitive\": true|false, \"title\": \"Brief action title\", \"description\": \"Action description\"} -->")
            }

            val contents = JSONArray()

            // System prompt as first turn or context
            val turnsToInclude = history.takeLast(6)
            for (msg in turnsToInclude) {
                val role = if (msg.isUser) "user" else "model"
                val partObj = JSONObject().put("text", msg.text)
                val contentObj = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(partObj))
                }
                contents.put(contentObj)
            }

            // Current input
            val currentUserObj = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userInput)))
            }
            contents.put(currentUserObj)

            val requestJson = JSONObject().apply {
                put("contents", contents)
                val systemInstObj = JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                }
                put("systemInstruction", systemInstObj)
            }

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // If remote fails, fallback to local intelligent parser
                val localAction = detectLocalAction(userInput)
                return@withContext Result.success(
                    GeminiAssistantResult(
                        replyText = "Fuga AI: " + generateLocalReply(userInput, localAction),
                        detectedAction = localAction
                    )
                )
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: "I'm here to help."

            val (cleanText, actionFromAi) = parseActionMarker(rawText)
            val finalAction = actionFromAi ?: detectLocalAction(userInput)

            Result.success(
                GeminiAssistantResult(
                    replyText = cleanText.ifBlank { "Done." },
                    detectedAction = finalAction
                )
            )
        } catch (e: Exception) {
            val localAction = detectLocalAction(userInput)
            Result.success(
                GeminiAssistantResult(
                    replyText = generateLocalReply(userInput, localAction),
                    detectedAction = localAction
                )
            )
        }
    }

    private fun parseActionMarker(text: String): Pair<String, ActionCommand?> {
        val markerStart = text.indexOf("<!--ACTION:")
        if (markerStart == -1) return Pair(text, null)

        val markerEnd = text.indexOf("-->", markerStart)
        if (markerEnd == -1) return Pair(text, null)

        val cleanText = (text.substring(0, markerStart) + text.substring(markerEnd + 3)).trim()
        val jsonStr = text.substring(markerStart + 11, markerEnd).trim()

        return try {
            val json = JSONObject(jsonStr)
            val typeStr = json.optString("type")
            val actionType = try {
                ActionType.valueOf(typeStr)
            } catch (e: Exception) {
                ActionType.WEB_SEARCH
            }
            val title = json.optString("title", actionType.label)
            val desc = json.optString("description", "")
            val target = json.optString("target", "")
            val param = json.optString("param", null)
            val isSensitive = json.optBoolean("isSensitive", actionType == ActionType.PAYMENT)

            val cmd = ActionCommand(
                type = actionType,
                title = title,
                description = desc,
                isSensitive = isSensitive,
                target = target,
                param = param
            )
            Pair(cleanText, cmd)
        } catch (e: Exception) {
            Pair(cleanText, null)
        }
    }

    fun detectLocalAction(input: String): ActionCommand? {
        val lower = input.lowercase().trim()

        // 1. Payment (Always sensitive)
        val paymentMatch = Regex("(?:pay|transfer|send money|upi)\\s+(?:(?:rs\\.?|inr|₹|\\$)?\\s*(\\d+)(?:\\s*(?:rs\\.?|inr|dollars))?)\\s+(?:to\\s+)?([a-zA-Z0-9 ]+)", RegexOption.IGNORE_CASE).find(input)
        if (paymentMatch != null || lower.startsWith("pay ") || lower.contains("send money")) {
            val amount = paymentMatch?.groupValues?.getOrNull(1) ?: "500"
            val recipient = paymentMatch?.groupValues?.getOrNull(2)?.trim() ?: "Recipient"
            return ActionCommand(
                type = ActionType.PAYMENT,
                title = "Payment to $recipient",
                description = "Authorization of ₹$amount via secure UPI/Banking",
                isSensitive = true,
                target = recipient,
                param = amount
            )
        }

        // 2. Flashlight
        if (lower.contains("flashlight") || lower.contains("torch")) {
            val turnOn = !lower.contains("off")
            return ActionCommand(
                type = ActionType.TOGGLE_FLASHLIGHT,
                title = if (turnOn) "Turn On Flashlight" else "Turn Off Flashlight",
                description = "Hardware Camera Flashlight control",
                isSensitive = false,
                target = if (turnOn) "on" else "off"
            )
        }

        // 3. Open App (YouTube, WhatsApp, Settings, etc.)
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val appName = input.substringAfter("open ").substringAfter("launch ").trim()
            return ActionCommand(
                type = ActionType.OPEN_APP,
                title = "Open $appName",
                description = "Launch application $appName",
                isSensitive = false,
                target = appName
            )
        }

        // 4. Call Phone
        if (lower.startsWith("call ") || lower.startsWith("dial ")) {
            val contact = input.substringAfter("call ").substringAfter("dial ").trim()
            return ActionCommand(
                type = ActionType.CALL_PHONE,
                title = "Call $contact",
                description = "Initiate phone call to $contact",
                isSensitive = false,
                target = contact
            )
        }

        // 5. Send message
        if (lower.startsWith("send message to ") || lower.startsWith("message ") || lower.startsWith("text ")) {
            val contact = input.substringAfter("to ").trim()
            return ActionCommand(
                type = ActionType.SEND_MESSAGE,
                title = "Message $contact",
                description = "Compose message to $contact",
                isSensitive = false,
                target = contact
            )
        }

        // 6. Set Alarm
        if (lower.contains("alarm") || lower.contains("wake me")) {
            val time = Regex("(\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)?)", RegexOption.IGNORE_CASE).find(input)?.value ?: "7:00 AM"
            return ActionCommand(
                type = ActionType.SET_ALARM,
                title = "Set Alarm for $time",
                description = "System clock alarm for $time",
                isSensitive = false,
                target = time
            )
        }

        // 7. Read Screen
        if (lower.contains("read screen") || lower.contains("read this screen") || lower.contains("what is on screen")) {
            return ActionCommand(
                type = ActionType.READ_SCREEN,
                title = "Read Active Screen",
                description = "Analyze text and contents on screen",
                isSensitive = false,
                target = "current_screen"
            )
        }

        // 8. Search
        if (lower.startsWith("search ") || lower.startsWith("google ")) {
            val query = input.substringAfter("search ").substringAfter("google ").substringAfter("for ").trim()
            return ActionCommand(
                type = ActionType.WEB_SEARCH,
                title = "Search \"$query\"",
                description = "Search on Google",
                isSensitive = false,
                target = query
            )
        }

        return null
    }

    private fun generateLocalReply(input: String, action: ActionCommand?): String {
        return when (action?.type) {
            ActionType.PAYMENT -> "Payment of ₹${action.param} to ${action.target} detected. Sensitive operation requires biometric verification."
            ActionType.OPEN_APP -> "Opening ${action.target} for you now."
            ActionType.CALL_PHONE -> "Preparing to call ${action.target}."
            ActionType.TOGGLE_FLASHLIGHT -> if (action.target == "on") "Turning flashlight on." else "Turning flashlight off."
            ActionType.SET_ALARM -> "Setting an alarm for ${action.target}."
            ActionType.SEND_MESSAGE -> "Drafting message for ${action.target}."
            ActionType.WEB_SEARCH -> "Searching for \"${action.target}\" on Google."
            ActionType.READ_SCREEN -> "Scanning the active screen contents."
            null -> {
                val lower = input.lowercase()
                when {
                    lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                        "Hello! I am Fuga, your personal AI assistant. How can I help you today?"
                    lower.contains("who are you") || lower.contains("what are you") ->
                        "I am Fuga AI, your on-device personal assistant with voice interaction, wake word, and smart automation capabilities."
                    lower.contains("capabilities") || lower.contains("what can you do") ->
                        "I can manage alarms, launch apps, call contacts, search the web, toggle flashlight, read your screen, build Android apps & websites, and handle secure payments with biometrics."
                    lower.contains("build app") || lower.contains("create app") ->
                        "I can build full Android apps with Jetpack Compose and export them as .ZIP packages! Check the Build App tab."
                    lower.contains("build website") || lower.contains("create website") ->
                        "I can build responsive websites, export source in .ZIP, and launch live previews in your browser! Check the Build Website tab."
                    else ->
                        "Understood. I am processing your request with Fuga AI."
                }
            }
        }
    }

    suspend fun generateAppProjectCode(
        prompt: String,
        title: String,
        apiKey: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val defaultFiles = CodeTemplates.getDefaultAppProject(title, prompt)
        if (apiKey.isBlank()) return@withContext defaultFiles

        try {
            val systemInstruction = "You are an expert Android developer. Generate a complete single-file Jetpack Compose MainActivity.kt for an Android application satisfying the prompt. Return ONLY the Kotlin code inside ```kotlin ... ``` codeblock, with complete imports and Composable UI."
            val userPrompt = "Build an Android app for: $prompt. App Title: $title."
            val res = callGeminiSimple(userPrompt, systemInstruction, apiKey)
            if (res.isNotBlank()) {
                val code = extractCodeBlock(res, "kotlin") ?: res
                val updatedFiles = defaultFiles.toMutableMap()
                val mainKey = updatedFiles.keys.firstOrNull { it.endsWith("MainActivity.kt") }
                if (mainKey != null && code.contains("class MainActivity")) {
                    updatedFiles[mainKey] = code
                }
                return@withContext updatedFiles
            }
            defaultFiles
        } catch (e: Exception) {
            defaultFiles
        }
    }

    suspend fun generateWebsiteProjectCode(
        prompt: String,
        title: String,
        apiKey: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val defaultFiles = CodeTemplates.getDefaultWebsiteProject(title, prompt)
        if (apiKey.isBlank()) return@withContext defaultFiles

        try {
            val systemInstruction = "You are a master Web Developer. Generate a complete, self-contained single-page responsive website in HTML5 with embedded CSS and modern JavaScript for the prompt. Return ONLY the complete HTML code starting with <!DOCTYPE html> inside ```html ... ``` codeblock."
            val userPrompt = "Build a responsive website for: $prompt. Title: $title."
            val res = callGeminiSimple(userPrompt, systemInstruction, apiKey)
            if (res.isNotBlank()) {
                val html = extractCodeBlock(res, "html") ?: res
                if (html.contains("<!DOCTYPE") || html.contains("<html")) {
                    val updatedFiles = defaultFiles.toMutableMap()
                    updatedFiles["index.html"] = html
                    return@withContext updatedFiles
                }
            }
            defaultFiles
        } catch (e: Exception) {
            defaultFiles
        }
    }

    private suspend fun callGeminiSimple(
        prompt: String,
        systemInstruction: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val contents = JSONArray().apply {
                val userObj = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
                put(userObj)
            }
            val requestJson = JSONObject().apply {
                put("contents", contents)
                val systemInstObj = JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                }
                put("systemInstruction", systemInstObj)
            }
            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext ""
            val root = JSONObject(body)
            val parts = root.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractCodeBlock(raw: String, lang: String): String? {
        val pattern = Regex("```(?:$lang)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val match = pattern.find(raw)
        return match?.groupValues?.getOrNull(1)?.trim()
    }
}
