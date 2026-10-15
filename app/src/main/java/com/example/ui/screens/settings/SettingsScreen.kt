package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiClient
import com.example.data.pref.FugaPreferences
import com.example.ui.components.GlassCard
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaError
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaSuccess
import com.example.ui.theme.FugaViolet
import com.example.ui.theme.FugaWarning
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: FugaPreferences,
    onBack: () -> Unit,
    onClearHistory: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToWakeWordSettings: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geminiClient = remember { GeminiClient() }

    var maskedKey by remember { mutableStateOf(prefs.getMaskedApiKey()) }
    var showEditKeyDialog by remember { mutableStateOf(false) }
    var newKeyInput by remember { mutableStateOf("") }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    var wakeWordEnabled by remember { mutableStateOf(prefs.isWakeWordEnabled) }
    var alwaysReadyEnabled by remember { mutableStateOf(prefs.isAlwaysReadyEnabled) }
    var dynamicIslandEnabled by remember { mutableStateOf(prefs.isDynamicIslandEnabled) }
    var screenAccessEnabled by remember { mutableStateOf(prefs.isScreenAccessEnabled) }
    var biometricEnabled by remember { mutableStateOf(prefs.isBiometricSecurityEnabled) }
    var memoryEnabled by remember { mutableStateOf(prefs.isMemoryEnabled) }
    var voiceFeedbackEnabled by remember { mutableStateOf(prefs.isVoiceFeedbackEnabled) }
    var bgServiceEnabled by remember { mutableStateOf(prefs.isBackgroundServiceEnabled) }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SpaceBlack, SpaceDark)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: AI / API Key
                item {
                    SettingsSectionHeader("AI / API Configuration")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = FugaCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Google AI Studio API Key",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = maskedKey,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = FugaSuccess,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Keystore AES-256 Encrypted",
                                                fontSize = 10.5.sp,
                                                color = FugaSuccess,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            newKeyInput = ""
                                            showEditKeyDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Replace key",
                                            tint = FugaPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    if (prefs.hasValidApiKey()) {
                                        IconButton(
                                            onClick = {
                                                prefs.removeApiKey()
                                                maskedKey = prefs.getMaskedApiKey()
                                                testResult = null
                                                Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove key",
                                                tint = FugaError,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Test API connection button
                            Button(
                                onClick = {
                                    val currentKey = prefs.getStoredApiKey()
                                    if (currentKey.isBlank()) {
                                        testResult = Pair(false, "No API key configured.")
                                        return@Button
                                    }
                                    isTestingConnection = true
                                    testResult = null
                                    coroutineScope.launch {
                                        val res = geminiClient.testApiKey(currentKey)
                                        isTestingConnection = false
                                        res.onSuccess {
                                            testResult = Pair(true, "Connection 200 OK: Gemini 3.5 Flash online")
                                        }.onFailure { err ->
                                            testResult = Pair(false, "Connection error: ${err.message}")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpaceCard),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(FugaPurple, FugaCyan)))
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = FugaCyan, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pinging Gemini API...", fontSize = 12.sp, color = TextPrimary)
                                } else {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = FugaCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test API Connection", fontSize = 12.sp, color = TextPrimary)
                                }
                            }

                            AnimatedVisibility(visible = testResult != null) {
                                testResult?.let { (success, msg) ->
                                    Row(
                                        modifier = Modifier.padding(top = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (success) FugaSuccess else FugaError,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = msg, fontSize = 11.5.sp, color = if (success) FugaSuccess else FugaError)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Voice & Wake Word
                item {
                    SettingsSectionHeader("Voice & Wake Word")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SettingToggleRow(
                                icon = Icons.Default.Mic,
                                title = "“Hey Fuga” Wake Word",
                                subtitle = "Detect wake phrase to activate Fuga without touching device",
                                checked = wakeWordEnabled,
                                onCheckedChange = {
                                    wakeWordEnabled = it
                                    prefs.isWakeWordEnabled = it
                                }
                            )

                            HorizontalDivider(color = SpaceBorder)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToWakeWordSettings() }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(FugaCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hearing,
                                        contentDescription = null,
                                        tint = FugaCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Wake Word Configuration",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (wakeWordEnabled) FugaSuccess.copy(alpha = 0.2f) else SpaceCard)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (wakeWordEnabled) "Active" else "Off",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (wakeWordEnabled) FugaSuccess else TextSecondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Sensitivity, chime tone, supported phrases & live test sandbox",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            HorizontalDivider(color = SpaceBorder)

                            SettingToggleRow(
                                icon = Icons.Default.Mic,
                                title = "Always Ready (Hands-Free)",
                                subtitle = "Continuously maintain wake-word readiness with automatic voice interaction",
                                checked = alwaysReadyEnabled,
                                onCheckedChange = {
                                    alwaysReadyEnabled = it
                                    prefs.isAlwaysReadyEnabled = it
                                }
                            )

                            HorizontalDivider(color = SpaceBorder)

                            SettingToggleRow(
                                icon = Icons.Default.RecordVoiceOver,
                                title = "Voice Feedback & Speech",
                                subtitle = "Fuga speaks replies aloud using Text-to-Speech",
                                checked = voiceFeedbackEnabled,
                                onCheckedChange = {
                                    voiceFeedbackEnabled = it
                                    prefs.isVoiceFeedbackEnabled = it
                                }
                            )
                        }
                    }
                }

                // Section: Dynamic Island & Background
                item {
                    SettingsSectionHeader("Dynamic Island & Background")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SettingToggleRow(
                                icon = Icons.Default.Layers,
                                title = "Floating Dynamic Island Pill",
                                subtitle = "Display active pill status over apps when working",
                                checked = dynamicIslandEnabled,
                                onCheckedChange = {
                                    dynamicIslandEnabled = it
                                    prefs.isDynamicIslandEnabled = it
                                }
                            )

                            HorizontalDivider(color = SpaceBorder)

                            SettingToggleRow(
                                icon = Icons.Default.Notifications,
                                title = "Background Standby Service",
                                subtitle = "Maintain standby ready state in background",
                                checked = bgServiceEnabled,
                                onCheckedChange = {
                                    bgServiceEnabled = it
                                    prefs.isBackgroundServiceEnabled = it
                                }
                            )
                        }
                    }
                }

                // Section: System Permissions & Restrictions
                item {
                    SettingsSectionHeader("System Permissions & Restrictions")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToPermissions() }
                                    .padding(vertical = 4.dp),
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
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = FugaCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Permissions & System Setup",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(FugaWarning.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Restrictions",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FugaWarning
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Manage mic, overlay, battery rules & remove Android 13+ restricted settings",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            HorizontalDivider(color = SpaceBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Remove Settings Restrictions",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Open App Info to select ⋮ > 'Allow restricted settings'",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = FugaWarning,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "App Info",
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Screen Access & Automation
                item {
                    SettingsSectionHeader("Screen Understanding & Automation")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingToggleRow(
                                icon = Icons.Default.Visibility,
                                title = "Screen Control & Reading",
                                subtitle = "Requires explicit Accessibility authorization. Never secretly captured.",
                                checked = screenAccessEnabled,
                                onCheckedChange = {
                                    screenAccessEnabled = it
                                    prefs.isScreenAccessEnabled = it
                                    if (it) {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }
                }

                // Section: Security & Biometrics
                item {
                    SettingsSectionHeader("Security & Biometrics")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingToggleRow(
                                icon = Icons.Default.Fingerprint,
                                title = "Biometric Verification for Payments",
                                subtitle = "Requires fingerprint or face recognition before executing financial commands",
                                checked = biometricEnabled,
                                onCheckedChange = {
                                    biometricEnabled = it
                                    prefs.isBiometricSecurityEnabled = it
                                }
                            )
                        }
                    }
                }

                // Section: Memory & History
                item {
                    SettingsSectionHeader("Memory & Conversation History")
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingToggleRow(
                                icon = Icons.Default.Psychology,
                                title = "Assistant Memory",
                                subtitle = "Remember user preferences and context across chats",
                                checked = memoryEnabled,
                                onCheckedChange = {
                                    memoryEnabled = it
                                    prefs.isMemoryEnabled = it
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = onNavigateToMemory,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Manage Memories", fontSize = 12.sp, color = FugaCyan)
                                }

                                Button(
                                    onClick = { showClearHistoryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = FugaError.copy(alpha = 0.8f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Clear All Chats", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Section: Account & About
                item {
                    SettingsSectionHeader("Account & About")
                    val userSession = prefs.getUserSession()
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = FugaPurple, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = userSession.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = if (userSession.email.isNotBlank()) userSession.email else "Local Session", fontSize = 12.sp, color = TextSecondary)
                                }
                            }

                            if (userSession.fugaGmail != null) {
                                HorizontalDivider(color = SpaceBorder)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = FugaCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = "Fuga Gmail: ${userSession.fugaGmail}", fontSize = 12.sp, color = TextSecondary)
                                }
                            }

                            HorizontalDivider(color = SpaceBorder)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAboutDialog = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = FugaCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("About Fuga AI v1.0", fontSize = 13.5.sp, color = TextPrimary)
                                }
                                Text("Details", fontSize = 12.sp, color = FugaCyan)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Dialog for Updating API Key
        if (showEditKeyDialog) {
            AlertDialog(
                onDismissRequest = { showEditKeyDialog = false },
                title = { Text("Update Gemini API Key", color = TextPrimary) },
                text = {
                    Column {
                        Text("Enter your new Google AI Studio API key.", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newKeyInput,
                            onValueChange = { newKeyInput = it },
                            label = { Text("API Key") },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FugaCyan,
                                unfocusedBorderColor = SpaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newKeyInput.isNotBlank()) {
                                prefs.setApiKey(newKeyInput.trim())
                                maskedKey = prefs.getMaskedApiKey()
                                testResult = null
                                showEditKeyDialog = false
                                Toast.makeText(context, "API Key updated securely", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FugaPurple)
                    ) {
                        Text("Save Key")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditKeyDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SpaceCard
            )
        }

        // Dialog for Clear History
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear Conversation History", color = TextPrimary) },
                text = { Text("Are you sure you want to delete all messages? This action cannot be undone.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearHistory()
                            showClearHistoryDialog = false
                            Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FugaError)
                    ) {
                        Text("Clear All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SpaceCard
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("Fuga AI Version 1.0", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Next-generation personal AI assistant built with privacy-first architecture.\n\n" +
                            "• Wake Word: “Hey Fuga”\n" +
                            "• Dynamic Island Pill\n" +
                            "• Gemini 3.5 Flash Model\n" +
                            "• Biometric Payment Security\n" +
                            "• Local Room Database Storage",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = FugaPurple)
                    ) {
                        Text("Close")
                    }
                },
                containerColor = SpaceCard
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        color = FugaCyan,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(FugaPurple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = FugaCyan, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.5.sp, color = TextSecondary, lineHeight = 15.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FugaPurple,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SpaceCard
            )
        )
    }
}
