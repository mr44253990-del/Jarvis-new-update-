package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

data class Particle(
    val baseAngle: Float,
    val distanceRatio: Float,
    val speed: Float,
    val radius: Float,
    val color: Color,
    val orbitTilt: Float
)

@Composable
fun HolographicCoreOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    audioLevel: Float,
    isMuted: Boolean,
    isAlwaysVoiceRun: Boolean = true,
    onOrbClick: () -> Unit,
    onStopClick: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_animation")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Pre-generate stable particles matching the screenshot (Cyan, Gold/Yellow, and Green)
    val particles = remember {
        val list = mutableListOf<Particle>()
        val colors = listOf(
            ParticleCyan,
            ParticleYellow,
            ParticleGreen,
            Color(0xFF67E8F9),
            Color(0xFFFDE68A),
            Color(0xFF86EFAC)
        )
        for (i in 0 until 180) {
            val angle = (i * 137.5f) % 360f
            val dist = 0.25f + 0.65f * (i.toFloat() / 180f)
            val speed = if (i % 2 == 0) 1f else -0.7f
            val rad = 1.5f + (i % 4) * 0.9f
            val col = colors[i % colors.size]
            val tilt = (i % 5) * 0.15f
            list.add(Particle(angle, dist, speed, rad, col, tilt))
        }
        list
    }

    val statusText = when {
        isSpeaking -> "• SPEAKING •"
        isListening -> "• LISTENING •"
        isThinking -> "• PROCESSING •"
        isAlwaysVoiceRun -> "• ACTIVE LISTENING •"
        else -> "• STANDBY •"
    }

    val statusColor = when {
        isSpeaking -> Color(0xFF6EE7B7)
        isListening -> MemoryCyan
        isThinking -> ChatOrange
        isAlwaysVoiceRun -> Color(0xFF22C55E)
        else -> Color(0xFF94A3B8)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Orb Container
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOrbClick
                )
                .testTag("holographic_orb"),
            contentAlignment = Alignment.Center
        ) {
            // Particle & Reactor Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxRadius = size.width / 2f - 10.dp.toPx()

                // Outer thin cyber glow ring
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                    radius = maxRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.8f)
                )

                // Secondary orbital ellipse
                drawCircle(
                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                    radius = maxRadius * 0.85f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2f)
                )

                // Swirling particles
                val boost = if (isListening) (audioLevel * 0.35f) else 0f
                val currentPulse = (corePulse + boost).coerceIn(0.8f, 1.4f)

                particles.forEach { p ->
                    val dynamicAngle = (p.baseAngle + (rotationAngle * p.speed)) * (Math.PI.toFloat() / 180f)
                    val r = maxRadius * p.distanceRatio * currentPulse
                    val px = cx + r * cos(dynamicAngle)
                    val py = cy + r * sin(dynamicAngle) * (1f - p.orbitTilt)

                    drawCircle(
                        color = p.color.copy(alpha = (0.5f + 0.4f * sin(dynamicAngle)).coerceIn(0.2f, 0.95f)),
                        radius = p.radius * (if (isSpeaking) 1.3f else 1f),
                        center = Offset(px, py)
                    )
                }

                // Inner glowing radiant core
                val coreRadius = maxRadius * 0.42f * currentPulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF22D3EE).copy(alpha = 0.85f),
                            Color(0xFF00E5FF).copy(alpha = 0.5f),
                            Color(0xFF059669).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = coreRadius * 1.5f
                    ),
                    radius = coreRadius * 1.3f,
                    center = Offset(cx, cy)
                )

                // Inner bright focal core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color(0xFF22D3EE).copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = coreRadius * 0.6f
                    ),
                    radius = coreRadius * 0.5f,
                    center = Offset(cx, cy)
                )

                // Rotating focal orbital arc
                drawArc(
                    color = Color(0xFFFDE047).copy(alpha = 0.7f),
                    startAngle = rotationAngle * 1.5f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(cx - coreRadius * 0.9f, cy - coreRadius * 0.9f),
                    size = androidx.compose.ui.geometry.Size(coreRadius * 1.8f, coreRadius * 1.8f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }

            // Status label below the center
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Controls Under Orb: [ ■ STOP ] and [ 🔊 ]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // STOP pill button (exact match to screenshot)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(StopRedBg)
                    .border(1.5.dp, StopRedBorder, RoundedCornerShape(20.dp))
                    .clickable(onClick = onStopClick)
                    .padding(horizontal = 22.dp, vertical = 9.dp)
                    .testTag("stop_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(StopRed, RoundedCornerShape(1.dp))
                )
                Text(
                    text = "STOP",
                    color = Color(0xFFFCA5A5),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Speaker/Mute Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SoundCyanBg)
                    .border(1.dp, SoundCyanBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleMute)
                    .testTag("mute_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = SoundCyanBorder,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
