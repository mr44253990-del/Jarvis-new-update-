package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoulSheet(
    currentName: String,
    currentPrompt: String,
    currentLanguage: String,
    onSaveSoul: (name: String, prompt: String, language: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var prompt by remember { mutableStateOf(currentPrompt) }
    var language by remember { mutableStateOf(currentLanguage) }

    val personas = listOf(
        "Rakib Jarvis" to "You are Rakib Jarvis, an elite futuristic synthetic intelligence and personal mobile assistant built for Rakib. Hyper-intelligent, tactical, loyal, and fast. Speak primarily in fluent Bengali (বাংলা).",
        "Friday Cyber Core" to "You are Friday, an ultra-fast tactical synthetic intelligence. Direct, precise, friendly, executing smartphone automation commands efficiently in Bengali.",
        "Kavita Voice AI" to "You are a warm, courteous, highly empathetic Bengali conversational assistant dedicated to helping Rakib manage daily tasks, phone calls, and learning.",
        "Iron Core Military Jarvis" to "Tactical AI system. Zero fluff, absolute speed and efficiency. Acknowledges commands with military precision and executes Android intents directly."
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberBgDark,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = SoulWhite)
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
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = SoulWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SOUL & PERSONALITY CORE",
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
                text = "Configure Archer AI's consciousness, personality tone, system instructions, and primary language.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("SELECT PRESET MATRIX", color = SoulWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                personas.forEach { (pName, pPrompt) ->
                    val isSelected = name == pName
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else CyberCardBg)
                            .border(
                                1.2.dp,
                                if (isSelected) SoulWhite else CyberBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                name = pName
                                prompt = pPrompt
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                name = pName
                                prompt = pPrompt
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = SoulWhite)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = pName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = pPrompt.take(80) + "...",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("PRIMARY LANGUAGE", color = SoulWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("bn" to "বাংলা (Bengali - Default)", "en" to "English (Global)").forEach { (code, label) ->
                    val isSelected = language == code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else CyberCardBg)
                            .border(
                                1.dp,
                                if (isSelected) MemoryCyan else CyberBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { language = code }
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MemoryCyan else Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("CUSTOM SYSTEM DIRECTIVE", color = SoulWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SoulWhite,
                    unfocusedBorderColor = CyberBorder,
                    focusedContainerColor = CyberCardBg,
                    unfocusedContainerColor = CyberCardBg
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.5.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSaveSoul(name, prompt, language)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoulWhite),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_soul_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Soul Matrix", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
