package com.example.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DynamicIslandStatus
import com.example.data.pref.FugaPreferences
import com.example.ui.components.FugaOrb
import com.example.ui.components.GlassCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaError
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaSuccess
import com.example.ui.theme.FugaWarning
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WakeWordSettingsScreen(
    prefs: FugaPreferences,
    onBack: () -> Unit,
    onSimulateWakeWord: () -> Unit,
    onNavigateToPermissions: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var wakeWordEnabled by remember { mutableStateOf(prefs.isWakeWordEnabled) }
    var alwaysReadyEnabled by remember { mutableStateOf(prefs.isAlwaysReadyEnabled) }
    var chimeEnabled by remember { mutableStateOf(prefs.isWakeWordChimeEnabled) }
    var hapticEnabled by remember { mutableStateOf(prefs.isWakeWordHapticEnabled) }
    var sensitivity by remember { mutableStateOf(prefs.wakeWordSensitivity) }

    var testTriggered by remember { mutableStateOf(false) }
    var testAudioLevel by remember { mutableFloatStateOf(0.1f) }

    val hasMic = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isBatteryUnrestricted = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true

    LaunchedEffect(testTriggered) {
        if (testTriggered) {
            testAudioLevel = 0.85f
            delay(1800)
            testAudioLevel = 0.1f
            testTriggered = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SpaceBlack, SpaceDark)))
            .padding(horizontal = 20.dp)
            .testTag("wake_word_settings_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top App Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("wake_word_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Wake Word Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Toggle & configure 'Hey Fuga' detection",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // HERO CARD: MASTER TOGGLE
                item {
                    val heroBorderColor by animateColorAsState(
                        targetValue = if (wakeWordEnabled) FugaCyan else SpaceBorder,
                        animationSpec = tween(300),
                        label = "heroBorder"
                    )

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, heroBorderColor, RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (wakeWordEnabled) FugaCyan.copy(alpha = 0.2f)
                                                else SpaceCard
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (wakeWordEnabled) Icons.Default.Hearing else Icons.Default.MicOff,
                                            contentDescription = null,
                                            tint = if (wakeWordEnabled) FugaCyan else TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = "“Hey Fuga” Wake Word",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (wakeWordEnabled) "Hands-free voice trigger active" else "Voice trigger is turned off",
                                            fontSize = 12.sp,
                                            color = if (wakeWordEnabled) FugaCyan else TextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = wakeWordEnabled,
                                    onCheckedChange = {
                                        wakeWordEnabled = it
                                        prefs.isWakeWordEnabled = it
                                    },
                                    modifier = Modifier.testTag("wake_word_master_switch"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = FugaPurple,
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = SpaceCard
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Orb Status Preview
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FugaOrb(
                                    size = 110.dp,
                                    status = if (wakeWordEnabled) DynamicIslandStatus.LISTENING else DynamicIslandStatus.IDLE,
                                    audioLevel = if (wakeWordEnabled) testAudioLevel else 0.05f
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (wakeWordEnabled) FugaSuccess.copy(alpha = 0.15f)
                                        else SpaceCard
                                    )
                                    .border(
                                        1.dp,
                                        if (wakeWordEnabled) FugaSuccess.copy(alpha = 0.4f) else SpaceBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (wakeWordEnabled) FugaSuccess else TextSecondary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (wakeWordEnabled) "LISTENING FOR “HEY FUGA”" else "DETECTION PAUSED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (wakeWordEnabled) FugaSuccess else TextSecondary,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TEST DETECTION SANDBOX
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(FugaPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = FugaCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Test Wake Word Trigger",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Verify microphone recognition & wake response",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Waveform Visualizer
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SpaceCard)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                WaveformVisualizer(
                                    isActive = wakeWordEnabled || testTriggered,
                                    audioLevel = testAudioLevel,
                                    barCount = 28,
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Test Trigger Success Banner
                            AnimatedVisibility(
                                visible = testTriggered,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FugaSuccess.copy(alpha = 0.2f))
                                        .border(1.dp, FugaSuccess, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FugaSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "“Hey Fuga” detected! System audio & visualizer triggered.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FugaSuccess
                                        )
                                    }
                                }
                            }

                            if (testTriggered) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Button(
                                onClick = {
                                    testTriggered = true
                                    onSimulateWakeWord()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("simulate_wake_word_test_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FugaPurple,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Simulate “Hey Fuga” Test",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // BEHAVIOR & SOUND CONTROLS
                item {
                    Text(
                        text = "BEHAVIOR & FEEDBACK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Always Ready
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Always Ready (Standby Mode)",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Keep wake-word detection armed in background when device is idle",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Switch(
                                    checked = alwaysReadyEnabled,
                                    onCheckedChange = {
                                        alwaysReadyEnabled = it
                                        prefs.isAlwaysReadyEnabled = it
                                    },
                                    modifier = Modifier.testTag("always_ready_switch"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = FugaPurple
                                    )
                                )
                            }

                            HorizontalDivider(color = SpaceBorder)

                            // Wake Sound Chime
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Activation Tone / Chime",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Play pleasant beep confirmation when 'Hey Fuga' is triggered",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Switch(
                                    checked = chimeEnabled,
                                    onCheckedChange = {
                                        chimeEnabled = it
                                        prefs.isWakeWordChimeEnabled = it
                                    },
                                    modifier = Modifier.testTag("wake_chime_switch"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = FugaPurple
                                    )
                                )
                            }

                            HorizontalDivider(color = SpaceBorder)

                            // Haptic Vibration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Haptic Vibration",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Vibrate softly upon recognizing the wake phrase",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Switch(
                                    checked = hapticEnabled,
                                    onCheckedChange = {
                                        hapticEnabled = it
                                        prefs.isWakeWordHapticEnabled = it
                                    },
                                    modifier = Modifier.testTag("wake_haptic_switch"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = FugaPurple
                                    )
                                )
                            }
                        }
                    }
                }

                // SENSITIVITY SELECTOR
                item {
                    Text(
                        text = "DETECTION SENSITIVITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Adjust how aggressively the wake word detector triggers:",
                                fontSize = 12.5.sp,
                                color = TextSecondary
                            )

                            val sensitivityOptions = listOf(
                                "High" to "Easiest pickup, best for noisy rooms or distance",
                                "Balanced" to "Recommended default for battery and accuracy",
                                "Strict" to "Strict matching, avoids false positive triggers"
                            )

                            sensitivityOptions.forEach { (level, desc) ->
                                val isSelected = sensitivity == level
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) FugaPurple.copy(alpha = 0.25f)
                                            else SpaceCard
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) FugaCyan else SpaceBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            sensitivity = level
                                            prefs.wakeWordSensitivity = level
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, if (isSelected) FugaCyan else TextSecondary, CircleShape)
                                                .padding(3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(FugaCyan)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = level,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) FugaCyan else TextPrimary
                                            )
                                            Text(
                                                text = desc,
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SUPPORTED PHRASES
                item {
                    Text(
                        text = "SUPPORTED WAKE PHRASES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "You can activate Fuga AI with any of these voice phrases:",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val phrases = listOf(
                                    "“Hey Fuga”" to true,
                                    "“Fuga”" to false,
                                    "“Ok Fuga”" to false,
                                    "“Hi Fuga”" to false,
                                    "“Hello Fuga”" to false
                                )
                                phrases.forEach { (phrase, isPrimary) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isPrimary) FugaPurple.copy(alpha = 0.35f)
                                                else SpaceCard
                                            )
                                            .border(
                                                1.dp,
                                                if (isPrimary) FugaCyan else SpaceBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = phrase,
                                            fontSize = 12.sp,
                                            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isPrimary) FugaCyan else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // PERMISSIONS & BATTERY QUICK LINK
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPermissions() },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FugaPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = FugaCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "System Permissions & Standby",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (hasMic && isBatteryUnrestricted)
                                        "Microphone & Unrestricted Battery active ✓"
                                    else
                                        "Ensure microphone & unrestricted battery are allowed",
                                    fontSize = 11.5.sp,
                                    color = if (hasMic && isBatteryUnrestricted) FugaSuccess else FugaWarning
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Manage",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
