package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DynamicIslandState
import com.example.data.model.DynamicIslandStatus
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DynamicIslandPill(
    state: DynamicIslandState,
    onCloseClick: () -> Unit,
    onPillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.status != DynamicIslandStatus.CLOSE,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 380.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SpaceBlack,
                            Color(0xFF0F1424),
                            SpaceBlack
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            FugaPurple.copy(alpha = 0.8f),
                            FugaCyan.copy(alpha = 0.8f),
                            FugaPink.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .clickable { onPillClick() }
                .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("dynamic_island_pill"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mini Orb Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, FugaCyan, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fuga_orb),
                        contentDescription = "Fuga Pill Orb",
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Center state-based visualizer
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (state.status) {
                                DynamicIslandStatus.IDLE -> "Fuga Ready"
                                DynamicIslandStatus.LISTENING -> "Listening..."
                                DynamicIslandStatus.THINKING -> "Thinking..."
                                DynamicIslandStatus.SPEAKING -> "Fuga Speaking"
                                DynamicIslandStatus.CLOSE -> "Standby"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when (state.status) {
                                DynamicIslandStatus.LISTENING -> FugaCyan
                                DynamicIslandStatus.THINKING -> FugaPurple
                                DynamicIslandStatus.SPEAKING -> FugaPink
                                else -> TextPrimary
                            }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Visual indicator according to state
                        when (state.status) {
                            DynamicIslandStatus.LISTENING -> {
                                WaveformVisualizer(
                                    barCount = 8,
                                    audioLevel = state.audioLevel,
                                    width = 60.dp,
                                    height = 18.dp,
                                    isActive = true
                                )
                            }
                            DynamicIslandStatus.SPEAKING -> {
                                WaveformVisualizer(
                                    barCount = 8,
                                    audioLevel = 0.8f,
                                    width = 60.dp,
                                    height = 18.dp,
                                    isActive = true
                                )
                            }
                            DynamicIslandStatus.THINKING -> {
                                BouncingDotsIndicator()
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(FugaCyan)
                                )
                            }
                        }
                    }

                    if (state.subtitle.isNotBlank() && state.status != DynamicIslandStatus.IDLE) {
                        Text(
                            text = state.subtitle,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Close / Deactivate Button
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("dynamic_island_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dynamic Island",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BouncingDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(5.dp).scale(dot1).clip(CircleShape).background(FugaPurple))
        Box(modifier = Modifier.size(5.dp).scale(dot2).clip(CircleShape).background(FugaCyan))
        Box(modifier = Modifier.size(5.dp).scale(dot3).clip(CircleShape).background(FugaPink))
    }
}
