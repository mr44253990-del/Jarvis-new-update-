package com.example.voice

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    // "no idal always voice run" - Continuous listening mode
    var isAlwaysVoiceRun: Boolean = true
    var onSpeakingFinished: (() -> Unit)? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _rmsAudioLevel = MutableStateFlow(0f)
    val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

    private var isManuallyStopped = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                _isListening.value = true
                            }

                            override fun onBeginningOfSpeech() {
                                _isListening.value = true
                            }

                            override fun onRmsChanged(rmsdB: Float) {
                                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                                _rmsAudioLevel.value = normalized
                            }

                            override fun onBufferReceived(buffer: ByteArray?) {}

                            override fun onEndOfSpeech() {
                                _isListening.value = false
                                _rmsAudioLevel.value = 0f
                            }

                            override fun onError(error: Int) {
                                _isListening.value = false
                                _rmsAudioLevel.value = 0f

                                // If in Always Voice Run mode, automatically retry/restart listening
                                if (isAlwaysVoiceRun && !isManuallyStopped && !_isSpeaking.value) {
                                    mainHandler.postDelayed({
                                        if (isAlwaysVoiceRun && !isManuallyStopped && !_isSpeaking.value) {
                                            startListening()
                                        }
                                    }, 400)
                                    return
                                }

                                val msg = when (error) {
                                    SpeechRecognizer.ERROR_AUDIO -> "অডিও রেকর্ডিং ত্রুটি"
                                    SpeechRecognizer.ERROR_NO_MATCH -> "কোনো কথা শনাক্ত করা যায়নি"
                                    SpeechRecognizer.ERROR_NETWORK -> "নেটওয়ার্ক কানেকশন ত্রুটি"
                                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোন পারমিশন প্রয়োজন"
                                    else -> "স্পিচ এরর কোড: $error"
                                }
                                onError(msg)
                            }

                            override fun onResults(results: Bundle?) {
                                _isListening.value = false
                                _rmsAudioLevel.value = 0f
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull() ?: ""
                                if (text.isNotBlank()) {
                                    onSpeechResult(text)
                                } else if (isAlwaysVoiceRun && !isManuallyStopped && !_isSpeaking.value) {
                                    // Empty results, loop back to listen
                                    mainHandler.postDelayed({
                                        if (isAlwaysVoiceRun && !isManuallyStopped && !_isSpeaking.value) {
                                            startListening()
                                        }
                                    }, 350)
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {}

                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            val bengaliLocale = Locale.Builder().setLanguage("bn").setRegion("BD").build()
            val result = tts?.setLanguage(bengaliLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val altBn = Locale.Builder().setLanguage("bn").setRegion("IN").build()
                if (tts?.setLanguage(altBn) == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    mainHandler.post {
                        onSpeakingFinished?.invoke()
                        // "no idal always voice run" - Auto restart listening after Archer finishes speaking!
                        if (isAlwaysVoiceRun && !isManuallyStopped) {
                            mainHandler.postDelayed({
                                if (isAlwaysVoiceRun && !isManuallyStopped && !_isSpeaking.value) {
                                    startListening()
                                }
                            }, 500)
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    if (isAlwaysVoiceRun && !isManuallyStopped) {
                        mainHandler.postDelayed({
                            if (isAlwaysVoiceRun && !isManuallyStopped) {
                                startListening()
                            }
                        }, 500)
                    }
                }
            })
        }
    }

    fun startListening() {
        isManuallyStopped = false
        stopSpeaking()

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-IN", "en-US"))
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                _isListening.value = false
                // Re-init recognizer on error
                initSpeechRecognizer()
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            _isListening.value = false
            _rmsAudioLevel.value = 0f
        }
    }

    fun speak(text: String, rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isTtsReady || text.isBlank()) return
        stopListening()
        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)
        val utteranceId = "archer_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        _isSpeaking.value = true
    }

    fun stopSpeaking() {
        if (isTtsReady) {
            tts?.stop()
        }
        _isSpeaking.value = false
    }

    fun stopAllManual() {
        isManuallyStopped = true
        stopSpeaking()
        stopListening()
        mainHandler.removeCallbacksAndMessages(null)
    }

    fun resumeAlwaysVoiceRun() {
        isManuallyStopped = false
        isAlwaysVoiceRun = true
        startListening()
    }

    fun destroy() {
        stopAllManual()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
