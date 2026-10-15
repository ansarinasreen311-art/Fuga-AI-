package com.example.ui.screens.agents

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.model.CustomAgent
import com.example.ui.components.GlassCard
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaError
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaSuccess
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAgentScreen(
    agents: List<CustomAgent>,
    activeAgent: CustomAgent?,
    onSelectAgent: (Long) -> Unit,
    onCreateAgent: (String, String, String, String, String, String) -> Unit,
    onDeleteAgent: (Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }

    var agentName by remember { mutableStateOf("") }
    var agentRole by remember { mutableStateOf("") }
    var agentPrompt by remember { mutableStateOf("") }
    var agentCapabilities by remember { mutableStateOf("Custom Architecture, Code Generation") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Custom AI Agents", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                        Text(
                            text = "Active: ${activeAgent?.name ?: "Fuga Prime"}",
                            fontSize = 12.sp,
                            color = FugaCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = FugaPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("create_agent_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Agent")
            }
        },
        containerColor = SpaceBlack,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("custom_agent_screen")
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(SpaceBlack, SpaceDark)))
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Choose or create specialized AI agents tailored for your workflow.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(agents, key = { it.id }) { agent ->
                    val isActive = agent.id == (activeAgent?.id ?: 1L)
                    val accent = try {
                        Color(android.graphics.Color.parseColor(agent.accentColorHex))
                    } catch (e: Exception) {
                        FugaPurple
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAgent(agent.id) },
                        shape = RoundedCornerShape(20.dp),
                        borderColor = if (isActive) accent else SpaceBorder
                    ) {
                        Column {
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
                                            .background(accent.copy(alpha = 0.2f))
                                            .border(1.5.dp, accent, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getAgentIcon(agent.iconName),
                                            contentDescription = null,
                                            tint = accent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = agent.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (agent.isDefault) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(SpaceCard)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("PRESET", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(
                                            text = agent.role,
                                            fontSize = 12.sp,
                                            color = FugaCyan
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(FugaSuccess.copy(alpha = 0.2f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = FugaSuccess,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FugaSuccess)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { onSelectAgent(agent.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SpaceCard),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Switch", fontSize = 12.sp, color = TextPrimary)
                                        }
                                    }

                                    if (!agent.isDefault) {
                                        IconButton(onClick = { onDeleteAgent(agent.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = FugaError,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = agent.systemPrompt,
                                fontSize = 12.5.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp,
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                agent.capabilities.split(",").take(3).forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(accent.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = tag.trim(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = accent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create Custom Agent", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = agentName,
                            onValueChange = { agentName = it },
                            label = { Text("Agent Name") },
                            placeholder = { Text("e.g. Flutter Architect, Data Analyst") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FugaCyan,
                                unfocusedBorderColor = SpaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = agentRole,
                            onValueChange = { agentRole = it },
                            label = { Text("Role Description") },
                            placeholder = { Text("e.g. Senior Cross-Platform Engineer") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FugaCyan,
                                unfocusedBorderColor = SpaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = agentPrompt,
                            onValueChange = { agentPrompt = it },
                            label = { Text("System Instructions") },
                            placeholder = { Text("Detailed prompt explaining how this agent should think, write code, and answer queries...") },
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FugaCyan,
                                unfocusedBorderColor = SpaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = agentCapabilities,
                            onValueChange = { agentCapabilities = it },
                            label = { Text("Capabilities (Comma-separated)") },
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
                            if (agentName.isNotBlank() && agentPrompt.isNotBlank()) {
                                onCreateAgent(
                                    agentName.trim(),
                                    agentRole.trim().ifBlank { "Specialized AI Agent" },
                                    agentPrompt.trim(),
                                    "smart_toy",
                                    "#8B5CF6",
                                    agentCapabilities.trim()
                                )
                                agentName = ""
                                agentRole = ""
                                agentPrompt = ""
                                showCreateDialog = false
                                Toast.makeText(context, "Custom Agent created!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FugaPurple)
                    ) {
                        Text("Create Agent")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SpaceCard
            )
        }
    }
}

fun getAgentIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "android" -> Icons.Default.Android
        "web" -> Icons.Default.Language
        "security" -> Icons.Default.Security
        "code" -> Icons.Default.Code
        "psychology" -> Icons.Default.Psychology
        else -> Icons.Default.SmartToy
    }
}
