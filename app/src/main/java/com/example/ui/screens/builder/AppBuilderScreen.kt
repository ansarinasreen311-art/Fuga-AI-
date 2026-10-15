package com.example.ui.screens.builder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builder.ExportResult
import com.example.builder.ProjectZipExporter
import com.example.ui.components.GlassCard
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBuilderScreen(
    onGenerateApp: suspend (String, String) -> Map<String, String>,
    onSaveProject: (String, String, Map<String, String>) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var appTitle by remember { mutableStateOf("Habit Tracker") }
    var promptInput by remember { mutableStateOf("Create a modern Jetpack Compose daily habit & streak tracker with dark theme and statistics cards.") }
    var isGenerating by remember { mutableStateOf(false) }

    var generatedFiles by remember { mutableStateOf<Map<String, String>?>(null) }
    var selectedFileKey by remember { mutableStateOf<String?>(null) }
    var exportResult by remember { mutableStateOf<ExportResult?>(null) }

    val quickTemplates = listOf(
        Pair("Habit Tracker", "Create a daily habit and streak tracker with statistics and check-in buttons."),
        Pair("Crypto Portfolio", "Create a cryptocurrency portfolio tracker with live assets, P&L cards, and watchlist."),
        Pair("Fitness Log", "Create a workout and gym log app with exercise sets, timer, and calories tracker."),
        Pair("Notes & Tasks", "Create a clean Markdown notes and task manager with priority tags.")
    )

    fun startBuild() {
        if (promptInput.isBlank()) return
        isGenerating = true
        coroutineScope.launch {
            val files = onGenerateApp(promptInput.trim(), appTitle.trim().ifBlank { "ModernApp" })
            generatedFiles = files
            selectedFileKey = files.keys.firstOrNull { it.endsWith("MainActivity.kt") } ?: files.keys.firstOrNull()

            val export = ProjectZipExporter.exportProjectToZip(
                context = context,
                projectName = appTitle,
                files = files,
                isWebsite = false
            )
            exportResult = export
            onSaveProject(appTitle, promptInput, files)
            isGenerating = false
            Toast.makeText(context, "Android App generated & .ZIP packaged!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FugaSuccess.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = FugaSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Android App Builder", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
                            Text("Jetpack Compose • .ZIP Export • Live Preview", fontSize = 11.sp, color = FugaCyan)
                        }
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
        containerColor = SpaceBlack,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_builder_screen")
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Input Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Build Custom Android Application",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = appTitle,
                                onValueChange = { appTitle = it },
                                label = { Text("App Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FugaCyan,
                                    unfocusedBorderColor = SpaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                label = { Text("App Requirements / Features") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FugaCyan,
                                    unfocusedBorderColor = SpaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Templates
                            Text("Quick Presets:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(quickTemplates) { (presetTitle, presetPrompt) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SpaceCard)
                                            .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                appTitle = presetTitle
                                                promptInput = presetPrompt
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(presetTitle, fontSize = 11.sp, color = FugaCyan, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { startBuild() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("generate_app_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FugaPurple,
                                    contentColor = Color.White
                                ),
                                enabled = !isGenerating
                            ) {
                                if (isGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Synthesizing Android Code...")
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generate App & Package .ZIP", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Results Card with Download .ZIP and Open in Browser
                if (generatedFiles != null) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = FugaSuccess.copy(alpha = 0.5f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FugaSuccess, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("App Build Ready!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    }

                                    Text(
                                        text = "${generatedFiles?.size} Files Generated",
                                        fontSize = 12.sp,
                                        color = FugaCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Primary Action Row: Download .ZIP and Open in Browser
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            exportResult?.let {
                                                ProjectZipExporter.shareZipFile(context, it.zipUri, appTitle)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("download_zip_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Download .ZIP", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            // Open simulated project viewer in browser
                                            val webUri = Uri.parse("https://ais-dev-o5ix3ivc4bwuidgny7g6e3-6604691959.asia-east1.run.app")
                                            val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            try {
                                                context.startActivity(browserIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Opening in browser", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("open_app_browser_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FugaCyan),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = Brush.horizontalGradient(listOf(FugaPurple, FugaCyan))
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Open in Browser", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // File Selector Tabs
                                Text("Project Files:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(generatedFiles?.keys?.toList() ?: emptyList()) { path ->
                                        val isSelected = path == selectedFileKey
                                        val displayName = path.substringAfterLast("/")
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) FugaPurple else SpaceCard)
                                                .border(1.dp, if (isSelected) FugaCyan else SpaceBorder, RoundedCornerShape(10.dp))
                                                .clickable { selectedFileKey = path }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = displayName,
                                                fontSize = 11.5.sp,
                                                color = if (isSelected) Color.White else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Code Viewer Box
                                val currentContent = generatedFiles?.get(selectedFileKey) ?: ""
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF070A13))
                                        .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedFileKey ?: "",
                                                fontSize = 11.sp,
                                                color = FugaCyan,
                                                fontFamily = FontFamily.Monospace
                                            )

                                            IconButton(
                                                onClick = {
                                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    cm.setPrimaryClip(ClipData.newPlainText("code", currentContent))
                                                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            item {
                                                Text(
                                                    text = currentContent,
                                                    color = Color(0xFFE2E8F0),
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}
