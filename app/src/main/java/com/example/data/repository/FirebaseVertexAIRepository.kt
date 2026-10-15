package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.ai.GeminiAssistantResult
import com.example.data.model.ActionCommand
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.CustomAgent
import com.example.data.model.MemoryItem
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.HarmBlockThreshold
import com.google.firebase.ai.type.HarmCategory
import com.google.firebase.ai.type.SafetySetting
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * High-level emission from the streaming Vertex AI response parser.
 */
sealed class VertexStreamingEvent {
    /**
     * A delta text chunk received directly from the model stream.
     */
    data class TextDelta(val chunk: String) : VertexStreamingEvent()

    /**
     * A natural, speech-ready sentence or phrase suitable for immediate TTS playback.
     */
    data class SpeechSentenceReady(val sentence: String) : VertexStreamingEvent()

    /**
     * Final completion payload containing the clean response text and parsed action command.
     */
    data class Complete(val result: GeminiAssistantResult) : VertexStreamingEvent()

    /**
     * An error occurred during generation or stream processing.
     */
    data class Error(val throwable: Throwable) : VertexStreamingEvent()
}

/**
 * Production repository integrating Firebase Vertex AI (Gemini) with real-time streaming response parsing.
 * Specifically tuned for natural, low-latency, real-time voice interactions.
 */
class FirebaseVertexAIRepository(
    private val context: Context,
    private val modelName: String = "gemini-3.5-flash",
    private val region: String = "us-central1"
) {

    companion object {
        private const val TAG = "FirebaseVertexAIRepo"
    }

    private var generativeModel: GenerativeModel? = null

    /**
     * Lazily initialize or retrieve the Firebase Vertex AI GenerativeModel instance.
     */
    @Synchronized
    fun getOrInitModel(customSystemPrompt: String? = null): GenerativeModel {
        val app = try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance()
            } else {
                FirebaseApp.getInstance()
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp check/init: ${e.message}")
            FirebaseApp.getInstance()
        }

        val backend = GenerativeBackend.vertexAI(region)
        val firebaseAI = try {
            Firebase.ai(app, backend)
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAI instance fallback: ${e.message}")
            FirebaseAI.getInstance(app, backend)
        }

        val config = generationConfig {
            temperature = 0.7f
            topP = 0.95f
            topK = 40
        }

        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, HarmBlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, HarmBlockThreshold.MEDIUM_AND_ABOVE)
        )

        val systemContent: Content? = customSystemPrompt?.let { prompt ->
            Content("system", listOf(TextPart(prompt)))
        }

        val model = firebaseAI.generativeModel(
            modelName = modelName,
            generationConfig = config,
            safetySettings = safetySettings,
            systemInstruction = systemContent
        )
        generativeModel = model
        return model
    }

    /**
     * Generates content as a real-time reactive Flow, parsing incoming tokens into natural speech sentences
     * for zero-latency, realistic voice interaction while extracting embedded assistant actions.
     */
    fun streamVoiceResponse(
        userInput: String,
        history: List<ChatMessage> = emptyList(),
        memories: List<MemoryItem> = emptyList(),
        customAgent: CustomAgent? = null
    ): Flow<VertexStreamingEvent> = flow {
        val systemPrompt = buildSystemInstruction(customAgent, memories)
        val model = getOrInitModel(systemPrompt)

        // Build conversation contents including recent history (last 8 turns for responsive voice context)
        val contents = mutableListOf<Content>()
        val recentHistory = history.takeLast(8)
        for (msg in recentHistory) {
            val role = if (msg.isUser) "user" else "model"
            contents.add(Content(role, listOf(TextPart(msg.text))))
        }
        contents.add(Content("user", listOf(TextPart(userInput))))

        val fullAccumulator = StringBuilder()
        val speechSentenceBuffer = StringBuilder()

        try {
            val responseStream = model.generateContentStream(contents)

            responseStream.collect { response ->
                val chunkText = response.text
                if (!chunkText.isNullOrEmpty()) {
                    fullAccumulator.append(chunkText)
                    speechSentenceBuffer.append(chunkText)

                    emit(VertexStreamingEvent.TextDelta(chunkText))

                    // Parse out ready sentences for natural voice playback
                    val readySentences = extractCompletedSentences(speechSentenceBuffer)
                    for (sentence in readySentences) {
                        val cleanForSpeech = cleanActionMarkersForVoice(sentence)
                        if (cleanForSpeech.isNotBlank()) {
                            emit(VertexStreamingEvent.SpeechSentenceReady(cleanForSpeech))
                        }
                    }
                }
            }

            // Flush any remaining text in the buffer after stream finishes
            val remainingSpeech = cleanActionMarkersForVoice(speechSentenceBuffer.toString().trim())
            if (remainingSpeech.isNotBlank()) {
                emit(VertexStreamingEvent.SpeechSentenceReady(remainingSpeech))
                speechSentenceBuffer.clear()
            }

            // Parse final action markers and build completion result
            val rawFull = fullAccumulator.toString()
            val (cleanText, actionFromModel) = parseActionMarker(rawFull)
            val finalAction = actionFromModel ?: detectLocalVoiceAction(userInput)

            val finalResult = GeminiAssistantResult(
                replyText = cleanText.ifBlank { "I've handled that for you." },
                detectedAction = finalAction
            )
            emit(VertexStreamingEvent.Complete(finalResult))

        } catch (e: Throwable) {
            Log.e(TAG, "Error in Vertex AI streaming: ${e.message}", e)
            emit(VertexStreamingEvent.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Synchronous / one-shot generation using Firebase Vertex AI with graceful fallback.
     */
    suspend fun generateContentOneShot(
        userInput: String,
        history: List<ChatMessage> = emptyList(),
        memories: List<MemoryItem> = emptyList(),
        customAgent: CustomAgent? = null
    ): Result<GeminiAssistantResult> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = buildSystemInstruction(customAgent, memories)
            val model = getOrInitModel(systemPrompt)

            val contents = mutableListOf<Content>()
            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.isUser) "user" else "model"
                contents.add(Content(role, listOf(TextPart(msg.text))))
            }
            contents.add(Content("user", listOf(TextPart(userInput))))

            val response = model.generateContent(contents)
            val rawText = response.text ?: ""
            val (cleanText, actionFromModel) = parseActionMarker(rawText)
            val finalAction = actionFromModel ?: detectLocalVoiceAction(userInput)

            Result.success(
                GeminiAssistantResult(
                    replyText = cleanText.ifBlank { "Understood." },
                    detectedAction = finalAction
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "Vertex AI one-shot failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Extracts full sentences demarcated by '.', '!', '?', or newline delimiters from the buffer.
     * Retains any unfinished trailing fragment in the buffer for the next stream chunks.
     */
    private fun extractCompletedSentences(buffer: StringBuilder): List<String> {
        val current = buffer.toString()
        val sentences = mutableListOf<String>()

        var startIdx = 0
        val len = current.length

        for (i in 0 until len) {
            val ch = current[i]
            val isSentenceDelimiter = (ch == '.' || ch == '!' || ch == '?' || ch == '\n' || ch == ':')

            if (isSentenceDelimiter) {
                // Check if followed by whitespace, quote, or end of string to avoid mid-number periods
                val isNextWhitespaceOrEnd = (i + 1 >= len) || current[i + 1].isWhitespace() || current[i + 1] == '"'
                if (isNextWhitespaceOrEnd) {
                    val segment = current.substring(startIdx, i + 1).trim()
                    // Don't cut off inside an unclosed ACTION marker tag
                    if (!isInsideActionMarker(segment)) {
                        if (segment.isNotBlank()) {
                            sentences.add(segment)
                        }
                        startIdx = i + 1
                    }
                }
            }
        }

        if (startIdx > 0) {
            val remaining = current.substring(startIdx)
            buffer.clear()
            buffer.append(remaining)
        }

        return sentences
    }

    private fun isInsideActionMarker(text: String): Boolean {
        val open = text.lastIndexOf("<!--ACTION:")
        val close = text.lastIndexOf("-->")
        return open != -1 && open > close
    }

    private fun cleanActionMarkersForVoice(text: String): String {
        return text.replace(Regex("<!--ACTION:.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("[*#_`~]"), "") // Strip markdown symbols for speech synthesis
            .replace("\\s+".toRegex(), " ")
            .trim()
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

    private fun detectLocalVoiceAction(input: String): ActionCommand? {
        val lower = input.lowercase().trim()

        if (lower.contains("flashlight") || lower.contains("torch")) {
            val turnOff = lower.contains("off")
            return ActionCommand(
                type = ActionType.TOGGLE_FLASHLIGHT,
                title = if (turnOff) "Turn Off Flashlight" else "Turn On Flashlight",
                description = "Hardware Camera Flash Control",
                isSensitive = false,
                target = if (turnOff) "off" else "on"
            )
        }

        if (lower.contains("alarm") || lower.contains("wake me")) {
            return ActionCommand(
                type = ActionType.SET_ALARM,
                title = "Set Alarm",
                description = "Clock Application Alarm",
                isSensitive = false,
                target = "07:00",
                param = "Morning Alarm"
            )
        }

        if (lower.startsWith("pay ") || lower.contains("send money")) {
            return ActionCommand(
                type = ActionType.PAYMENT,
                title = "UPI Payment",
                description = "Biometric protected transaction",
                isSensitive = true,
                target = "Contact",
                param = "500"
            )
        }

        return null
    }

    private fun buildSystemInstruction(customAgent: CustomAgent?, memories: List<MemoryItem>): String {
        return buildString {
            if (customAgent != null) {
                append("You are operating as specialized agent '${customAgent.name}' (${customAgent.role}). ")
                append(customAgent.systemPrompt)
                append("\nCapabilities: ${customAgent.capabilities}.\n")
            } else {
                append("You are Fuga AI, an advanced, highly responsive, voice-first Android AI personal assistant. ")
                append("Keep spoken voice answers natural, conversational, concise, and helpful. Avoid robotic bullet dumps in voice mode unless asked. ")
            }

            if (memories.isNotEmpty()) {
                append("\nUser Personal Memory:\n")
                memories.take(15).forEach { m ->
                    append("- [${m.category}] ${m.content}\n")
                }
            }

            append("\nIf user requests a phone action (flashlight, alarm, open app, payment, call, email), include the machine action marker tag in this exact format:\n")
            append("<!--ACTION:{\"type\":\"TOGGLE_FLASHLIGHT\",\"title\":\"Turn On Flashlight\",\"description\":\"Toggle torch\",\"isSensitive\":false,\"target\":\"on\",\"param\":null}-->\n")
            append("Supported action types: OPEN_APP, CALL_PHONE, SEND_MESSAGE, SET_ALARM, TOGGLE_FLASHLIGHT, PAYMENT, READ_SCREEN, WEB_SEARCH.\n")
            append("Place spoken reply text first, and append the <!--ACTION:...--> marker at the very end.\n")
        }
    }
}
