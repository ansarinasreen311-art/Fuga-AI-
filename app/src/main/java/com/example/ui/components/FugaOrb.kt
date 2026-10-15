package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.DynamicIslandStatus
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaViolet

@Composable
fun FugaOrb(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    status: DynamicIslandStatus = DynamicIslandStatus.IDLE,
    audioLevel: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rotation"
    )

    val dynamicBoost = when (status) {
        DynamicIslandStatus.LISTENING -> (audioLevel * 0.35f).coerceIn(0.05f, 0.4f)
        DynamicIslandStatus.SPEAKING -> 0.15f
        DynamicIslandStatus.THINKING -> 0.08f
        else -> 0f
    }

    val finalScale = pulseScale + dynamicBoost

    Box(
        modifier = modifier
            .size(size)
            .testTag("fuga_orb_container")
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing cosmic aura ring 1
        Box(
            modifier = Modifier
                .size(size * 1.15f)
                .scale(finalScale)
                .rotate(auraRotation)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            FugaPurple.copy(alpha = 0.4f),
                            FugaCyan.copy(alpha = 0.5f),
                            FugaPink.copy(alpha = 0.35f),
                            FugaViolet.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // Middle glowing halo
        Box(
            modifier = Modifier
                .size(size * 1.05f)
                .scale(if (status == DynamicIslandStatus.LISTENING) 1.1f else 1.0f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            FugaCyan.copy(alpha = 0.25f),
                            FugaPurple.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // Core Fuga AI Planet Orb
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(FugaCyan, FugaPurple, FugaPink, FugaCyan)
                    ),
                    shape = CircleShape
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.fuga_orb),
                contentDescription = "Fuga AI Planet Orb",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
