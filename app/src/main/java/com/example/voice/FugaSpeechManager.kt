package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class FugaSpeechManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _wakeWordDetected = MutableStateFlow(false)
    val wakeWordDetected: StateFlow<Boolean> = _wakeWordDetected.asStateFlow()

    private var onSpeechResultListener: ((String) -> Unit)? = null
    private var onWakeWordTriggered: (() -> Unit)? = null
    var onSpeakingCompletedListener: (() -> Unit)? = null

    // Streaming speech queue
    private val speechQueue = ConcurrentLinkedQueue<String>()
    private val utteranceCounter = AtomicInteger(0)
    private var activeUtterances = AtomicInteger(0)

    var isWakeWordListening = false
        private set

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.setSpeechRate(1.05f)
                textToSpeech?.setPitch(1.0f)
                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        val remaining = activeUtterances.decrementAndGet()
                        if (remaining <= 0) {
                            activeUtterances.set(0)
                            _isSpeaking.value = false
                            onSpeakingCompletedListener?.invoke()
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        val remaining = activeUtterances.decrementAndGet()
                        if (remaining <= 0) {
                            activeUtterances.set(0)
                            _isSpeaking.value = false
                            onSpeakingCompletedListener?.invoke()
                        }
                    }
                })
            }
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        stopWakeWordDetection()
        stopSpeaking()
        onSpeechResultListener = onResult

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        _audioRms.value = (rmsdB / 10f).coerceIn(0.05f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioRms.value = 0f
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioRms.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty()) {
                            onSpeechResultListener?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrEmpty() && (text.contains("hey fuga", ignoreCase = true) || text.contains("fuga", ignoreCase = true))) {
                            // Wake word triggered
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun startWakeWordDetection(onTrigger: () -> Unit) {
        if (isWakeWordListening) return
        isWakeWordListening = true
        onWakeWordTriggered = onTrigger

        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        if (isWakeWordListening) {
                            _audioRms.value = (rmsdB / 10f).coerceIn(0.02f, 0.4f)
                        }
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        if (isWakeWordListening) {
                            restartWakeWordListener()
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.joinToString(" ")?.lowercase() ?: ""
                        if (text.contains("hey fuga") || text.contains("fuga") || text.contains("hey fooga")) {
                            _wakeWordDetected.value = true
                            stopWakeWordDetection()
                            onWakeWordTriggered?.invoke()
                        } else if (isWakeWordListening) {
                            restartWakeWordListener()
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.joinToString(" ")?.lowercase() ?: ""
                        if (text.contains("hey fuga") || text.contains("fuga") || text.contains("hey fooga")) {
                            _wakeWordDetected.value = true
                            stopWakeWordDetection()
                            onWakeWordTriggered?.invoke()
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isWakeWordListening = false
        }
    }

    private fun restartWakeWordListener() {
        if (!isWakeWordListening) return
        try {
            val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isWakeWordListening = false
        }
    }

    fun stopWakeWordDetection() {
        isWakeWordListening = false
        _wakeWordDetected.value = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
    }

    /**
     * Speaks the full text by flushing the speech queue.
     */
    fun speak(text: String) {
        if (!isTtsReady || text.isBlank()) return
        stopSpeaking()
        val cleanText = text.replace(Regex("<!--ACTION:.*?-->"), "").trim()
        val params = Bundle()
        val utteranceId = "fuga_tts_${utteranceCounter.incrementAndGet()}"
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        activeUtterances.set(1)
        _isSpeaking.value = true
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Enqueues a streamed sentence or speech chunk for smooth real-time voice synthesis.
     */
    fun speakChunk(chunk: String) {
        if (!isTtsReady || chunk.isBlank()) return
        val cleanChunk = chunk.replace(Regex("<!--ACTION:.*?-->"), "").trim()
        if (cleanChunk.isBlank()) return

        val utteranceId = "fuga_chunk_${utteranceCounter.incrementAndGet()}"
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        activeUtterances.incrementAndGet()
        _isSpeaking.value = true
        textToSpeech?.speak(cleanChunk, TextToSpeech.QUEUE_ADD, params, utteranceId)
    }

    fun stopSpeaking() {
        speechQueue.clear()
        activeUtterances.set(0)
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {}
        _isSpeaking.value = false
    }

    fun setSpeakerphoneOn(on: Boolean) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.isSpeakerphoneOn = on
        } catch (e: Exception) {}
    }

    fun destroy() {
        stopListening()
        stopWakeWordDetection()
        stopSpeaking()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
