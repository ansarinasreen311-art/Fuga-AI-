package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class FugaWakeWordDetector(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting: StateFlow<Boolean> = _isDetecting.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _lastDetectedKeyword = MutableStateFlow<String?>(null)
    val lastDetectedKeyword: StateFlow<String?> = _lastDetectedKeyword.asStateFlow()

    private var onKeywordDetectedListener: ((String) -> Unit)? = null
    private var isIntentionallyStopped = false

    companion object {
        private val WAKE_PHRASES = listOf(
            "hey fuga",
            "fuga",
            "hey foga",
            "ok fuga",
            "hi fuga",
            "hello fuga",
            "hey phuga"
        )
    }

    fun startDetection(onKeywordDetected: (String) -> Unit) {
        if (_isDetecting.value) return
        isIntentionallyStopped = false
        onKeywordDetectedListener = onKeywordDetected
        _isDetecting.value = true

        initAndListen()
    }

    private fun initAndListen() {
        if (isIntentionallyStopped) return

        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    _isDetecting.value = false
                    return@post
                }

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {
                            val normalized = (rmsdB / 10f).coerceIn(0f, 1f)
                            _audioLevel.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _audioLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            _audioLevel.value = 0f
                            // Re-arm detector quietly on silence/no-match unless stopped
                            if (!isIntentionallyStopped) {
                                mainHandler.postDelayed({ initAndListen() }, 250)
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            _audioLevel.value = 0f
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            checkMatches(matches)
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            checkMatches(matches)
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                if (!isIntentionallyStopped) {
                    mainHandler.postDelayed({ initAndListen() }, 500)
                }
            }
        }
    }

    private fun checkMatches(matches: List<String>?) {
        if (matches == null || isIntentionallyStopped) return
        for (phrase in matches) {
            val lower = phrase.lowercase().trim()
            for (wakePhrase in WAKE_PHRASES) {
                if (lower.contains(wakePhrase)) {
                    triggerWakeWordDetected(wakePhrase)
                    return
                }
            }
        }

        // If no wake word found, re-arm
        if (!isIntentionallyStopped) {
            mainHandler.postDelayed({ initAndListen() }, 200)
        }
    }

    private fun triggerWakeWordDetected(phrase: String) {
        _lastDetectedKeyword.value = phrase
        playWakeToneAndVibrate()
        onKeywordDetectedListener?.invoke(phrase)

        // Temporarily pause detector while active assistant conversation runs
        pauseDetection()
    }

    fun simulateWakeWordTrigger() {
        triggerWakeWordDetected("hey fuga")
    }

    private fun playWakeToneAndVibrate() {
        try {
            val prefs = com.example.data.pref.FugaPreferences(context)
            // Haptic vibration
            if (prefs.isWakeWordHapticEnabled) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80)
                }
            }

            // Quick pleasant activation chime
            if (prefs.isWakeWordChimeEnabled) {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
            }
        } catch (e: Throwable) {
            // Ignore hardware audio/vibe errors
        }
    }

    fun pauseDetection() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        _audioLevel.value = 0f
    }

    fun resumeDetection() {
        if (!isIntentionallyStopped) {
            initAndListen()
        }
    }

    fun stopDetection() {
        isIntentionallyStopped = true
        _isDetecting.value = false
        _audioLevel.value = 0f
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {}
        speechRecognizer = null
        mainHandler.removeCallbacksAndMessages(null)
    }

    fun destroy() {
        stopDetection()
    }
}
