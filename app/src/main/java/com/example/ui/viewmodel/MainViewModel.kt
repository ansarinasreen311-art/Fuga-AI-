package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FugaApp
import com.example.actions.ActionExecutor
import com.example.ai.GeminiAssistantResult
import com.example.ai.GeminiClient
import com.example.builder.ProjectZipExporter
import com.example.data.model.ActionCommand
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.CustomAgent
import com.example.data.model.DynamicIslandState
import com.example.data.model.DynamicIslandStatus
import com.example.data.model.GeneratedProject
import com.example.data.model.MemoryItem
import com.example.data.model.ProjectType
import com.example.data.model.CallStatus
import com.example.data.model.CallTranscriptItem
import com.example.data.model.UserSession
import com.example.data.repository.VertexStreamingEvent
import com.example.voice.FugaSpeechManager
import com.example.voice.FugaWakeWordDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenDestination {
    WELCOME,
    PERMISSIONS,
    CAPABILITIES,
    WAKE_WORD_SETUP,
    FUGA_GMAIL_SETUP,
    HOME,
    SETTINGS,
    HISTORY,
    MEMORY,
    CUSTOM_AGENTS,
    APP_BUILDER,
    WEBSITE_BUILDER,
    AUDIO_CALL,
    WAKE_WORD_SETTINGS
}

sealed class UiEvent {
    data class RequestBiometric(val message: ChatMessage) : UiEvent()
    data class ShowToast(val text: String) : UiEvent()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FugaApp
    val prefs = app.preferences
    private val chatRepo = app.chatRepository
    private val memoryRepo = app.memoryRepository
    private val customAgentRepo = app.customAgentRepository
    private val projectRepo = app.projectRepository

    val geminiClient = GeminiClient()
    val vertexAIRepo = app.vertexAIRepository
    val actionExecutor = ActionExecutor(application)
    val speechManager = FugaSpeechManager(application)
    val wakeWordDetector = FugaWakeWordDetector(application)

    private val _currentScreen = MutableStateFlow(
        if (prefs.isFirstLaunchCompleted) ScreenDestination.HOME else ScreenDestination.WELCOME
    )
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _dynamicIslandState = MutableStateFlow(
        DynamicIslandState(
            status = DynamicIslandStatus.IDLE,
            headline = "Fuga is ready",
            subtitle = "Tap mic or say “Hey Fuga”"
        )
    )
    val dynamicIslandState: StateFlow<DynamicIslandState> = _dynamicIslandState.asStateFlow()

    private val _userSession = MutableStateFlow(prefs.getUserSession())
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    val messages: StateFlow<List<ChatMessage>> = chatRepo.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryItem>> = memoryRepo.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customAgents: StateFlow<List<CustomAgent>> = customAgentRepo.allAgents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAgent: StateFlow<CustomAgent?> = customAgentRepo.activeAgent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val projects: StateFlow<List<GeneratedProject>> = projectRepo.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val audioLevel: StateFlow<Float> = speechManager.audioRms

    // Always Ready Hands-Free State
    private val _isAlwaysReadyActive = MutableStateFlow(false)
    val isAlwaysReadyActive: StateFlow<Boolean> = _isAlwaysReadyActive.asStateFlow()

    // Audio Call State
    private val _callStatus = MutableStateFlow(CallStatus.IDLE)
    val callStatus: StateFlow<CallStatus> = _callStatus.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted.asStateFlow()

    private val _isCallSpeakerOn = MutableStateFlow(true)
    val isCallSpeakerOn: StateFlow<Boolean> = _isCallSpeakerOn.asStateFlow()

    private val _callTranscripts = MutableStateFlow<List<CallTranscriptItem>>(emptyList())
    val callTranscripts: StateFlow<List<CallTranscriptItem>> = _callTranscripts.asStateFlow()

    private val _callLiveCaption = MutableStateFlow("")
    val callLiveCaption: StateFlow<String> = _callLiveCaption.asStateFlow()

    private var callTimerJob: Job? = null

    init {
        // Observe speech manager state to update dynamic island
        viewModelScope.launch {
            speechManager.isListening.collect { listening ->
                if (listening) {
                    _dynamicIslandState.value = DynamicIslandState(
                        status = DynamicIslandStatus.LISTENING,
                        headline = "Fuga Listening...",
                        subtitle = "Speak your request",
                        audioLevel = speechManager.audioRms.value
                    )
                } else if (_dynamicIslandState.value.status == DynamicIslandStatus.LISTENING) {
                    _dynamicIslandState.value = DynamicIslandState(
                        status = DynamicIslandStatus.IDLE,
                        headline = "Fuga is ready",
                        subtitle = "Tap mic or say “Hey Fuga”"
                    )
                }
            }
        }

        viewModelScope.launch {
            speechManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _dynamicIslandState.value = DynamicIslandState(
                        status = DynamicIslandStatus.SPEAKING,
                        headline = "Fuga Speaking",
                        subtitle = "Providing assistant reply"
                    )
                } else if (_dynamicIslandState.value.status == DynamicIslandStatus.SPEAKING) {
                    _dynamicIslandState.value = DynamicIslandState(
                        status = DynamicIslandStatus.IDLE,
                        headline = "Fuga is ready",
                        subtitle = "Tap mic or say “Hey Fuga”"
                    )
                }
            }
        }

        // Handle speaking completion for Call auto-duplex & Always-Ready auto-rearm
        speechManager.onSpeakingCompletedListener = {
            if (_currentScreen.value == ScreenDestination.AUDIO_CALL) {
                if (_callStatus.value == CallStatus.SPEAKING && !_isCallMuted.value) {
                    startCallListening()
                }
            } else if (_currentScreen.value == ScreenDestination.HOME) {
                rearmAlwaysReadyListener()
            }
        }

        // Initialize Always Ready if enabled on startup
        if (prefs.isFirstLaunchCompleted) {
            updateAlwaysReadyState()
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
        updateAlwaysReadyState()
    }

    fun onContinueWithGoogle(name: String = "Alex", email: String = "alex.fuga.user@gmail.com") {
        prefs.saveUserSession(name, email)
        _userSession.value = prefs.getUserSession()
        navigateTo(ScreenDestination.PERMISSIONS)
    }

    fun onPermissionsFinished() {
        navigateTo(ScreenDestination.CAPABILITIES)
    }

    fun onCapabilitiesFinished() {
        navigateTo(ScreenDestination.WAKE_WORD_SETUP)
    }

    fun onWakeWordFinished() {
        navigateTo(ScreenDestination.FUGA_GMAIL_SETUP)
    }

    fun onFugaGmailFinished(gmail: String?) {
        val current = prefs.getUserSession()
        prefs.saveUserSession(current.displayName, current.email, current.photoUrl, gmail)
        _userSession.value = prefs.getUserSession()
        prefs.isFirstLaunchCompleted = true
        navigateTo(ScreenDestination.HOME)
    }

    fun startVoiceListening() {
        speechManager.startListening { transcribedText ->
            if (transcribedText.isNotBlank()) {
                sendMessage(transcribedText, isVoice = true)
            }
        }
    }

    fun stopVoiceListening() {
        speechManager.stopListening()
    }

    fun closeDynamicIsland() {
        _dynamicIslandState.value = DynamicIslandState(status = DynamicIslandStatus.CLOSE)
    }

    fun speakText(text: String) {
        if (prefs.isVoiceFeedbackEnabled) {
            speechManager.speak(text)
        }
    }

    fun sendMessage(input: String, isVoice: Boolean = false) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        // Quick redirection for Builder commands
        val lower = trimmed.lowercase()
        if (lower.startsWith("build app") || lower.startsWith("create app") || lower.startsWith("new app")) {
            navigateTo(ScreenDestination.APP_BUILDER)
            return
        }
        if (lower.startsWith("build website") || lower.startsWith("create website") || lower.startsWith("new website")) {
            navigateTo(ScreenDestination.WEBSITE_BUILDER)
            return
        }
        if (lower.contains("switch agent") || lower.contains("custom agent")) {
            navigateTo(ScreenDestination.CUSTOM_AGENTS)
            return
        }
        if (lower.contains("call me") || lower.contains("audio call") || lower.contains("call fuga") ||
            lower.contains("start a call") || lower.contains("voice call") || lower == "call") {
            startAudioCall()
            return
        }

        viewModelScope.launch {
            // 1. Insert user message
            val userMsg = ChatMessage(
                text = trimmed,
                isUser = true,
                isVoice = isVoice
            )
            chatRepo.addMessage(userMsg)

            // 2. Set dynamic island to thinking
            val agentName = activeAgent.value?.name ?: "Fuga"
            _dynamicIslandState.value = DynamicIslandState(
                status = DynamicIslandStatus.THINKING,
                headline = "$agentName Thinking...",
                subtitle = "Processing command"
            )

            val history = messages.value
            val memoryList = if (prefs.isMemoryEnabled) memories.value else emptyList()

            var streamSucceeded = false
            var streamedFullReply: GeminiAssistantResult? = null

            // 3. Attempt real-time streaming parsing via Firebase Vertex AI Repository
            try {
                vertexAIRepo.streamVoiceResponse(
                    userInput = trimmed,
                    history = history,
                    memories = memoryList,
                    customAgent = activeAgent.value
                ).collect { event ->
                    when (event) {
                        is VertexStreamingEvent.TextDelta -> {
                            // Update live subtitle if desired
                        }
                        is VertexStreamingEvent.SpeechSentenceReady -> {
                            streamSucceeded = true
                            if (prefs.isVoiceFeedbackEnabled) {
                                speechManager.speakChunk(event.sentence)
                            }
                        }
                        is VertexStreamingEvent.Complete -> {
                            streamSucceeded = true
                            streamedFullReply = event.result
                        }
                        is VertexStreamingEvent.Error -> {
                            // Caught below or fall back
                        }
                    }
                }
            } catch (e: Exception) {
                streamSucceeded = false
            }

            // 4. If streaming was successful and yielded a complete result, use it; otherwise fallback to geminiClient
            val fullReply = streamedFullReply
            val assistantResult: GeminiAssistantResult = if (streamSucceeded && fullReply != null) {
                fullReply
            } else {
                val apiKey = prefs.getStoredApiKey()
                val result = geminiClient.generateResponse(
                    userInput = trimmed,
                    history = history,
                    memories = memoryList,
                    apiKey = apiKey,
                    customAgent = activeAgent.value
                )
                result.getOrNull() ?: GeminiAssistantResult(
                    replyText = "I received your request.",
                    detectedAction = geminiClient.detectLocalAction(trimmed)
                )
            }

            val replyText = assistantResult.replyText
            val detectedAction = assistantResult.detectedAction

            val isPayment = detectedAction?.type == ActionType.PAYMENT
            val isSensitive = (detectedAction?.isSensitive == true || isPayment) && prefs.isBiometricSecurityEnabled

            // 5. Save assistant reply in database
            val assistantMsg = ChatMessage(
                text = replyText,
                isUser = false,
                isVoice = isVoice,
                actionType = detectedAction?.type?.name,
                actionDetail = detectedAction?.let { "${it.title}: ${it.target}" },
                actionStatus = if (isSensitive) "PENDING_BIOMETRIC" else if (detectedAction != null) "EXECUTED" else null,
                isSensitive = isSensitive
            )
            val insertedId = chatRepo.addMessage(assistantMsg)
            val savedMsg = assistantMsg.copy(id = insertedId)

            // 6. If not sensitive, execute immediately
            if (detectedAction != null && !isSensitive) {
                actionExecutor.execute(detectedAction)
            }

            // 7. If sensitive, fire biometric prompt
            if (isSensitive) {
                _uiEvents.emit(UiEvent.RequestBiometric(savedMsg))
            }

            // 8. Voice response (if stream didn't already speak sentence-by-sentence)
            if (prefs.isVoiceFeedbackEnabled && !streamSucceeded) {
                speechManager.speak(replyText)
            } else if (!prefs.isVoiceFeedbackEnabled) {
                _dynamicIslandState.value = DynamicIslandState(
                    status = DynamicIslandStatus.IDLE,
                    headline = "$agentName is ready",
                    subtitle = "Tap mic or say “Hey Fuga”"
                )
            }
        }
    }

    fun executeAction(message: ChatMessage) {
        viewModelScope.launch {
            val typeStr = message.actionType ?: return@launch
            val actionType = try {
                ActionType.valueOf(typeStr)
            } catch (e: Exception) {
                return@launch
            }
            val command = ActionCommand(
                type = actionType,
                title = message.actionDetail ?: actionType.label,
                description = "Manual trigger",
                isSensitive = message.isSensitive,
                target = message.actionDetail?.substringAfter(":")?.trim() ?: ""
            )
            val result = actionExecutor.execute(command)
            val updated = message.copy(actionStatus = "EXECUTED")
            chatRepo.updateMessage(updated)
            _uiEvents.emit(UiEvent.ShowToast(result.message))
        }
    }

    fun onBiometricAuthenticationSuccess(message: ChatMessage) {
        viewModelScope.launch {
            val amount = Regex("\\d+").find(message.actionDetail ?: "")?.value ?: "500"
            val recipient = message.actionDetail?.substringAfter("to ")?.substringBefore(":")?.trim() ?: "Recipient"
            val command = ActionCommand(
                type = ActionType.PAYMENT,
                title = "Payment to $recipient",
                description = "Biometrically verified transfer",
                isSensitive = false,
                target = recipient,
                param = amount
            )
            val result = actionExecutor.execute(command)
            val updated = message.copy(
                text = "${message.text}\n\n✓ Payment of ₹$amount to $recipient biometrically verified & approved.",
                actionStatus = "EXECUTED"
            )
            chatRepo.updateMessage(updated)
            if (prefs.isVoiceFeedbackEnabled) {
                speechManager.speak("Payment authorized and completed.")
            }
            _uiEvents.emit(UiEvent.ShowToast(result.message))
        }
    }

    // Custom Agent Management
    fun selectActiveAgent(id: Long) {
        viewModelScope.launch {
            customAgentRepo.setActiveAgent(id)
            _uiEvents.emit(UiEvent.ShowToast("Switched active agent"))
        }
    }

    fun createCustomAgent(
        name: String,
        role: String,
        prompt: String,
        icon: String,
        color: String,
        capabilities: String
    ) {
        viewModelScope.launch {
            customAgentRepo.createAgent(name, role, prompt, icon, color, capabilities)
            _uiEvents.emit(UiEvent.ShowToast("Custom Agent created"))
        }
    }

    fun deleteCustomAgent(id: Long) {
        viewModelScope.launch {
            customAgentRepo.deleteAgent(id)
        }
    }

    // App & Website Builders
    suspend fun generateApp(prompt: String, title: String): Map<String, String> {
        val apiKey = prefs.getStoredApiKey()
        return geminiClient.generateAppProjectCode(prompt, title, apiKey)
    }

    suspend fun generateWebsite(prompt: String, title: String): Map<String, String> {
        val apiKey = prefs.getStoredApiKey()
        return geminiClient.generateWebsiteProjectCode(prompt, title, apiKey)
    }

    fun saveGeneratedProject(title: String, prompt: String, files: Map<String, String>, type: ProjectType) {
        viewModelScope.launch {
            val filesJson = ProjectZipExporter.filesToJson(files)
            projectRepo.saveProject(
                GeneratedProject(
                    title = title,
                    description = prompt,
                    type = type,
                    prompt = prompt,
                    filesJson = filesJson
                )
            )
        }
    }

    fun addMemory(category: String, content: String) {
        viewModelScope.launch {
            memoryRepo.addMemory(category, content)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepo.deleteMemory(id)
        }
    }

    fun clearMemories() {
        viewModelScope.launch {
            memoryRepo.clearMemories()
        }
    }

    fun deleteChatMessage(id: Long) {
        viewModelScope.launch {
            chatRepo.deleteMessage(id)
        }
    }

    fun clearAllChatMessages() {
        viewModelScope.launch {
            chatRepo.clearHistory()
        }
    }

    // ==========================================
    // AUDIO CALL ENGINE
    // ==========================================

    fun startAudioCall() {
        wakeWordDetector.stopDetection()
        speechManager.stopListening()
        speechManager.stopSpeaking()

        _callStatus.value = CallStatus.CONNECTING
        _callDurationSeconds.value = 0
        _isCallMuted.value = false
        _isCallSpeakerOn.value = true
        _callTranscripts.value = emptyList()
        _callLiveCaption.value = "Connecting audio channel..."
        speechManager.setSpeakerphoneOn(true)

        navigateTo(ScreenDestination.AUDIO_CALL)

        // Start call duration timer
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive && _callStatus.value != CallStatus.ENDED) {
                delay(1000)
                if (_callStatus.value != CallStatus.CONNECTING && _callStatus.value != CallStatus.ENDED) {
                    _callDurationSeconds.value += 1
                }
            }
        }

        // Voice Greeting
        viewModelScope.launch {
            delay(500)
            val agentName = activeAgent.value?.name ?: "Fuga"
            val greeting = "Hello! $agentName is connected. I'm listening, how can I assist you?"
            _callLiveCaption.value = greeting
            _callTranscripts.value = listOf(
                CallTranscriptItem(speaker = agentName, text = greeting, isUser = false)
            )
            _callStatus.value = CallStatus.SPEAKING
            speechManager.speak(greeting)
        }
    }

    fun startCallListening() {
        if (_currentScreen.value != ScreenDestination.AUDIO_CALL || _isCallMuted.value) return
        _callStatus.value = CallStatus.LISTENING
        _callLiveCaption.value = "Listening to you..."
        speechManager.startListening { transcribedText ->
            if (_currentScreen.value == ScreenDestination.AUDIO_CALL && transcribedText.isNotBlank()) {
                handleCallUserInput(transcribedText)
            }
        }
    }

    private fun handleCallUserInput(userInput: String) {
        val trimmed = userInput.trim()
        if (trimmed.isEmpty()) return

        val userItem = CallTranscriptItem(speaker = "You", text = trimmed, isUser = true)
        _callTranscripts.value = _callTranscripts.value + userItem
        _callStatus.value = CallStatus.THINKING
        _callLiveCaption.value = "Thinking..."

        viewModelScope.launch {
            val agentName = activeAgent.value?.name ?: "Fuga"
            val history = messages.value
            val memoryList = if (prefs.isMemoryEnabled) memories.value else emptyList()

            var streamSucceeded = false
            var fullReply = ""

            try {
                vertexAIRepo.streamVoiceResponse(
                    userInput = trimmed,
                    history = history,
                    memories = memoryList,
                    customAgent = activeAgent.value
                ).collect { event ->
                    when (event) {
                        is VertexStreamingEvent.TextDelta -> {
                            fullReply += event.chunk
                            _callLiveCaption.value = fullReply
                        }
                        is VertexStreamingEvent.SpeechSentenceReady -> {
                            streamSucceeded = true
                            _callStatus.value = CallStatus.SPEAKING
                            speechManager.speakChunk(event.sentence)
                        }
                        is VertexStreamingEvent.Complete -> {
                            streamSucceeded = true
                            fullReply = event.result.replyText
                        }
                        is VertexStreamingEvent.Error -> {}
                    }
                }
            } catch (e: Exception) {
                streamSucceeded = false
            }

            if (!streamSucceeded || fullReply.isBlank()) {
                val apiKey = prefs.getStoredApiKey()
                val res = geminiClient.generateResponse(
                    userInput = trimmed,
                    history = history,
                    memories = memoryList,
                    apiKey = apiKey,
                    customAgent = activeAgent.value
                )
                fullReply = res.getOrNull()?.replyText ?: "I received that, let's keep going."
                _callStatus.value = CallStatus.SPEAKING
                _callLiveCaption.value = fullReply
                speechManager.speak(fullReply)
            }

            val assistantItem = CallTranscriptItem(speaker = agentName, text = fullReply, isUser = false)
            _callTranscripts.value = _callTranscripts.value + assistantItem
        }
    }

    fun toggleCallMute() {
        val newMuted = !_isCallMuted.value
        _isCallMuted.value = newMuted
        if (newMuted) {
            speechManager.stopListening()
            _callStatus.value = CallStatus.MUTED
            _callLiveCaption.value = "Microphone Muted"
        } else {
            startCallListening()
        }
    }

    fun toggleCallSpeaker() {
        val newSpeaker = !_isCallSpeakerOn.value
        _isCallSpeakerOn.value = newSpeaker
        speechManager.setSpeakerphoneOn(newSpeaker)
    }

    fun interruptCallSpeech() {
        speechManager.stopSpeaking()
        if (!_isCallMuted.value) {
            startCallListening()
        } else {
            _callStatus.value = CallStatus.MUTED
        }
    }

    fun endAudioCall() {
        callTimerJob?.cancel()
        callTimerJob = null
        speechManager.stopSpeaking()
        speechManager.stopListening()

        val durationFormatted = String.format(
            java.util.Locale.US,
            "%02d:%02d",
            _callDurationSeconds.value / 60,
            _callDurationSeconds.value % 60
        )
        val turnCount = _callTranscripts.value.size
        _callStatus.value = CallStatus.ENDED

        viewModelScope.launch {
            if (turnCount > 1) {
                val callSummaryMsg = ChatMessage(
                    text = "📞 Voice Call with Fuga AI ended\nDuration: $durationFormatted • $turnCount conversation turns.",
                    isUser = false,
                    isVoice = true,
                    actionStatus = "EXECUTED"
                )
                chatRepo.addMessage(callSummaryMsg)
            }
        }

        navigateTo(ScreenDestination.HOME)
        updateAlwaysReadyState()
    }

    // ==========================================
    // ALWAYS READY HANDS-FREE ENGINE
    // ==========================================

    fun updateAlwaysReadyState() {
        val shouldBeReady = prefs.isAlwaysReadyEnabled &&
                prefs.isWakeWordEnabled &&
                _currentScreen.value == ScreenDestination.HOME &&
                _callStatus.value == CallStatus.IDLE

        if (shouldBeReady) {
            if (!_isAlwaysReadyActive.value) {
                _isAlwaysReadyActive.value = true
                wakeWordDetector.startDetection { detectedPhrase ->
                    onWakeWordDetected(detectedPhrase)
                }
            }
        } else {
            if (_isAlwaysReadyActive.value) {
                _isAlwaysReadyActive.value = false
                wakeWordDetector.stopDetection()
            }
        }
    }

    private fun onWakeWordDetected(phrase: String) {
        _isAlwaysReadyActive.value = false
        _dynamicIslandState.value = DynamicIslandState(
            status = DynamicIslandStatus.LISTENING,
            headline = "Fuga Listening...",
            subtitle = "“$phrase” detected • Say your command"
        )
        startVoiceListening()
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.isWakeWordEnabled = enabled
        updateAlwaysReadyState()
    }

    fun setAlwaysReadyEnabled(enabled: Boolean) {
        prefs.isAlwaysReadyEnabled = enabled
        updateAlwaysReadyState()
    }

    fun simulateWakeWordTrigger() {
        wakeWordDetector.simulateWakeWordTrigger()
    }

    fun rearmAlwaysReadyListener() {
        if (_currentScreen.value == ScreenDestination.HOME && prefs.isAlwaysReadyEnabled && prefs.isWakeWordEnabled) {
            wakeWordDetector.resumeDetection()
            _isAlwaysReadyActive.value = true
        }
    }

    override fun onCleared() {
        callTimerJob?.cancel()
        wakeWordDetector.destroy()
        speechManager.destroy()
        super.onCleared()
    }
}
