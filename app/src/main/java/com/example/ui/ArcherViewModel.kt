package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ArcherApplication
import com.example.action.ActionExecutor
import com.example.action.ActionParser
import com.example.action.DeviceAction
import com.example.data.api.GeminiApiClient
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.preference.AppPreferences
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SheetType {
    MEMORY, CHAT, SOUL, SETTINGS, DIAGNOSTICS, HISTORY, TASKS, HEADLINES, NOTES
}

class ArcherViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ArcherApplication
    val db = app.database
    val prefs = app.preferences

    private val apiClient = GeminiApiClient()
    private val actionExecutor = ActionExecutor(
        context = application.applicationContext,
        taskDao = db.taskDao(),
        memoryDao = db.memoryDao(),
        actionLogDao = db.actionLogDao()
    )

    // Data streams from Room
    val tasks: StateFlow<List<TaskEntity>> = db.taskDao().getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = db.memoryDao().getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = db.chatMessageDao().getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val actionLogs = db.actionLogDao().getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _activeSheet = MutableStateFlow<SheetType?>(null)
    val activeSheet: StateFlow<SheetType?> = _activeSheet.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isMemoryActive = MutableStateFlow(false)
    val isMemoryActive: StateFlow<Boolean> = _isMemoryActive.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _lastUserSpeech = MutableStateFlow("")
    val lastUserSpeech: StateFlow<String> = _lastUserSpeech.asStateFlow()

    private val _lastAiResponse = MutableStateFlow("")
    val lastAiResponse: StateFlow<String> = _lastAiResponse.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Testing neural link...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _latencyMs = MutableStateFlow<Long?>(null)
    val latencyMs: StateFlow<Long?> = _latencyMs.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _todayHeadline = MutableStateFlow("AI Core Online. Ready for voice directives.")
    val todayHeadline: StateFlow<String> = _todayHeadline.asStateFlow()

    private val _isHeadlineLoading = MutableStateFlow(false)
    val isHeadlineLoading: StateFlow<Boolean> = _isHeadlineLoading.asStateFlow()

    // Voice Manager
    var voiceManager: VoiceManager? = null
        private set

    init {
        val alwaysRun = prefs.isAlwaysVoiceRun()
        voiceManager = VoiceManager(
            context = application.applicationContext,
            onSpeechResult = { text ->
                _lastUserSpeech.value = text
                processUserInput(text)
            },
            onError = { err ->
                _lastAiResponse.value = err
            }
        ).apply {
            isAlwaysVoiceRun = alwaysRun
        }

        viewModelScope.launch {
            prefs.alwaysVoiceRunFlow.collect { enabled ->
                voiceManager?.isAlwaysVoiceRun = enabled
            }
        }

        // Automatically test connection on startup
        testConnection()
    }

    fun openSheet(sheet: SheetType) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = null
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            voiceManager?.stopSpeaking()
        }
    }

    fun onOrbClick() {
        if (voiceManager?.isSpeaking?.value == true) {
            voiceManager?.stopSpeaking()
        } else if (voiceManager?.isListening?.value == true) {
            voiceManager?.stopListening()
        } else {
            voiceManager?.resumeAlwaysVoiceRun()
        }
    }

    fun stopAll() {
        voiceManager?.stopAllManual()
        _isThinking.value = false
    }

    fun toggleAlwaysVoiceRun(enabled: Boolean) {
        prefs.setAlwaysVoiceRun(enabled)
        voiceManager?.isAlwaysVoiceRun = enabled
        if (enabled && voiceManager?.isListening?.value != true && voiceManager?.isSpeaking?.value != true) {
            voiceManager?.startListening()
        }
    }

    fun processUserInput(input: String) {
        if (input.isBlank()) return
        _lastUserSpeech.value = input

        viewModelScope.launch {
            // Save user message to Room
            db.chatMessageDao().insertMessage(
                ChatMessageEntity(sender = "user", text = input)
            )

            // 1. Check if it's an actionable hardware command
            val action = ActionParser.parse(input)
            if (action !is DeviceAction.None) {
                val actionResult = actionExecutor.execute(action)
                _lastAiResponse.value = actionResult
                db.chatMessageDao().insertMessage(
                    ChatMessageEntity(sender = "archer", text = actionResult)
                )
                if (!_isMuted.value && prefs.isAutoSpeak()) {
                    voiceManager?.speak(actionResult, prefs.getSpeechRate(), prefs.getSpeechPitch())
                }
                return@launch
            }

            // 2. Query Google Gemini live model
            _isThinking.value = true
            val memoryStrings = memories.value.map { "${it.factKey}: ${it.factValue}" }
            if (memoryStrings.isNotEmpty()) {
                _isMemoryActive.value = true
            }
            val historyPairs = chatMessages.value.takeLast(6).map { it.sender to it.text }

            val result = apiClient.generateResponse(
                apiKey = prefs.getApiKey(),
                model = prefs.getSelectedModel(),
                systemPrompt = prefs.getSoulPrompt(),
                memories = memoryStrings,
                history = historyPairs,
                userMessage = input
            )

            _isThinking.value = false
            _isMemoryActive.value = false
            result.onSuccess { aiText ->
                _lastAiResponse.value = aiText
                db.chatMessageDao().insertMessage(
                    ChatMessageEntity(sender = "archer", text = aiText)
                )
                if (!_isMuted.value && prefs.isAutoSpeak()) {
                    voiceManager?.speak(aiText, prefs.getSpeechRate(), prefs.getSpeechPitch())
                }
            }.onFailure { error ->
                val errorMsg = error.localizedMessage ?: "অজ্ঞাত ত্রুটি"
                _lastAiResponse.value = errorMsg
                db.chatMessageDao().insertMessage(
                    ChatMessageEntity(sender = "archer", text = errorMsg)
                )
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            val apiKey = prefs.getApiKey()
            val model = prefs.getSelectedModel()

            if (apiKey.isBlank()) {
                _connectionStatus.value = "Gemini API Key প্রয়োজন। সেটিংস থেকে এপিআই কী সেট করুন।"
                _latencyMs.value = null
                _isTestingConnection.value = false
                return@launch
            }

            val testResult = apiClient.testConnection(apiKey, model)
            _isTestingConnection.value = false
            testResult.onSuccess { latency ->
                _latencyMs.value = latency
                _connectionStatus.value = "Connected (${model}) - পিং সফল!"
            }.onFailure { err ->
                _latencyMs.value = null
                _connectionStatus.value = "কানেকশন ত্রুটি: ${err.localizedMessage}"
            }
        }
    }

    fun fetchDailyBriefing() {
        viewModelScope.launch {
            _isHeadlineLoading.value = true
            val prompt = "Give a 2-sentence crisp futuristic Jarvis-style daily intelligence briefing in Bengali (বাংলা) for the user, summarizing date, motivation, and technology status."
            val result = apiClient.generateResponse(
                apiKey = prefs.getApiKey(),
                model = prefs.getSelectedModel(),
                systemPrompt = prefs.getSoulPrompt(),
                memories = emptyList(),
                history = emptyList(),
                userMessage = prompt
            )
            _isHeadlineLoading.value = false
            result.onSuccess { text ->
                _todayHeadline.value = text
                if (!_isMuted.value && prefs.isAutoSpeak()) {
                    voiceManager?.speak(text, prefs.getSpeechRate(), prefs.getSpeechPitch())
                }
            }.onFailure {
                _todayHeadline.value = "আজকের দিনে Archer AI আপনার সেবায় প্রস্তুত। কম্যান্ডার, নির্দেশ দিন।"
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            db.taskDao().updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            db.taskDao().insertTask(TaskEntity(title = title, isCompleted = false))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            db.taskDao().deleteTask(task)
        }
    }

    fun addMemory(key: String, value: String) {
        viewModelScope.launch {
            _isMemoryActive.value = true
            db.memoryDao().insertMemory(MemoryEntity(factKey = key, factValue = value))
            kotlinx.coroutines.delay(1200)
            _isMemoryActive.value = false
        }
    }

    fun launchMiniMode(context: android.content.Context): Boolean {
        return if (android.provider.Settings.canDrawOverlays(context)) {
            val intent = android.content.Intent(context, com.example.service.ArcherFloatingOverlayService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            true
        } else {
            false
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            db.memoryDao().deleteMemory(memory)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            db.chatMessageDao().clearHistory()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            db.actionLogDao().clearLogs()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.destroy()
    }
}
