package com.example.ui.sheets

import android.content.Context
import android.os.BatteryManager
import android.provider.Settings
import android.speech.SpeechRecognizer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ArcherAccessibilityService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsSheet(
    apiKey: String,
    selectedModel: String,
    connectionStatus: String,
    latencyMs: Long?,
    isTesting: Boolean,
    onTestPing: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    val hasSpeech = SpeechRecognizer.isRecognitionAvailable(context)
    val hasOverlay = Settings.canDrawOverlays(context)
    val isAccessibilityActive = ArcherAccessibilityService.isServiceRunning()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberBgDark,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF10B981))
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
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "DIAGNOSTIC & TELEMETRY",
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

            Text(
                text = "Real-time diagnostic report of Archer AI neural subsystems, hardware telemetry, and API connectivity.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subsystem Status Matrix
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DiagItem(
                    label = "Gemini API Key",
                    value = if (apiKey.isNotBlank()) "Configured (${apiKey.take(6)}...)" else "Missing / Empty",
                    isGood = apiKey.isNotBlank()
                )
                DiagItem(
                    label = "Active Live Model",
                    value = selectedModel,
                    isGood = true
                )
                DiagItem(
                    label = "API Ping / Latency",
                    value = if (latencyMs != null) "$latencyMs ms (Active)" else connectionStatus.ifBlank { "Not tested yet" },
                    isGood = latencyMs != null
                )
                DiagItem(
                    label = "Speech Recognizer",
                    value = if (hasSpeech) "Hardware Available" else "Unavailable",
                    isGood = hasSpeech
                )
                DiagItem(
                    label = "Floating Overlay Permission",
                    value = if (hasOverlay) "Granted (SYSTEM_ALERT_WINDOW)" else "Not Granted",
                    isGood = hasOverlay
                )
                DiagItem(
                    label = "Accessibility Automation",
                    value = if (isAccessibilityActive) "Service Running" else "Standby (Disabled in Android Settings)",
                    isGood = isAccessibilityActive
                )
                DiagItem(
                    label = "Battery Telemetry",
                    value = "$batteryPct% Capacity",
                    isGood = batteryPct > 20
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onTestPing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run Live Subsystem Test", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Troubleshooting Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SOLVING CONNECTION ISSUES (সমাধান)",
                        color = ParticleYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. যদি API ত্রুটি দেখায়: Settings এ গিয়ে সঠিক Google Gemini API Key প্রবেশ করান।\n" +
                                "2. ভয়েস কাজ না করলে: মাইক্রোফোন পারমিশন এলাউ আছে কিনা চেক করুন।\n" +
                                "3. ব্যাকগ্রাউন্ড মিনিমাইজ মোড: Settings থেকে 'Floating Jarvis Orb' অন করে পারমিশন দিন।\n" +
                                "4. স্ক্রিন শেয়ার অটোমেশন: Accessibility Settings থেকে 'Archer AI Screen Assistant' অন করুন।",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DiagItem(label: String, value: String, isGood: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberCardBg)
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (isGood) Color(0xFF10B981) else Color(0xFFEF4444))
            )
            Text(
                text = value,
                color = if (isGood) Color(0xFFA7F3D0) else Color(0xFFFCA5A5),
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
