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
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    isMemoryActive: Boolean = false,
    onOrbClick: () -> Unit,
    onStopClick: () -> Unit,
    onToggleMute: () -> Unit,
    onMiniModeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_animation")

    // Dynamic rotation speed (3.5x faster when thinking)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isThinking) 3500 else if (isMemoryActive) 5000 else 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isThinking) 800 else 2000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Liquid ripple wave for speech / thinking
    val rippleWave by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple"
    )

    // Pre-generate stable particles (Cyan, Gold/Yellow, and Green)
    val particles = remember {
        val list = mutableListOf<Particle>()
        val colors = listOf(
            ParticleCyan,
            ParticleYellow,
            ParticleGreen,
            Color(0xFF67E8F9),
            Color(0xFFFDE68A),
            Color(0xFF86EFAC),
            Color(0xFF38BDF8),
            Color(0xFFF59E0B)
        )
        for (i in 0 until 200) {
            val angle = (i * 137.5f) % 360f
            val dist = 0.22f + 0.70f * (i.toFloat() / 200f)
            val speed = if (i % 2 == 0) 1f else -0.8f
            val rad = 1.6f + (i % 4) * 1.1f
            val col = colors[i % colors.size]
            val tilt = (i % 5) * 0.16f
            list.add(Particle(angle, dist, speed, rad, col, tilt))
        }
        list
    }

    val statusText = when {
        isSpeaking -> "• SPEAKING •"
        isThinking -> "• NEURAL THINKING •"
        isMemoryActive -> "• ACCESSING MEMORY •"
        isListening -> "• LISTENING •"
        isAlwaysVoiceRun -> "• ACTIVE LISTENING •"
        else -> "• STANDBY •"
    }

    val statusColor = when {
        isSpeaking -> Color(0xFF6EE7B7)
        isThinking -> Color(0xFFFBBF24)
        isMemoryActive -> Color(0xFF38BDF8)
        isListening -> MemoryCyan
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

                // Thinking / Speaking liquid ripple wave aura
                if (isThinking || isSpeaking || isMemoryActive) {
                    val waveRadius = maxRadius * rippleWave
                    val waveAlpha = (1f - (rippleWave - 0.4f) / 0.9f).coerceIn(0f, 0.6f)
                    drawCircle(
                        color = if (isThinking) Color(0xFFF59E0B).copy(alpha = waveAlpha)
                        else if (isMemoryActive) MemoryCyan.copy(alpha = waveAlpha)
                        else Color(0xFF10B981).copy(alpha = waveAlpha),
                        radius = waveRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                // Outer thin cyber glow ring
                drawCircle(
                    color = if (isThinking) Color(0xFFF59E0B).copy(alpha = 0.7f)
                    else if (isMemoryActive) Color(0xFF38BDF8).copy(alpha = 0.8f)
                    else Color(0xFF00E5FF).copy(alpha = 0.5f),
                    radius = maxRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2f)
                )

                // Secondary orbital ellipse
                drawCircle(
                    color = if (isThinking) Color(0xFFEF4444).copy(alpha = 0.4f)
                    else Color(0xFF10B981).copy(alpha = 0.3f),
                    radius = maxRadius * 0.86f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4f)
                )

                // Swirling particles calculation
                val boost = if (isListening) (audioLevel * 0.4f) else if (isThinking) 0.25f else 0f
                val currentPulse = (corePulse + boost).coerceIn(0.8f, 1.45f)

                particles.forEach { p ->
                    val multiplier = if (isThinking) 2.5f else 1f
                    val dynamicAngle = (p.baseAngle + (rotationAngle * p.speed * multiplier)) * (Math.PI.toFloat() / 180f)
                    val r = maxRadius * p.distanceRatio * currentPulse
                    val px = cx + r * cos(dynamicAngle)
                    val py = cy + r * sin(dynamicAngle) * (1f - p.orbitTilt)

                    val pColor = if (isThinking && p.speed > 0) Color(0xFFFDE047)
                    else if (isMemoryActive && p.speed < 0) Color(0xFF38BDF8)
                    else p.color

                    drawCircle(
                        color = pColor.copy(alpha = (0.55f + 0.4f * sin(dynamicAngle)).coerceIn(0.2f, 0.98f)),
                        radius = p.radius * (if (isSpeaking || isThinking) 1.4f else 1f),
                        center = Offset(px, py)
                    )
                }

                // Inner glowing radiant core
                val coreRadius = maxRadius * 0.44f * currentPulse
                val coreGradientColors = if (isThinking) {
                    listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.9f),
                        Color(0xFFEF4444).copy(alpha = 0.6f),
                        Color(0xFF7C2D12).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                } else if (isMemoryActive) {
                    listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.95f),
                        Color(0xFF0284C7).copy(alpha = 0.6f),
                        Color(0xFF0369A1).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                } else {
                    listOf(
                        Color(0xFF22D3EE).copy(alpha = 0.9f),
                        Color(0xFF00E5FF).copy(alpha = 0.55f),
                        Color(0xFF059669).copy(alpha = 0.3f),
                        Color.Transparent
                    )
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = coreGradientColors,
                        center = Offset(cx, cy),
                        radius = coreRadius * 1.5f
                    ),
                    radius = coreRadius * 1.35f,
                    center = Offset(cx, cy)
                )

                // Inner bright focal core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            if (isThinking) Color(0xFFFDE047) else Color(0xFF22D3EE).copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = coreRadius * 0.65f
                    ),
                    radius = coreRadius * 0.55f,
                    center = Offset(cx, cy)
                )

                // Rotating focal orbital arc
                drawArc(
                    color = if (isThinking) Color(0xFFEF4444) else Color(0xFFFDE047).copy(alpha = 0.85f),
                    startAngle = rotationAngle * (if (isThinking) 3f else 1.5f),
                    sweepAngle = if (isThinking) 120f else 75f,
                    useCenter = false,
                    topLeft = Offset(cx - coreRadius * 0.95f, cy - coreRadius * 0.95f),
                    size = Size(coreRadius * 1.9f, coreRadius * 1.9f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Status label below the center
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
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

        // Controls Under Orb: [ ■ STOP ], [ 🔊 ], [ 🗖 MINI ]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // STOP pill button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(StopRedBg)
                    .border(1.5.dp, StopRedBorder, RoundedCornerShape(20.dp))
                    .clickable(onClick = onStopClick)
                    .padding(horizontal = 20.dp, vertical = 9.dp)
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

            // Mini Screen / Floating Mode Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0F231B))
                    .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onMiniModeClick)
                    .testTag("mini_mode_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureInPictureAlt,
                    contentDescription = "Mini Floating Screen",
                    tint = Color(0xFF4ADE80),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
