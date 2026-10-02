package com.example.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("archer_ai_preferences", Context.MODE_PRIVATE)

    companion object {
        const val KEY_API_KEY = "api_key"
        const val KEY_SELECTED_MODEL = "selected_model"
        const val KEY_SOUL_NAME = "soul_name"
        const val KEY_SOUL_PROMPT = "soul_prompt"
        const val KEY_LANGUAGE = "language" // "bn", "en"
        const val KEY_AUTO_SPEAK = "auto_speak"
        const val KEY_SPEECH_RATE = "speech_rate"
        const val KEY_SPEECH_PITCH = "speech_pitch"
        const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        const val KEY_SCREEN_SHARE_ENABLED = "screen_share_enabled"
        const val KEY_SHOW_HUD = "show_hud"
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_ALWAYS_VOICE_RUN = "always_voice_run"

        val LIVE_MODELS = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-pro-preview",
            "gemini-3.1-flash-lite-preview",
            "gemini-2.5-flash-native-audio-preview-12-2025",
            "gemini-2.5-flash-preview-tts",
            "gemini-2.5-flash-image",
            "gemini-3.1-flash-image-preview"
        )

        const val DEFAULT_MODEL = "gemini-3.5-flash"
    }

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    private val _modelFlow = MutableStateFlow(getSelectedModel())
    val modelFlow: StateFlow<String> = _modelFlow.asStateFlow()

    private val _autoSpeakFlow = MutableStateFlow(isAutoSpeak())
    val autoSpeakFlow: StateFlow<Boolean> = _autoSpeakFlow.asStateFlow()

    private val _showHudFlow = MutableStateFlow(isShowHud())
    val showHudFlow: StateFlow<Boolean> = _showHudFlow.asStateFlow()

    private val _overlayEnabledFlow = MutableStateFlow(isOverlayEnabled())
    val overlayEnabledFlow: StateFlow<Boolean> = _overlayEnabledFlow.asStateFlow()

    private val _screenShareFlow = MutableStateFlow(isScreenShareEnabled())
    val screenShareFlow: StateFlow<Boolean> = _screenShareFlow.asStateFlow()

    private val _alwaysVoiceRunFlow = MutableStateFlow(isAlwaysVoiceRun())
    val alwaysVoiceRunFlow: StateFlow<Boolean> = _alwaysVoiceRunFlow.asStateFlow()

    fun getApiKey(): String {
        val saved = prefs.getString(KEY_API_KEY, "") ?: ""
        if (saved.isNotBlank()) return saved
        // Fallback to BuildConfig if provided
        return try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _apiKeyFlow.value = key.trim()
    }

    fun getSelectedModel(): String {
        return prefs.getString(KEY_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL, model).apply()
        _modelFlow.value = model
    }

    fun getSoulName(): String {
        return prefs.getString(KEY_SOUL_NAME, "Archer AI (Jarvis)") ?: "Archer AI (Jarvis)"
    }

    fun setSoulName(name: String) {
        prefs.edit().putString(KEY_SOUL_NAME, name).apply()
    }

    fun getSoulPrompt(): String {
        val defaultPrompt = "You are Archer AI, an elite Jarvis-level futuristic cyberpunk mobile assistant. " +
                "You are loyal, tactical, witty, intelligent, and highly capable. " +
                "CRITICAL: Always speak primarily in fluent, natural Bengali (বাংলা) unless explicitly asked to speak in English. " +
                "You can execute phone calls, send SMS, play YouTube songs, open apps, manage tasks, control flashlight, check battery, and automate device operations. " +
                "Keep responses concise, confident, and direct, suitable for voice speech."
        return prefs.getString(KEY_SOUL_PROMPT, defaultPrompt) ?: defaultPrompt
    }

    fun setSoulPrompt(prompt: String) {
        prefs.edit().putString(KEY_SOUL_PROMPT, prompt).apply()
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "bn") ?: "bn"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun isAutoSpeak(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SPEAK, true)
    }

    fun setAutoSpeak(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
        _autoSpeakFlow.value = enabled
    }

    fun getSpeechRate(): Float {
        return prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
    }

    fun getSpeechPitch(): Float {
        return prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_SPEECH_PITCH, pitch).apply()
    }

    fun isOverlayEnabled(): Boolean {
        return prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
        _overlayEnabledFlow.value = enabled
    }

    fun isScreenShareEnabled(): Boolean {
        return prefs.getBoolean(KEY_SCREEN_SHARE_ENABLED, false)
    }

    fun setScreenShareEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCREEN_SHARE_ENABLED, enabled).apply()
        _screenShareFlow.value = enabled
    }

    fun isShowHud(): Boolean {
        return prefs.getBoolean(KEY_SHOW_HUD, true)
    }

    fun setShowHud(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_HUD, show).apply()
        _showHudFlow.value = show
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(completed: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, !completed).apply()
    }

    fun isAlwaysVoiceRun(): Boolean {
        return prefs.getBoolean(KEY_ALWAYS_VOICE_RUN, true) // Enabled by default: No Idle, Always Voice Run
    }

    fun setAlwaysVoiceRun(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALWAYS_VOICE_RUN, enabled).apply()
        _alwaysVoiceRunFlow.value = enabled
    }
}
