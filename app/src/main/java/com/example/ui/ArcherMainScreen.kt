package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.sheets.*
import com.example.ui.theme.*

@Composable
fun ArcherMainScreen(
    viewModel: ArcherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe State
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val actionLogs by viewModel.actionLogs.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()

    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val isMemoryActive by viewModel.isMemoryActive.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val lastUserSpeech by viewModel.lastUserSpeech.collectAsStateWithLifecycle()
    val lastAiResponse by viewModel.lastAiResponse.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val latencyMs by viewModel.latencyMs.collectAsStateWithLifecycle()
    val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val todayHeadline by viewModel.todayHeadline.collectAsStateWithLifecycle()
    val isHeadlineLoading by viewModel.isHeadlineLoading.collectAsStateWithLifecycle()

    // Voice Manager Flows
    val isListening by viewModel.voiceManager?.isListening?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val isSpeaking by viewModel.voiceManager?.isSpeaking?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    val audioLevel by viewModel.voiceManager?.rmsAudioLevel?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0f) }

    // Preferences Flows
    val apiKey by viewModel.prefs.apiKeyFlow.collectAsStateWithLifecycle()
    val selectedModel by viewModel.prefs.modelFlow.collectAsStateWithLifecycle()
    val autoSpeak by viewModel.prefs.autoSpeakFlow.collectAsStateWithLifecycle()
    val showHud by viewModel.prefs.showHudFlow.collectAsStateWithLifecycle()
    val overlayEnabled by viewModel.prefs.overlayEnabledFlow.collectAsStateWithLifecycle()
    val screenShareEnabled by viewModel.prefs.screenShareFlow.collectAsStateWithLifecycle()
    val alwaysVoiceRun by viewModel.prefs.alwaysVoiceRunFlow.collectAsStateWithLifecycle()

    var speechRate by remember { mutableFloatStateOf(viewModel.prefs.getSpeechRate()) }
    var speechPitch by remember { mutableFloatStateOf(viewModel.prefs.getSpeechPitch()) }

    // Permissions Launcher
    val permissionsToRequest = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CALL_PHONE)
        add(Manifest.permission.SEND_SMS)
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // If audio permission is granted, start speech recognition
        if (results[Manifest.permission.RECORD_AUDIO] == true) {
            viewModel.onOrbClick()
        }
    }

    val requestAllPermissions = {
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Auto start Always Voice Run if permission is already granted
    LaunchedEffect(alwaysVoiceRun) {
        val hasAudio = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasAudio && alwaysVoiceRun && !isListening && !isSpeaking) {
            viewModel.voiceManager?.resumeAlwaysVoiceRun()
        }
    }

    // First Launch setup dialog if API key is empty
    var showFirstLaunchPrompt by remember {
        mutableStateOf(viewModel.prefs.isFirstLaunch() && apiKey.isBlank())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberBgDark,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF09241B),
                            Color(0xFF04120C),
                            CyberBgDark
                        ),
                        radius = 1200f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ==================== 1. TOP HEADER ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // History & Notes Icons (Left)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.openSheet(SheetType.HISTORY) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B1F17))
                                .border(1.dp, Color(0xFF16382B), CircleShape)
                                .testTag("history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Action History",
                                tint = Color(0xFF6EE7B7),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openSheet(SheetType.NOTES) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B1F17))
                                .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f), CircleShape)
                                .testTag("notes_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Files & Notes (yt.txt)",
                                tint = MemoryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Center App Title: Hood Emblem + RAKIB JARVIS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF064E3B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MemoryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "RAKIB JARVIS",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Right: Bug / Diagnostics Button with glowing border & live dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                                .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp))
                                .clickable { viewModel.openSheet(SheetType.DIAGNOSTICS) }
                                .testTag("diagnostics_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Diagnostics",
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Quick Always Voice Run Toggle & Status Dot
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (alwaysVoiceRun) Color(0xFF064E3B) else Color(0xFF1E293B))
                                .border(1.dp, if (alwaysVoiceRun) SettingsGreen else Color.Gray, RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleAlwaysVoiceRun(!alwaysVoiceRun) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (alwaysVoiceRun) SettingsGreen else Color.Gray)
                            )
                            Text(
                                text = if (alwaysVoiceRun) "ALWAYS VOICE" else "VOICE PAUSED",
                                color = if (alwaysVoiceRun) Color(0xFFA7F3D0) else Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // ==================== 2. LIVE HUD TRANSCRIPT OVERLAY ====================
                HudTranscriptOverlay(
                    userText = lastUserSpeech,
                    aiText = lastAiResponse,
                    isVisible = showHud
                )

                // ==================== 3. CENTER ORB & PILL MATRIX ====================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: The 4 Pills
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.width(135.dp)
                        ) {
                            PillMenuButton(
                                title = "MEMORY",
                                icon = Icons.Default.Lightbulb,
                                accentColor = MemoryCyan,
                                borderColor = MemoryCyanBorder,
                                testTag = "pill_memory",
                                onClick = { viewModel.openSheet(SheetType.MEMORY) }
                            )

                            PillMenuButton(
                                title = "CHAT",
                                icon = Icons.AutoMirrored.Filled.Chat,
                                accentColor = ChatOrange,
                                borderColor = ChatOrangeBorder,
                                testTag = "pill_chat",
                                onClick = { viewModel.openSheet(SheetType.CHAT) }
                            )

                            PillMenuButton(
                                title = "SOUL",
                                icon = Icons.Default.Psychology,
                                accentColor = SoulWhite,
                                borderColor = SoulWhiteBorder,
                                testTag = "pill_soul",
                                onClick = { viewModel.openSheet(SheetType.SOUL) }
                            )

                            PillMenuButton(
                                title = "SETTIN...",
                                icon = Icons.Default.Settings,
                                accentColor = SettingsGreen,
                                borderColor = SettingsGreenBorder,
                                testTag = "pill_settings",
                                onClick = { viewModel.openSheet(SheetType.SETTINGS) }
                            )
                        }

                        // Connecting Circuit Lines Canvas
                        CircuitLines(
                            modifier = Modifier
                                .weight(0.25f)
                                .height(220.dp)
                        )

                        // Right: The Swirling Holographic Core Orb
                        HolographicCoreOrb(
                            isListening = isListening,
                            isSpeaking = isSpeaking,
                            isThinking = isThinking,
                            audioLevel = audioLevel,
                            isMuted = isMuted,
                            isAlwaysVoiceRun = alwaysVoiceRun,
                            isMemoryActive = isMemoryActive,
                            onOrbClick = {
                                val hasAudio = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasAudio) {
                                    requestAllPermissions()
                                } else {
                                    viewModel.onOrbClick()
                                }
                            },
                            onStopClick = { viewModel.stopAll() },
                            onToggleMute = { viewModel.toggleMute() },
                            onMiniModeClick = {
                                if (!viewModel.launchMiniMode(context)) {
                                    viewModel.openSheet(SheetType.SETTINGS)
                                }
                            },
                            modifier = Modifier.weight(0.75f)
                        )
                    }
                }

                // ==================== 4. BOTTOM SECTION ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: TODAY HEADLINES
                    TodayHeadlinesCard(
                        headlineText = todayHeadline,
                        onCardClick = { viewModel.openSheet(SheetType.HEADLINES) },
                        modifier = Modifier.weight(0.45f)
                    )

                    // Right: TODAY TASKS
                    TodayTasksCard(
                        tasks = tasks,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onCardClick = { viewModel.openSheet(SheetType.TASKS) },
                        modifier = Modifier.weight(0.55f)
                    )
                }
            }
        }
    }

    // ==================== MODAL SHEETS ====================
    when (activeSheet) {
        SheetType.MEMORY -> {
            MemorySheet(
                memories = memories,
                onAddMemory = { k, v -> viewModel.addMemory(k, v) },
                onDeleteMemory = { viewModel.deleteMemory(it) },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.CHAT -> {
            ChatSheet(
                messages = chatMessages,
                isThinking = isThinking,
                onSendMessage = { viewModel.processUserInput(it) },
                onSpeakMessage = {
                    viewModel.voiceManager?.speak(
                        it,
                        viewModel.prefs.getSpeechRate(),
                        viewModel.prefs.getSpeechPitch()
                    )
                },
                onMicClick = {
                    val hasAudio = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasAudio) {
                        requestAllPermissions()
                    } else {
                        viewModel.onOrbClick()
                    }
                },
                onClearChat = { viewModel.clearChat() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.SOUL -> {
            SoulSheet(
                currentName = viewModel.prefs.getSoulName(),
                currentPrompt = viewModel.prefs.getSoulPrompt(),
                currentLanguage = viewModel.prefs.getLanguage(),
                onSaveSoul = { n, p, l ->
                    viewModel.prefs.setSoulName(n)
                    viewModel.prefs.setSoulPrompt(p)
                    viewModel.prefs.setLanguage(l)
                },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.SETTINGS -> {
            SettingsSheet(
                apiKey = apiKey,
                selectedModel = selectedModel,
                autoSpeak = autoSpeak,
                showHud = showHud,
                overlayEnabled = overlayEnabled,
                screenShareEnabled = screenShareEnabled,
                alwaysVoiceRun = alwaysVoiceRun,
                speechRate = speechRate,
                speechPitch = speechPitch,
                connectionStatus = connectionStatus,
                latencyMs = latencyMs,
                isTestingConnection = isTestingConnection,
                onSaveApiKey = {
                    viewModel.prefs.setApiKey(it)
                },
                onSelectModel = {
                    viewModel.prefs.setSelectedModel(it)
                    viewModel.testConnection()
                },
                onToggleAutoSpeak = { viewModel.prefs.setAutoSpeak(it) },
                onToggleShowHud = { viewModel.prefs.setShowHud(it) },
                onToggleOverlay = { viewModel.prefs.setOverlayEnabled(it) },
                onToggleScreenShare = { viewModel.prefs.setScreenShareEnabled(it) },
                onToggleAlwaysVoiceRun = { viewModel.toggleAlwaysVoiceRun(it) },
                onSpeechRateChange = {
                    speechRate = it
                    viewModel.prefs.setSpeechRate(it)
                },
                onSpeechPitchChange = {
                    speechPitch = it
                    viewModel.prefs.setSpeechPitch(it)
                },
                onTestConnection = { viewModel.testConnection() },
                onRequestPermissions = { requestAllPermissions() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.DIAGNOSTICS -> {
            DiagnosticsSheet(
                apiKey = apiKey,
                selectedModel = selectedModel,
                connectionStatus = connectionStatus,
                latencyMs = latencyMs,
                isTesting = isTestingConnection,
                onTestPing = { viewModel.testConnection() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.HISTORY -> {
            HistorySheet(
                logs = actionLogs,
                onClearLogs = { viewModel.clearLogs() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.TASKS -> {
            TasksSheet(
                tasks = tasks,
                onAddTask = { viewModel.addTask(it) },
                onToggleTask = { viewModel.toggleTask(it) },
                onDeleteTask = { viewModel.deleteTask(it) },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.HEADLINES -> {
            HeadlinesSheet(
                headlineText = todayHeadline,
                isLoading = isHeadlineLoading,
                onFetchBriefing = { viewModel.fetchDailyBriefing() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        SheetType.NOTES -> {
            NotesSheet(
                onDismiss = { viewModel.closeSheet() }
            )
        }
        null -> {}
    }

    // First Launch Setup Dialog
    if (showFirstLaunchPrompt) {
        var tempKey by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFirstLaunchPrompt = false },
            containerColor = CyberCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = MemoryCyan)
                    Text("RAKIB JARVIS INITIALIZATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "স্বাগতম! Rakib Jarvis পরিচালনার জন্য Google Gemini API Key প্রবেশ করান। ডিফল্টভাবে মডেলটি 'gemini-2.5-flash' এ প্রস্তুত রয়েছে।",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.5.sp
                    )

                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("Gemini API Key", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MemoryCyan,
                            unfocusedBorderColor = CyberBorder
                        )
                    )

                    Text(
                        text = "মাইক্রোফোন, কল, এসএমএস এবং ক্যামেরা অ্যাক্সেসের অনুমতি দিতে কনফার্ম চাপুন।",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempKey.isNotBlank()) {
                            viewModel.prefs.setApiKey(tempKey)
                        }
                        viewModel.prefs.setFirstLaunch(true)
                        showFirstLaunchPrompt = false
                        requestAllPermissions()
                        viewModel.testConnection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MemoryCyan)
                ) {
                    Text("Initialize AI", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.prefs.setFirstLaunch(true)
                    showFirstLaunchPrompt = false
                    requestAllPermissions()
                }) {
                    Text("Skip for now", color = Color.Gray)
                }
            }
        )
    }
}
