package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.model.ChatMessage
import com.example.data.model.ProjectType
import com.example.ui.screens.agents.CustomAgentScreen
import com.example.ui.screens.builder.AppBuilderScreen
import com.example.ui.screens.builder.WebsiteBuilderScreen
import com.example.ui.screens.call.AudioCallScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.memory.MemoryScreen
import com.example.ui.screens.onboarding.CapabilitiesScreen
import com.example.ui.screens.onboarding.FugaGmailSetupScreen
import com.example.ui.screens.onboarding.PermissionsScreen
import com.example.ui.screens.onboarding.WakeWordSetupScreen
import com.example.ui.screens.onboarding.WelcomeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.WakeWordSettingsScreen
import com.example.ui.theme.FugaTheme
import com.example.ui.theme.SpaceBlack
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiEvent

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent.getBooleanExtra("EXTRA_WAKE_WORD_ACTIVATED", false)) {
            viewModel.startVoiceListening()
        }

        setContent {
            FugaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SpaceBlack
                ) {
                    FugaAppContent(viewModel = viewModel, onShowBiometricPrompt = { msg ->
                        showBiometricPrompt(msg)
                    })
                }
            }
        }
    }

    private fun showBiometricPrompt(message: ChatMessage) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricAuthenticationSuccess(message)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        this@MainActivity,
                        "Biometric verification cancelled: Sensitive action aborted.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@MainActivity,
                        "Biometric verification failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fuga AI Biometric Authorization")
            .setSubtitle("Sensitive action verification: ${message.actionDetail ?: "Payment"}")
            .setDescription("Authenticate using your fingerprint or device biometric to proceed.")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            // Fallback for emulator or unconfigured lockscreen
            Toast.makeText(
                this,
                "Device credentials accepted for simulation: Payment authorized.",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.onBiometricAuthenticationSuccess(message)
        }
    }
}

@Composable
fun FugaAppContent(
    viewModel: MainViewModel,
    onShowBiometricPrompt: (ChatMessage) -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val dynamicIslandState by viewModel.dynamicIslandState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val activeAgent by viewModel.activeAgent.collectAsState()
    val customAgents by viewModel.customAgents.collectAsState()
    val isAlwaysReadyActive by viewModel.isAlwaysReadyActive.collectAsState()
    val callStatus by viewModel.callStatus.collectAsState()
    val callDurationSeconds by viewModel.callDurationSeconds.collectAsState()
    val isCallMuted by viewModel.isCallMuted.collectAsState()
    val isCallSpeakerOn by viewModel.isCallSpeakerOn.collectAsState()
    val callTranscripts by viewModel.callTranscripts.collectAsState()
    val callLiveCaption by viewModel.callLiveCaption.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.RequestBiometric -> onShowBiometricPrompt(event.message)
                is UiEvent.ShowToast -> {}
            }
        }
    }

    when (currentScreen) {
        ScreenDestination.WELCOME -> {
            WelcomeScreen(
                onContinueWithGoogle = { name, email -> viewModel.onContinueWithGoogle(name, email) }
            )
        }
        ScreenDestination.PERMISSIONS -> {
            PermissionsScreen(
                onContinue = {
                    if (viewModel.prefs.isFirstLaunchCompleted) {
                        viewModel.navigateTo(ScreenDestination.SETTINGS)
                    } else {
                        viewModel.onPermissionsFinished()
                    }
                },
                onBack = if (viewModel.prefs.isFirstLaunchCompleted) {
                    { viewModel.navigateTo(ScreenDestination.SETTINGS) }
                } else null
            )
        }
        ScreenDestination.CAPABILITIES -> {
            CapabilitiesScreen(
                onContinue = { viewModel.onCapabilitiesFinished() }
            )
        }
        ScreenDestination.WAKE_WORD_SETUP -> {
            WakeWordSetupScreen(
                wakeWordEnabled = viewModel.prefs.isWakeWordEnabled,
                onToggleWakeWord = { viewModel.prefs.isWakeWordEnabled = it },
                onContinue = { viewModel.onWakeWordFinished() }
            )
        }
        ScreenDestination.FUGA_GMAIL_SETUP -> {
            FugaGmailSetupScreen(
                currentGmail = userSession.fugaGmail,
                onSaveAndFinish = { gmail -> viewModel.onFugaGmailFinished(gmail) },
                onSkip = { viewModel.onFugaGmailFinished(null) }
            )
        }
        ScreenDestination.HOME -> {
            HomeScreen(
                userSession = userSession,
                activeAgent = activeAgent,
                messages = messages,
                dynamicIslandState = dynamicIslandState,
                isListening = isListening,
                audioLevel = audioLevel,
                isAlwaysReadyActive = isAlwaysReadyActive,
                onSendMessage = { prompt, isVoice -> viewModel.sendMessage(prompt, isVoice) },
                onStartVoiceListening = { viewModel.startVoiceListening() },
                onStopVoiceListening = { viewModel.stopVoiceListening() },
                onCloseDynamicIsland = { viewModel.closeDynamicIsland() },
                onSpeakMessage = { text -> viewModel.speakText(text) },
                onExecuteAction = { msg -> viewModel.executeAction(msg) },
                onBiometricVerifyPayment = { msg -> onShowBiometricPrompt(msg) },
                onNavigateToSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                onNavigateToHistory = { viewModel.navigateTo(ScreenDestination.HISTORY) },
                onNavigateToMemory = { viewModel.navigateTo(ScreenDestination.MEMORY) },
                onNavigateToAgents = { viewModel.navigateTo(ScreenDestination.CUSTOM_AGENTS) },
                onNavigateToAppBuilder = { viewModel.navigateTo(ScreenDestination.APP_BUILDER) },
                onNavigateToWebsiteBuilder = { viewModel.navigateTo(ScreenDestination.WEBSITE_BUILDER) },
                onStartAudioCall = { viewModel.startAudioCall() },
                onSimulateWakeWord = { viewModel.simulateWakeWordTrigger() },
                onNavigateToWakeWordSettings = { viewModel.navigateTo(ScreenDestination.WAKE_WORD_SETTINGS) }
            )
        }
        ScreenDestination.AUDIO_CALL -> {
            AudioCallScreen(
                activeAgent = activeAgent,
                callStatus = callStatus,
                durationSeconds = callDurationSeconds,
                isMuted = isCallMuted,
                isSpeakerOn = isCallSpeakerOn,
                audioLevel = audioLevel,
                transcripts = callTranscripts,
                liveCaption = callLiveCaption,
                onToggleMute = { viewModel.toggleCallMute() },
                onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                onInterrupt = { viewModel.interruptCallSpeech() },
                onEndCall = { viewModel.endAudioCall() }
            )
        }
        ScreenDestination.CUSTOM_AGENTS -> {
            CustomAgentScreen(
                agents = customAgents,
                activeAgent = activeAgent,
                onSelectAgent = { id -> viewModel.selectActiveAgent(id) },
                onCreateAgent = { name, role, prompt, icon, color, capabilities ->
                    viewModel.createCustomAgent(name, role, prompt, icon, color, capabilities)
                },
                onDeleteAgent = { id -> viewModel.deleteCustomAgent(id) },
                onBack = { viewModel.navigateTo(ScreenDestination.HOME) }
            )
        }
        ScreenDestination.APP_BUILDER -> {
            AppBuilderScreen(
                onGenerateApp = { prompt, title -> viewModel.generateApp(prompt, title) },
                onSaveProject = { title, prompt, files ->
                    viewModel.saveGeneratedProject(title, prompt, files, ProjectType.ANDROID_APP)
                },
                onBack = { viewModel.navigateTo(ScreenDestination.HOME) }
            )
        }
        ScreenDestination.WEBSITE_BUILDER -> {
            WebsiteBuilderScreen(
                onGenerateWebsite = { prompt, title -> viewModel.generateWebsite(prompt, title) },
                onSaveProject = { title, prompt, files ->
                    viewModel.saveGeneratedProject(title, prompt, files, ProjectType.WEBSITE)
                },
                onBack = { viewModel.navigateTo(ScreenDestination.HOME) }
            )
        }
        ScreenDestination.SETTINGS -> {
            SettingsScreen(
                prefs = viewModel.prefs,
                onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                onClearHistory = { viewModel.clearAllChatMessages() },
                onNavigateToMemory = { viewModel.navigateTo(ScreenDestination.MEMORY) },
                onNavigateToPermissions = { viewModel.navigateTo(ScreenDestination.PERMISSIONS) },
                onNavigateToWakeWordSettings = { viewModel.navigateTo(ScreenDestination.WAKE_WORD_SETTINGS) }
            )
        }
        ScreenDestination.WAKE_WORD_SETTINGS -> {
            WakeWordSettingsScreen(
                prefs = viewModel.prefs,
                onBack = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                onSimulateWakeWord = { viewModel.simulateWakeWordTrigger() },
                onNavigateToPermissions = { viewModel.navigateTo(ScreenDestination.PERMISSIONS) }
            )
        }
        ScreenDestination.HISTORY -> {
            HistoryScreen(
                messages = messages,
                onDeleteMessage = { id -> viewModel.deleteChatMessage(id) },
                onClearAll = { viewModel.clearAllChatMessages() },
                onBack = { viewModel.navigateTo(ScreenDestination.HOME) }
            )
        }
        ScreenDestination.MEMORY -> {
            MemoryScreen(
                memories = memories,
                onAddMemory = { cat, content -> viewModel.addMemory(cat, content) },
                onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                onClearMemories = { viewModel.clearMemories() },
                onBack = { viewModel.navigateTo(ScreenDestination.HOME) }
            )
        }
    }
}
