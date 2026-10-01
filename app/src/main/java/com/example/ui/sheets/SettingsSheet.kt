package com.example.ui.sheets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preference.AppPreferences
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    apiKey: String,
    selectedModel: String,
    autoSpeak: Boolean,
    showHud: Boolean,
    overlayEnabled: Boolean,
    screenShareEnabled: Boolean,
    speechRate: Float,
    speechPitch: Float,
    connectionStatus: String,
    latencyMs: Long?,
    isTestingConnection: Boolean,
    onSaveApiKey: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onToggleAutoSpeak: (Boolean) -> Unit,
    onToggleShowHud: (Boolean) -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    onToggleScreenShare: (Boolean) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onTestConnection: () -> Unit,
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var keyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var keyVisible by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var showCommandsDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberBgDark,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = SettingsGreen)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = SettingsGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SYSTEM CONFIGURATION",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. GEMINI API KEY SECTION
            Text(
                text = "GOOGLE GEMINI API KEY",
                color = SettingsGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter your Google AI Studio Gemini API Key. Your key is stored locally and securely on your device.",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text("API Key (AIzaSy...)", color = Color(0xFF94A3B8)) },
                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle key visibility",
                            tint = Color.Gray
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SettingsGreen,
                    unfocusedBorderColor = CyberBorder,
                    focusedContainerColor = CyberCardBg,
                    unfocusedContainerColor = CyberCardBg
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onSaveApiKey(keyInput)
                        onTestConnection()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("save_api_key_button")
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Save & Test Connection", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Connection Status & Diagnostic Banner
            if (connectionStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val isSuccess = connectionStatus.contains("Connected") || connectionStatus.contains("সফল")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSuccess) Color(0xFF064E3B) else Color(0xFF450A0A))
                        .border(
                            1.dp,
                            if (isSuccess) SettingsGreen else StopRed,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSuccess) SettingsGreen else StopRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isSuccess) "CONNECTION ACTIVE" else "CONNECTION ISSUE",
                                color = if (isSuccess) SettingsGreen else StopRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (latencyMs != null) {
                                Text(
                                    text = "(${latencyMs}ms)",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = connectionStatus,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. LIVE MODEL SELECTOR
            Text(
                text = "GOOGLE GEMINI LIVE MODEL",
                color = SettingsGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "All Google Gemini live generation models available for selection:",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .border(1.2.dp, SettingsGreen.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .clickable { modelDropdownExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("model_selector_dropdown")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = selectedModel,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (selectedModel == AppPreferences.DEFAULT_MODEL) "Default recommended model" else "Specialized Google Gemini Model",
                            color = SettingsGreen,
                            fontSize = 10.5.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select",
                        tint = SettingsGreen
                    )
                }

                DropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false },
                    modifier = Modifier.background(CyberCardBg)
                ) {
                    AppPreferences.LIVE_MODELS.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = model,
                                        color = if (model == selectedModel) SettingsGreen else Color.White,
                                        fontWeight = if (model == selectedModel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    val desc = when (model) {
                                        "gemini-3.5-flash" -> "General Assistant & Fast Reasoning (Default)"
                                        "gemini-3.1-pro-preview" -> "Advanced Reasoning & Complex Commands"
                                        "gemini-3.1-flash-lite-preview" -> "Ultra Lightweight & Low Latency"
                                        "gemini-2.5-flash-native-audio-preview-12-2025" -> "Real-time Live Audio & Voice"
                                        "gemini-2.5-flash-preview-tts" -> "Dedicated Voice & Text-to-Speech"
                                        "gemini-2.5-flash-image" -> "Visual Understanding & Vision"
                                        else -> "High-Definition Multimodal Preview"
                                    }
                                    Text(text = desc, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                            },
                            onClick = {
                                onSelectModel(model)
                                modelDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. AUTOMATION & SCREEN SHARE
            Text(
                text = "AUTOMATION & FLOATING OVERLAY",
                color = SettingsGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Floating Overlay Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Floating Jarvis Orb (Display Over Apps)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shows a draggable glowing Jarvis core over YouTube, Chrome, etc., for instant voice control.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = overlayEnabled,
                    onCheckedChange = { checked ->
                        if (checked && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } else {
                            onToggleOverlay(checked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = SettingsGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Screen Share & Accessibility Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Screen Share & Touch Automation",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enables Archer AI to read on-screen elements and click items automatically on command.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = screenShareEnabled,
                    onCheckedChange = { checked ->
                        onToggleScreenShare(checked)
                        if (checked) {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = SettingsGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live HUD Subtitles Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Speech Subtitles HUD",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Displays live transcript of what you say and Archer's responses at the top of the screen.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = showHud,
                    onCheckedChange = onToggleShowHud,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = SettingsGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. VOICE SPEECH (TTS) CONFIGURATION
            Text(
                text = "VOICE ENGINE & AUDIO",
                color = SettingsGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Auto-Speak Responses", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Archer AI automatically vocalizes responses using Bengali TTS.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                Switch(
                    checked = autoSpeak,
                    onCheckedChange = onToggleAutoSpeak,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = SettingsGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Speech Rate Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Speech Rate (গতি)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${String.format("%.2f", speechRate)}x", color = SettingsGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = speechRate,
                    onValueChange = onSpeechRateChange,
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = SettingsGreen,
                        activeTrackColor = SettingsGreen,
                        inactiveTrackColor = CyberBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. COMMANDS CHEAT SHEET & PERMISSIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showCommandsDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MemoryCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MemoryCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Voice Commands", fontSize = 12.sp)
                }

                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SettingsGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Permissions", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCommandsDialog) {
        AlertDialog(
            onDismissRequest = { showCommandsDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text("Archer AI Voice Commands", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val commands = listOf(
                        "📞 Phone Call" to "'017... নাম্বারে কল করো' / 'ডায়াল করো 12345'",
                        "✉️ Send SMS" to "'018... নাম্বারে এসএমএস পাঠাও'",
                        "▶️ YouTube" to "'ইউটিউব ওপেন করো' / 'ইউটিউবে গান বাজাও'",
                        "🌐 Browser" to "'গুগলে সার্চ করো ...' / 'ওয়েবসাইটে যাও'",
                        "📸 Camera" to "'ক্যামেরা খোলো' / 'ছবি তোলো'",
                        "🔦 Flashlight" to "'টর্চ জ্বালাও' / 'লাইট বন্ধ করো'",
                        "🔋 Battery" to "'ব্যাটারি কত শতাংশ?'",
                        "⏰ Time & Alarm" to "'কয়টা বাজে?' / 'অ্যালার্ম দাও'",
                        "📝 Tasks" to "'টাস্ক যোগ করো ভিডিও আপলোড' / 'আজকের কাজ'",
                        "💡 Neural Memory" to "'মনে রাখো যে আমার নাম ফারহান'",
                        "⚙️ Settings" to "'ওয়াইফাই সেটিংস ওপেন করো' / 'ব্লুটুথ'",
                        "🔊 Volume" to "'ভলিউম বাড়াও' / 'সাউন্ড কমাও'",
                        "📱 Minimize" to "'মিনিমাইজ করো' / 'স্ক্রিন শেয়ার মোড'"
                    )
                    commands.forEach { (cat, ex) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberBgElevated)
                                .padding(8.dp)
                        ) {
                            Text(text = cat, color = MemoryCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = ex, color = Color(0xFFCBD5E1), fontSize = 11.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCommandsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsGreen)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
