package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.MemoryCyan
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var fileList by remember {
        val files = context.filesDir.listFiles { _, name -> name.endsWith(".txt") }?.toList() ?: emptyList()
        if (!files.any { it.name == "yt.txt" }) {
            // Seed yt.txt if not existing
            File(context.filesDir, "yt.txt").writeText("Rakib Jarvis YouTube & Automation Log\nStatus: Online\nReady for streaming commands.")
        }
        mutableStateOf(context.filesDir.listFiles { _, name -> name.endsWith(".txt") }?.toList() ?: emptyList())
    }

    var selectedFile by remember { mutableStateOf<File?>(fileList.firstOrNull { it.name == "yt.txt" } ?: fileList.firstOrNull()) }
    var fileContent by remember(selectedFile) { mutableStateOf(selectedFile?.readText() ?: "") }
    var newFileName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CyberBgDark,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color(0xFF38BDF8))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
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
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MemoryCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "NEURAL FILE & NOTES MATRIX",
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
                text = "Manage system files like yt.txt, smart notes, and voice transcripts. You can also say 'yt.txt তৈরি করো' or 'read yt.txt'.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // File Tabs / List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                fileList.forEach { file ->
                    val isSelected = selectedFile?.name == file.name
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF0C4A6E) else CyberCardBg)
                            .border(
                                1.dp,
                                if (isSelected) MemoryCyan else CyberBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedFile = file
                                fileContent = file.readText()
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = file.name,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "New File", tint = MemoryCyan)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // File Content Editor
            OutlinedTextField(
                value = fileContent,
                onValueChange = { fileContent = it },
                label = { Text(selectedFile?.name ?: "File Content", color = MemoryCyan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("file_content_editor"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MemoryCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedContainerColor = CyberCardBg,
                    unfocusedContainerColor = CyberCardBg
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 12.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        selectedFile?.let {
                            it.writeText(fileContent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MemoryCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save File", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                if (selectedFile?.name != "yt.txt") {
                    OutlinedButton(
                        onClick = {
                            selectedFile?.delete()
                            fileList = context.filesDir.listFiles { _, name -> name.endsWith(".txt") }?.toList() ?: emptyList()
                            selectedFile = fileList.firstOrNull()
                            fileContent = selectedFile?.readText() ?: ""
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF87171))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = CyberCardBg,
            title = {
                Text("Create New Text File", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    placeholder = { Text("e.g. notes.txt", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MemoryCyan,
                        unfocusedBorderColor = CyberBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = if (newFileName.endsWith(".txt")) newFileName else "$newFileName.txt"
                        if (name.isNotBlank()) {
                            val f = File(context.filesDir, name)
                            f.writeText("Created by Rakib Jarvis\n")
                            fileList = context.filesDir.listFiles { _, n -> n.endsWith(".txt") }?.toList() ?: emptyList()
                            selectedFile = f
                            fileContent = f.readText()
                            showCreateDialog = false
                            newFileName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MemoryCyan)
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
