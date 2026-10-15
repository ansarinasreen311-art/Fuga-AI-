package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.CustomAgent
import com.example.data.model.DynamicIslandState
import com.example.data.model.DynamicIslandStatus
import com.example.data.model.UserSession
import com.example.ui.components.DynamicIslandPill
import com.example.ui.components.FugaOrb
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaSuccess
import com.example.ui.theme.FugaViolet
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    userSession: UserSession,
    activeAgent: CustomAgent?,
    messages: List<ChatMessage>,
    dynamicIslandState: DynamicIslandState,
    isListening: Boolean,
    audioLevel: Float,
    isAlwaysReadyActive: Boolean = false,
    onSendMessage: (String, Boolean) -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onCloseDynamicIsland: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    onExecuteAction: (ChatMessage) -> Unit,
    onBiometricVerifyPayment: (ChatMessage) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToAppBuilder: () -> Unit,
    onNavigateToWebsiteBuilder: () -> Unit,
    onStartAudioCall: () -> Unit,
    onSimulateWakeWord: () -> Unit,
    onNavigateToWakeWordSettings: () -> Unit = {}
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickActions = listOf(
        Pair("📞 Audio Call with Fuga", "audio_call"),
        Pair("📱 Build Android App", "build_app"),
        Pair("🌐 Build Website", "build_web"),
        Pair("🤖 Custom Agents", "agents"),
        Pair("Open YouTube", "launch"),
        Pair("Turn on flashlight", "torch"),
        Pair("Set alarm for 7 AM", "alarm"),
        Pair("Pay ₹500 to Rahul", "payment"),
        Pair("Read this screen", "screen")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SpaceBlack, SpaceDark, Color(0xFF090D1C))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Dynamic Island, Title, & Action Icons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Floating Dynamic Island Pill
                DynamicIslandPill(
                    state = dynamicIslandState,
                    onCloseClick = onCloseDynamicIsland,
                    onPillClick = {
                        if (isListening) onStopVoiceListening() else onStartVoiceListening()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(listOf(FugaPurple, FugaCyan, FugaPink))
                                )
                                .padding(1.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SpaceBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userSession.displayName.firstOrNull()?.toString()?.uppercase() ?: "F",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FugaCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Fuga AI",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Agent: ${activeAgent?.name ?: "Fuga Prime"}",
                                fontSize = 11.5.sp,
                                color = FugaCyan,
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateToAgents,
                            modifier = Modifier.testTag("agents_nav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Custom Agents",
                                tint = FugaPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToMemory,
                            modifier = Modifier.testTag("memory_nav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Memory",
                                tint = FugaCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.testTag("history_nav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Conversation History",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = onStartAudioCall,
                            modifier = Modifier.testTag("audio_call_nav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneInTalk,
                                contentDescription = "Start Audio Call",
                                tint = FugaCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.testTag("settings_nav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Always Ready Status Banner & Quick Audio Call Access
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (isAlwaysReadyActive) FugaSuccess.copy(alpha = 0.15f) else Color(0x331E1E2C),
                                    Color(0x3300FFFF)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            if (isAlwaysReadyActive) FugaSuccess.copy(alpha = 0.35f) else SpaceBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onNavigateToWakeWordSettings() }
                            .testTag("always_ready_indicator")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isAlwaysReadyActive) FugaSuccess else TextSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAlwaysReadyActive) "Always Ready • Say “Hey Fuga”" else "Wake Word Standby",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAlwaysReadyActive) FugaSuccess else TextSecondary
                        )
                    }

                    // Call Button Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FugaCyan.copy(alpha = 0.2f))
                            .clickable { onStartAudioCall() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("home_call_fuga_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = FugaCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Call Fuga",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FugaCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Feature Action Bar: Agents • Build App • Build Website
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(FugaPurple.copy(alpha = 0.25f), SpaceCard))
                            )
                            .border(1.dp, FugaPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { onNavigateToAgents() }
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .testTag("shortcut_agents"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = FugaPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agents", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(FugaSuccess.copy(alpha = 0.2f), SpaceCard))
                            )
                            .border(1.dp, FugaSuccess.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { onNavigateToAppBuilder() }
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .testTag("shortcut_build_app"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = FugaSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Build App", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(FugaCyan.copy(alpha = 0.2f), SpaceCard))
                            )
                            .border(1.dp, FugaCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { onNavigateToWebsiteBuilder() }
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .testTag("shortcut_build_web"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = FugaCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Build Website", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            // Middle Area: If no messages, show prominent Fuga Orb & Greeting. If messages exist, show chat.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FugaOrb(
                            size = 160.dp,
                            status = if (isListening) DynamicIslandStatus.LISTENING else dynamicIslandState.status,
                            audioLevel = audioLevel,
                            onClick = {
                                if (isListening) onStopVoiceListening() else onStartVoiceListening()
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Hey there, how can I help you?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Active Persona: ${activeAgent?.name ?: "Fuga Prime"}\nAsk anything, generate code, or build apps & websites.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageItem(
                                message = msg,
                                onSpeak = onSpeakMessage,
                                onExecuteAction = onExecuteAction,
                                onBiometricVerifyPayment = onBiometricVerifyPayment
                            )
                        }
                    }
                }
            }

            // Bottom Action Area: Quick Action Chips + Input bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceDark.copy(alpha = 0.95f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Quick Action Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 10.dp)
                ) {
                    items(quickActions) { (label, actionKey) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(SpaceCard)
                                .border(1.dp, SpaceBorder, RoundedCornerShape(16.dp))
                                .clickable {
                                    when (actionKey) {
                                        "audio_call" -> onStartAudioCall()
                                        "build_app" -> onNavigateToAppBuilder()
                                        "build_web" -> onNavigateToWebsiteBuilder()
                                        "agents" -> onNavigateToAgents()
                                        else -> onSendMessage(label, false)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("quick_action_$label")
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = FugaCyan
                            )
                        }
                    }
                }

                // Input Bar with Mic & Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Microphone button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) Brush.horizontalGradient(listOf(FugaPink, FugaPurple))
                                else Brush.horizontalGradient(listOf(FugaPurple, FugaViolet))
                            )
                            .clickable {
                                if (isListening) onStopVoiceListening() else onStartVoiceListening()
                            }
                            .testTag("mic_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop listening" else "Start voice",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Text field
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input"),
                        placeholder = {
                            Text(
                                text = if (isListening) "Listening to speech..." else "Ask Fuga or build anything...",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FugaCyan,
                            unfocusedBorderColor = SpaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SpaceCard,
                            unfocusedContainerColor = SpaceCard
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    val text = textInput.trim()
                                    textInput = ""
                                    onSendMessage(text, false)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val text = textInput.trim()
                                textInput = ""
                                onSendMessage(text, false)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (textInput.isNotBlank()) FugaCyan else SpaceCard)
                            .testTag("send_message_button"),
                        enabled = textInput.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) SpaceBlack else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
