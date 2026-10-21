package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class FrenchSpeechEngine(private val context: Context) : RecognitionListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isContinuousMode = MutableStateFlow(false)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _speechRate = MutableStateFlow(0.85f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private var currentLanguage = "auto"
    private var onSpeechResultListener: ((String) -> Unit)? = null
    private var isPaused = false


    private val _voiceName = MutableStateFlow("Natural French (Parisian)")
    val voiceName: StateFlow<String> = _voiceName.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _frenchAccentForEnglish = MutableStateFlow(true)
    val frenchAccentForEnglish: StateFlow<Boolean> = _frenchAccentForEnglish.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureBestHumanFrenchVoice()
                tts?.setSpeechRate(_speechRate.value)
                tts?.setPitch(_pitch.value)
            } else {
                Log.e("FrenchSpeechEngine", "TTS initialization failed")
            }
        }
    }

    private var humanFrenchVoice: android.speech.tts.Voice? = null

    private fun configureBestHumanFrenchVoice() {
        try {
            val allVoices = tts?.voices
            if (!allVoices.isNullOrEmpty()) {
                // Find high-quality human French voices (Google Neural, network, or high-definition)
                val frenchVoices = allVoices.filter { voice ->
                    voice.locale.language.equals("fr", ignoreCase = true)
                }

                // Prioritize natural/network neural voices with high quality
                val bestVoice = frenchVoices.maxWithOrNull(
                    compareBy<android.speech.tts.Voice> {
                        if (it.name.contains("network", ignoreCase = true)) 2 else 0
                    }.thenBy {
                        if (it.name.contains("neural", ignoreCase = true) || it.name.contains("natural", ignoreCase = true)) 2 else 0
                    }.thenBy {
                        it.quality
                    }.thenBy {
                        if (it.locale.country.equals("FR", ignoreCase = true)) 1 else 0
                    }
                )

                if (bestVoice != null) {
                    humanFrenchVoice = bestVoice
                    tts?.voice = bestVoice
                    _voiceName.value = "Parisian French (${bestVoice.name.substringAfterLast("-", "Natural")})"
                    isTtsReady = true
                    Log.d("FrenchSpeechEngine", "Configured human French voice: ${bestVoice.name}")
                    return
                }
            }

            val result = tts?.setLanguage(Locale.FRANCE)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!isTtsReady) {
                tts?.setLanguage(Locale.FRENCH)
                isTtsReady = true
            }
            _voiceName.value = "Standard Parisian French"
        } catch (e: Exception) {
            Log.w("FrenchSpeechEngine", "Error selecting voice", e)
            tts?.setLanguage(Locale.FRANCE)
            isTtsReady = true
        }
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        _pitch.value = pitch
        tts?.setPitch(pitch)
    }

    fun setFrenchAccentForEnglish(enabled: Boolean) {
        _frenchAccentForEnglish.value = enabled
    }

    fun speak(text: String, rate: Float? = null, locale: Locale = Locale.FRENCH, onComplete: (() -> Unit)? = null) {
        val currentRate = rate ?: _speechRate.value
        tts?.setLanguage(locale)
        if ((locale.language.equals("fr", ignoreCase = true) || _frenchAccentForEnglish.value) && humanFrenchVoice != null) {
            try {
                tts?.voice = humanFrenchVoice
            } catch (ignored: Exception) {}
        }
        tts?.setSpeechRate(currentRate)
        tts?.setPitch(_pitch.value)

        if (onComplete != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    onComplete()
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {}
            })
        }

        val utteranceId = "tts_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun speakTranslation(text: String, isEnglishTranslation: Boolean = false, rate: Float? = null, onComplete: (() -> Unit)? = null) {
        if (isEnglishTranslation) {
            // When reading an English translation with French accent:
            // Use French locale engine so English words are pronounced with an authentic, human French accent!
            if (_frenchAccentForEnglish.value) {
                speak(text, rate = rate ?: 0.90f, locale = Locale.FRANCE, onComplete = onComplete)
            } else {
                speak(text, rate = rate ?: 0.90f, locale = Locale.US, onComplete = onComplete)
            }
        } else {
            // Authentic Parisian French voice
            speak(text, rate = rate ?: 0.88f, locale = Locale.FRENCH, onComplete = onComplete)
        }
    }

    private val _isRecordingSession = MutableStateFlow(false)
    val isRecordingSession: StateFlow<Boolean> = _isRecordingSession.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val accumulatedSessionTranscript = StringBuilder()
    private var pendingSessionCallback: ((String) -> Unit)? = null

    fun stopSpeaking() {
        tts?.stop()
    }

    /**
     * Start a manual recording session.
     * The engine keeps listening and accumulating spoken text until finishManualSessionAsync() or cancelManualSession() is called.
     */
    fun startManualSession(language: String = "auto") {
        stopSpeaking()
        currentLanguage = language
        _isContinuousMode.value = false
        _isRecordingSession.value = true
        _isListening.value = true
        accumulatedSessionTranscript.clear()
        _liveTranscript.value = ""
        _audioRms.value = 0f
        isPaused = false
        onSpeechResultListener = null
        pendingSessionCallback = null

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("FrenchSpeechEngine", "Speech recognition not available on this device")
            return
        }

        startRecognitionInternal()
    }

    /**
     * Stop the manual recording session asynchronously.
     * Tells SpeechRecognizer to stop listening and flushes the remaining audio to onResults,
     * delivering the complete accumulated transcript without discarding the last spoken words.
     */
    fun finishManualSessionAsync(onComplete: (String) -> Unit) {
        _isRecordingSession.value = false
        _isListening.value = false
        _audioRms.value = 0f
        pendingSessionCallback = onComplete

        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("FrenchSpeechEngine", "Error stopping listening on finish", e)
        }

        // Safety fallback: if onResults doesn't fire within 650ms, deliver what was captured
        mainHandler.postDelayed({
            pendingSessionCallback?.let { callback ->
                pendingSessionCallback = null
                val finalResult = _liveTranscript.value.trim()
                cleanupRecognizer()
                callback(finalResult)
            }
        }, 650)
    }

    /**
     * Synchronous finish for testing and immediate extraction.
     */
    fun finishManualSession(): String {
        _isRecordingSession.value = false
        _isListening.value = false
        _audioRms.value = 0f
        mainHandler.removeCallbacksAndMessages(null)
        pendingSessionCallback = null
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("FrenchSpeechEngine", "Error finishing manual session", e)
        }
        val result = _liveTranscript.value.trim()
        cleanupRecognizer()
        return result
    }

    private fun cleanupRecognizer() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("FrenchSpeechEngine", "Error destroying speech recognizer", e)
        }
    }

    /**
     * Cancel the manual recording session without keeping the speech.
     */
    fun cancelManualSession() {
        _isRecordingSession.value = false
        _isListening.value = false
        _audioRms.value = 0f
        accumulatedSessionTranscript.clear()
        _liveTranscript.value = ""
        pendingSessionCallback = null
        mainHandler.removeCallbacksAndMessages(null)
        cleanupRecognizer()
    }

    fun startListening(
        language: String = "auto",
        continuous: Boolean = false,
        onResult: ((String) -> Unit)? = null
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("FrenchSpeechEngine", "Speech recognition not available on this device")
            return
        }

        currentLanguage = language
        _isContinuousMode.value = continuous
        isPaused = false
        onSpeechResultListener = onResult

        startRecognitionInternal()
    }

    private fun startRecognitionInternal() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@FrenchSpeechEngine)
            }

            val resolvedPrimary = when {
                currentLanguage.startsWith("fr", ignoreCase = true) -> "fr-FR"
                currentLanguage.startsWith("en", ignoreCase = true) -> "en-US"
                else -> {
                    // For auto, default to English with French fallback
                    if (Locale.getDefault().language == "fr") "fr-FR" else "en-US"
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, resolvedPrimary)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, resolvedPrimary)
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("fr-FR", "en-US"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e("FrenchSpeechEngine", "Error starting listening", e)
            if (!_isRecordingSession.value) {
                _isListening.value = false
            }
        }
    }

    fun pauseListening() {
        isPaused = true
        _isListening.value = false
        speechRecognizer?.stopListening()
    }

    fun resumeListening() {
        if (isPaused) {
            isPaused = false
            startRecognitionInternal()
        }
    }

    fun stopListening() {
        _isRecordingSession.value = false
        _isContinuousMode.value = false
        isPaused = false
        mainHandler.removeCallbacksAndMessages(null)
        pendingSessionCallback = null
        cleanupRecognizer()
        _isListening.value = false
    }

    // Speech recognition callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
    }

    override fun onBeginningOfSpeech() {
        _isListening.value = true
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (_isListening.value || _isRecordingSession.value) {
            _audioRms.value = rmsdB
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        // Only set listening to false if NOT in a manual recording session!
        if (!_isRecordingSession.value && pendingSessionCallback == null) {
            _isListening.value = false
        }
    }

    override fun onError(error: Int) {
        Log.d("FrenchSpeechEngine", "SpeechRecognizer error code: $error")

        // If a finish callback is pending, deliver whatever was captured so far
        val pending = pendingSessionCallback
        if (pending != null) {
            pendingSessionCallback = null
            mainHandler.removeCallbacksAndMessages(null)
            val current = _liveTranscript.value.trim()
            cleanupRecognizer()
            pending(current)
            return
        }

        if (_isRecordingSession.value) {
            // In manual recording session, seamlessly keep listening across pauses/timeouts
            mainHandler.postDelayed({
                if (_isRecordingSession.value) {
                    startRecognitionInternal()
                }
            }, 200)
            return
        }

        _isListening.value = false

        // If continuous mode is active and not manually stopped/paused, auto-recover
        if (_isContinuousMode.value && !isPaused) {
            mainHandler.postDelayed({
                if (_isContinuousMode.value && !isPaused) {
                    startRecognitionInternal()
                }
            }, 400)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
        val bestResult = if (matches.isNotEmpty()) {
            if (currentLanguage.startsWith("fr", ignoreCase = true)) {
                AmericanFrenchPhoneticAdapter.resolveBestFrenchMatch(matches)
            } else {
                matches.first()
            }
        } else ""

        if (bestResult.isNotBlank()) {
            if (accumulatedSessionTranscript.isNotEmpty()) {
                accumulatedSessionTranscript.append(" ")
            }
            accumulatedSessionTranscript.append(bestResult)
            _liveTranscript.value = accumulatedSessionTranscript.toString().trim()
        }

        // Check if user tapped finish and is waiting for the flushed results
        val pending = pendingSessionCallback
        if (pending != null) {
            pendingSessionCallback = null
            mainHandler.removeCallbacksAndMessages(null)
            val fullResult = _liveTranscript.value.trim()
            cleanupRecognizer()
            pending(fullResult)
            return
        }

        if (_isRecordingSession.value) {
            // Seamlessly keep listening until the user touches the mic to finish
            mainHandler.postDelayed({
                if (_isRecordingSession.value) {
                    startRecognitionInternal()
                }
            }, 150)
            return
        }

        _isListening.value = false
        if (bestResult.isNotBlank()) {
            _liveTranscript.value = bestResult
            onSpeechResultListener?.invoke(bestResult)
        }

        // In continuous mode, continue listening for the next phrase
        if (_isContinuousMode.value && !isPaused) {
            mainHandler.postDelayed({
                if (_isContinuousMode.value && !isPaused) {
                    startRecognitionInternal()
                }
            }, 300)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.firstOrNull()?.let { match ->
            if (match.isNotBlank()) {
                val adapted = if (currentLanguage.startsWith("fr", ignoreCase = true)) {
                    AmericanFrenchPhoneticAdapter.normalizeAmericanFrench(match)
                } else match

                if (_isRecordingSession.value) {
                    val prefix = accumulatedSessionTranscript.toString().trim()
                    _liveTranscript.value = if (prefix.isNotEmpty()) "$prefix $adapted" else adapted
                } else {
                    _liveTranscript.value = adapted
                }
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        stopListening()
        tts?.shutdown()
    }
}
