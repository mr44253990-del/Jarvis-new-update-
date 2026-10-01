package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ChatOrange
import com.example.ui.theme.MemoryCyan
import com.example.ui.theme.SettingsGreen
import com.example.ui.theme.SoulWhite

@Composable
fun CircuitLines(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circuit_flow")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_flow"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 4 lines spaced according to the pills on the left (relative Y coordinates)
        val pillYs = listOf(
            h * 0.12f, // Memory
            h * 0.37f, // Chat
            h * 0.62f, // Soul
            h * 0.87f  // Settings
        )

        // Target arrival point on the orb's left perimeter
        val orbTargetX = w
        val orbTargetYs = listOf(
            h * 0.40f,
            h * 0.46f,
            h * 0.52f,
            h * 0.58f
        )

        val colors = listOf(MemoryCyan, ChatOrange, SoulWhite, SettingsGreen)

        for (i in 0 until 4) {
            val startY = pillYs[i]
            val endY = orbTargetYs[i]
            val color = colors[i]

            val path = Path().apply {
                moveTo(0f, startY)
                // Curve smoothly towards the orb
                val ctrl1X = w * 0.35f
                val ctrl2X = w * 0.70f
                cubicTo(ctrl1X, startY, ctrl2X, endY, orbTargetX, endY)
            }

            // Glow trace background
            drawPath(
                path = path,
                color = color.copy(alpha = 0.28f),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Sharp line
            drawPath(
                path = path,
                color = color.copy(alpha = 0.85f),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Traveling light pulse particle
            val t = (pulseProgress + i * 0.25f) % 1f
            // Evaluate cubic bezier for position
            val p0 = Offset(0f, startY)
            val p1 = Offset(w * 0.35f, startY)
            val p2 = Offset(w * 0.70f, endY)
            val p3 = Offset(orbTargetX, endY)

            val px = (1 - t) * (1 - t) * (1 - t) * p0.x +
                    3 * (1 - t) * (1 - t) * t * p1.x +
                    3 * (1 - t) * t * t * p2.x +
                    t * t * t * p3.x
            val py = (1 - t) * (1 - t) * (1 - t) * p0.y +
                    3 * (1 - t) * (1 - t) * t * p1.y +
                    3 * (1 - t) * t * t * p2.y +
                    t * t * t * p3.y

            drawCircle(
                color = Color.White,
                radius = 3.2.dp.toPx(),
                center = Offset(px, py)
            )
            drawCircle(
                color = color,
                radius = 5.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}
