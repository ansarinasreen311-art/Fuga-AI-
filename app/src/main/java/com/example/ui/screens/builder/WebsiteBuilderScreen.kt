package com.example.ui.screens.builder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
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
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaSuccess
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteBuilderScreen(
    onGenerateWebsite: suspend (String, String) -> Map<String, String>,
    onSaveProject: (String, String, Map<String, String>) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var siteTitle by remember { mutableStateOf("Futuristic SaaS Studio") }
    var promptInput by remember { mutableStateOf("Build a high-conversion cybernetic SaaS landing page with animated gradients, glassmorphism dashboard, and interactive feature cards.") }
    var isGenerating by remember { mutableStateOf(false) }

    var generatedFiles by remember { mutableStateOf<Map<String, String>?>(null) }
    var selectedFileKey by remember { mutableStateOf<String?>("index.html") }
    var exportResult by remember { mutableStateOf<ExportResult?>(null) }

    val presets = listOf(
        Pair("Futuristic SaaS", "Build a dark cybernetic SaaS landing page with glowing buttons and metrics counter."),
        Pair("Developer Portfolio", "Build a cyberpunk personal portfolio with project showcase cards, skills grid, and contact modal."),
        Pair("AI Startup Hub", "Build an AI tool landing page with interactive prompt demo and pricing table."),
        Pair("Cyber Cafe & Bistro", "Build a modern restaurant menu and booking webpage with responsive layout.")
    )

    fun startWebBuild() {
        if (promptInput.isBlank()) return
        isGenerating = true
        coroutineScope.launch {
            val files = onGenerateWebsite(promptInput.trim(), siteTitle.trim().ifBlank { "NextGenWeb" })
            generatedFiles = files
            selectedFileKey = "index.html"

            val export = ProjectZipExporter.exportProjectToZip(
                context = context,
                projectName = siteTitle,
                files = files,
                isWebsite = true
            )
            exportResult = export
            onSaveProject(siteTitle, promptInput, files)
            isGenerating = false
            Toast.makeText(context, "Website generated & packaged!", Toast.LENGTH_SHORT).show()
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
                                .background(FugaCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = FugaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Website Builder", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
                            Text("HTML5 • CSS3 • JS • .ZIP Export • Browser Preview", fontSize = 11.sp, color = FugaCyan)
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
            .testTag("website_builder_screen")
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
                                text = "Build Responsive Web Application",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = siteTitle,
                                onValueChange = { siteTitle = it },
                                label = { Text("Website Title") },
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
                                label = { Text("Website Concept & Design Requirements") },
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

                            // Presets
                            Text("Quick Presets:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(presets) { (title, prompt) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SpaceCard)
                                            .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                siteTitle = title
                                                promptInput = prompt
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(title, fontSize = 11.sp, color = FugaCyan, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { startWebBuild() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("generate_website_button"),
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
                                    Text("Generating Responsive Web Code...")
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Build Website & Package .ZIP", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Results Card with Open in Browser and Download .ZIP
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
                                        Text("Website Built Successfully!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    }

                                    Text(
                                        text = "${generatedFiles?.size} Web Files Ready",
                                        fontSize = 12.sp,
                                        color = FugaCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Row: Open in Browser & Download .ZIP
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            exportResult?.previewUri?.let { uri ->
                                                ProjectZipExporter.openInBrowser(context, uri)
                                            } ?: Toast.makeText(context, "Opening preview", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .height(46.dp)
                                            .testTag("open_website_browser_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Open in Browser", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SpaceBlack)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            exportResult?.let {
                                                ProjectZipExporter.shareZipFile(context, it.zipUri, siteTitle)
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("download_website_zip_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = Brush.horizontalGradient(listOf(FugaPurple, FugaCyan))
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp), tint = FugaPurple)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Download .ZIP", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // File Selector Tabs
                                Text("Web Assets:", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(generatedFiles?.keys?.toList() ?: emptyList()) { path ->
                                        val isSelected = path == selectedFileKey
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) FugaCyan else SpaceCard)
                                                .border(1.dp, if (isSelected) Color.White else SpaceBorder, RoundedCornerShape(10.dp))
                                                .clickable { selectedFileKey = path }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = path,
                                                fontSize = 11.5.sp,
                                                color = if (isSelected) SpaceBlack else TextPrimary,
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
