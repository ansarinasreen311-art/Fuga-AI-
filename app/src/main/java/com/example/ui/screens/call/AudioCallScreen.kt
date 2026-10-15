package com.example.ui.screens.call

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallStatus
import com.example.data.model.CallTranscriptItem
import com.example.data.model.CustomAgent
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaError
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaSuccess
import com.example.ui.theme.FugaViolet
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AudioCallScreen(
    activeAgent: CustomAgent?,
    callStatus: CallStatus,
    durationSeconds: Int,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    audioLevel: Float,
    transcripts: List<CallTranscriptItem>,
    liveCaption: String,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onInterrupt: () -> Unit,
    onEndCall: () -> Unit
) {
    BackHandler {
        onEndCall()
    }

    var showTranscriptDrawer by remember { mutableStateOf(false) }
    val transcriptListState = rememberLazyListState()

    LaunchedEffect(transcripts.size) {
        if (transcripts.isNotEmpty()) {
            transcriptListState.animateScrollToItem(transcripts.size - 1)
        }
    }

    val agentName = activeAgent?.name ?: "Fuga Prime"
    val agentRole = activeAgent?.role ?: "Conversational AI Assistant"

    // Pulsing animations for glowing audio orb
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val formattedDuration = remember(durationSeconds) {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    val statusColor by animateColorAsState(
        targetValue = when (callStatus) {
            CallStatus.LISTENING -> FugaSuccess
            CallStatus.SPEAKING -> FugaViolet
            CallStatus.THINKING -> FugaCyan
            CallStatus.MUTED -> Color(0xFFFFA000)
            CallStatus.CONNECTING -> FugaPink
            else -> TextSecondary
        },
        label = "status_color"
    )

    val statusText = when (callStatus) {
        CallStatus.CONNECTING -> "Connecting to Fuga AI voice channel..."
        CallStatus.LISTENING -> "Listening to you... Speak freely"
        CallStatus.THINKING -> "Thinking..."
        CallStatus.SPEAKING -> "$agentName is speaking..."
        CallStatus.MUTED -> "Microphone Muted"
        CallStatus.ENDED -> "Call Ended"
        CallStatus.IDLE -> "Standby"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF161033),
                        SpaceDark,
                        SpaceBlack
                    ),
                    radius = 1200f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("audio_call_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Security Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier.testTag("call_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "End Call",
                        tint = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = FugaCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Encrypted Audio Channel",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showTranscriptDrawer = !showTranscriptDrawer },
                    modifier = Modifier.testTag("toggle_transcript_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = "Toggle Captions",
                        tint = if (showTranscriptDrawer) FugaCyan else TextSecondary
                    )
                }
            }

            // Caller Identity & Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = agentName,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = agentRole,
                    fontSize = 13.sp,
                    color = FugaCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = formattedDuration,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }

            // Central Animated Holographic Voice Orb & Waveform Visualizer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .padding(vertical = 10.dp)
            ) {
                // Expanding glowing ripple rings reacting to audio level
                val dynamicScale = (1f + (audioLevel * 0.45f)).coerceIn(1f, 1.6f)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(dynamicScale * pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Central high-tech orb
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(FugaPurple, FugaCyan, FugaPink, FugaViolet, FugaPurple)
                            )
                        )
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1D183B),
                                        SpaceBlack
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Live dynamic equalizer bars inside orb
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val barCount = 7
                            for (i in 0 until barCount) {
                                val barFactor = remember(i, audioLevel) {
                                    val variance = kotlin.math.sin(i * 0.8 + durationSeconds * 2).toFloat()
                                    ((audioLevel * 0.7f + variance * 0.3f).coerceIn(0.15f, 1f))
                                }
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height((36.dp * barFactor).coerceAtLeast(8.dp))
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(statusColor, FugaCyan)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Live Status Indicator Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x331E1E2C),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Live Caption / Transcript Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x33131726))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                if (showTranscriptDrawer) {
                    // Full scrollable conversation transcript
                    LazyColumn(
                        state = transcriptListState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (transcripts.isEmpty()) {
                            item {
                                Text(
                                    text = "Start speaking to see transcript history...",
                                    fontSize = 12.5.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        items(transcripts) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (item.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (item.isUser) Color(0x338A2BE2) else Color(0x3300FFFF),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.5.dp,
                                        if (item.isUser) FugaPurple.copy(alpha = 0.4f) else FugaCyan.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text(
                                            text = item.speaker,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isUser) FugaPink else FugaCyan
                                        )
                                        Text(
                                            text = item.text,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Real-time floating subtitle view
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val activeText = when {
                            liveCaption.isNotBlank() -> liveCaption
                            transcripts.isNotEmpty() -> transcripts.last().text
                            callStatus == CallStatus.LISTENING -> "“Hey Fuga, can you draft an app or give me an update?”"
                            else -> "Live speech recognized will appear here..."
                        }
                        Text(
                            text = activeText,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Bottom In-Call Controls Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute / Unmute Button
                CallControlButton(
                    icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (isMuted) "Unmute" else "Mute",
                    isActive = isMuted,
                    activeColor = Color(0xFFFFA000),
                    testTag = "call_mute_button",
                    onClick = onToggleMute
                )

                // Barge-in / Interrupt Button
                CallControlButton(
                    icon = Icons.Default.Stop,
                    label = "Interrupt",
                    isActive = callStatus == CallStatus.SPEAKING,
                    activeColor = FugaPink,
                    testTag = "call_interrupt_button",
                    onClick = onInterrupt
                )

                // Speakerphone Toggle Button
                CallControlButton(
                    icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    label = if (isSpeakerOn) "Speaker" else "Earpiece",
                    isActive = isSpeakerOn,
                    activeColor = FugaCyan,
                    testTag = "call_speaker_button",
                    onClick = onToggleSpeaker
                )

                // End Call Button (Primary Red)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(FugaError)
                        .clickable(onClick = onEndCall)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.25f) else Color(0x33252538))
                .border(
                    1.dp,
                    if (isActive) activeColor else Color(0x33FFFFFF),
                    CircleShape
                )
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) activeColor else TextSecondary
        )
    }
}
