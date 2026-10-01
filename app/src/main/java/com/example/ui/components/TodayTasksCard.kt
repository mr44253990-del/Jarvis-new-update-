package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.MemoryCyan

@Composable
fun TodayTasksCard(
    tasks: List<TaskEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uncompletedCount = tasks.count { !it.isCompleted }
    val progress = if (tasks.isEmpty()) 0f else {
        tasks.count { it.isCompleted }.toFloat() / tasks.size.toFloat()
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CyberCardBg)
            .border(1.2.dp, CyberBorder.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .padding(14.dp)
            .testTag("today_tasks_card")
    ) {
        // Header Row matching screenshot: TODAY TASKS 1 left 🔥 >
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TODAY TASKS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$uncompletedCount left",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "🔥",
                    fontSize = 12.sp
                )
            }
            Text(
                text = ">",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cyan progress bar matching screenshot
        LinearProgressIndicator(
            progress = { animatedProgress.coerceIn(0.08f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MemoryCyan,
            trackColor = Color(0xFF13362B),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Show first 1 or 2 task items as in the screenshot
        if (tasks.isEmpty()) {
            Text(
                text = "No tasks yet. Say 'Add task' to Jarvis.",
                color = Color(0xFF64748B),
                fontSize = 12.sp
            )
        } else {
            val previewTask = tasks.firstOrNull { !it.isCompleted } ?: tasks.first()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleTask(previewTask) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (previewTask.isCompleted) Color(0xFF10B981) else Color.Transparent)
                        .border(1.8.dp, MemoryCyan, CircleShape)
                )
                Text(
                    text = previewTask.title,
                    color = if (previewTask.isCompleted) Color(0xFF64748B) else Color.White,
                    textDecoration = if (previewTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
