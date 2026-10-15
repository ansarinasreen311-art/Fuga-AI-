package com.example.builder

object CodeTemplates {

    fun getDefaultAppProject(title: String, prompt: String): Map<String, String> {
        val appName = title.ifBlank { "ModernApp" }.replace(" ", "")
        val lowerName = appName.lowercase()

        val mainActivity = """
package com.example.$lowerName

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ItemRecord(val id: Int, val title: String, val subtitle: String, val isCompleted: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    var items by remember {
        mutableStateOf(
            listOf(
                ItemRecord(1, "Launch Project", "Set up architecture and modules", true),
                ItemRecord(2, "Design UI Theme", "Apply Material Design 3 and colors", true),
                ItemRecord(3, "Implement Core Feature", "$prompt", false),
                ItemRecord(4, "Test and Deploy", "Export APK and .zip package", false)
            )
        )
    }
    var newItemText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$title", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (newItemText.isNotBlank()) {
                        items = items + ItemRecord(items.size + 1, newItemText.trim(), "Added via quick action")
                        newItemText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                label = { Text("Add new item") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Overview (${'$'}{items.count { it.isCompleted }}/${'$'}{items.size} Completed)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isCompleted) 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.subtitle,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                items = items.map {
                                    if (it.id == item.id) it.copy(isCompleted = !it.isCompleted) else it
                                }
                            }) {
                                Icon(
                                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (item.isCompleted) Color(0xFF10B981) else Color(0xFF8B5CF6)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8B5CF6),
            primaryContainer = Color(0xFF3B1F70),
            secondary = Color(0xFF06B6D4),
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B)
        ),
        content = content
    )
}
""".trimIndent()

        val manifest = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="$title"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""".trimIndent()

        val buildGradle = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.$lowerName"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.$lowerName"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
}
""".trimIndent()

        val readme = """
# $title

Generated automatically by **Fuga AI App Builder**.

## Prompt
> $prompt

## Architecture
- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Target SDK**: Android 16 (API 36)
- **Minimum SDK**: Android 7.0 (API 24)

## How to Run
1. Unzip this project.
2. Open Android Studio.
3. Click **Open Project** and select this directory.
4. Run on an Emulator or connected Android Device.
""".trimIndent()

        return mapOf(
            "app/src/main/java/com/example/$lowerName/MainActivity.kt" to mainActivity,
            "app/src/main/AndroidManifest.xml" to manifest,
            "app/build.gradle.kts" to buildGradle,
            "README.md" to readme
        )
    }

    fun getDefaultWebsiteProject(title: String, prompt: String): Map<String, String> {
        val siteTitle = title.ifBlank { "NextGen Web App" }

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$siteTitle</title>
  <link rel="stylesheet" href="styles.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap" rel="stylesheet">
</head>
<body>
  <!-- Header / Navigation -->
  <header class="navbar">
    <div class="logo">
      <span class="logo-orb"></span>
      <span class="logo-text">$siteTitle</span>
    </div>
    <nav class="nav-links">
      <a href="#features">Features</a>
      <a href="#demo">Interactive Demo</a>
      <a href="#pricing">Details</a>
    </nav>
    <button class="btn btn-primary" onclick="triggerAction()">Get Started</button>
  </header>

  <!-- Hero Section -->
  <section class="hero">
    <div class="badge">Generated by Fuga AI Web Builder</div>
    <h1 class="hero-title">Experience the Future of <span>$siteTitle</span></h1>
    <p class="hero-subtitle">$prompt</p>
    <div class="hero-actions">
      <button class="btn btn-primary" onclick="triggerAction()">Launch Experience</button>
      <button class="btn btn-secondary" onclick="toggleTheme()">Toggle Theme</button>
    </div>
  </section>

  <!-- Interactive Demo Section -->
  <section id="demo" class="interactive-card">
    <div class="card-header">
      <h2>Interactive Live Preview</h2>
      <p>Test live JavaScript functionality directly in your browser.</p>
    </div>
    <div class="demo-controls">
      <input type="text" id="itemInput" placeholder="Enter custom item or action..." class="input-field">
      <button class="btn btn-primary" onclick="addItem()">Add to Dashboard</button>
    </div>
    <ul id="itemList" class="item-list">
      <li class="item-entry"><span>⚡ High performance reactive architecture</span> <button class="btn-sm" onclick="this.parentElement.remove()">✕</button></li>
      <li class="item-entry"><span>🎨 Cybernetic glassmorphism and modern gradients</span> <button class="btn-sm" onclick="this.parentElement.remove()">✕</button></li>
      <li class="item-entry"><span>📱 100% responsive for Mobile, Tablet & Desktop</span> <button class="btn-sm" onclick="this.parentElement.remove()">✕</button></li>
    </ul>
    <div class="stats-grid">
      <div class="stat-box">
        <h3 id="statCount">3</h3>
        <p>Active Elements</p>
      </div>
      <div class="stat-box">
        <h3>99.9%</h3>
        <p>Uptime & Speed</p>
      </div>
      <div class="stat-box">
        <h3>0ms</h3>
        <p>Latency</p>
      </div>
    </div>
  </section>

  <!-- Features Section -->
  <section id="features" class="features-grid">
    <div class="feature-card">
      <div class="icon">🚀</div>
      <h3>Lightning Fast</h3>
      <p>Built with zero bloated dependencies, providing ultra-fast load times.</p>
    </div>
    <div class="feature-card">
      <div class="icon">💎</div>
      <h3>Modern UI Design</h3>
      <p>Crafted with contemporary aesthetics, rounded corners, and glowing neon accents.</p>
    </div>
    <div class="feature-card">
      <div class="icon">🔒</div>
      <h3>Privacy First</h3>
      <p>Zero tracking, fully client-side capabilities, and secure zip source download.</p>
    </div>
  </section>

  <!-- Footer -->
  <footer class="footer">
    <p>&copy; 2026 $siteTitle. Built with Fuga AI Assistant.</p>
  </footer>

  <script src="script.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
:root {
  --bg-color: #0B0F19;
  --surface-color: #121826;
  --card-bg: rgba(30, 41, 59, 0.7);
  --border-color: rgba(139, 92, 246, 0.25);
  --primary: #8B5CF6;
  --primary-hover: #7C3AED;
  --accent-cyan: #06B6D4;
  --text-main: #F8FAFC;
  --text-muted: #94A3B8;
  --radius: 16px;
}

[data-theme="light"] {
  --bg-color: #F8FAFC;
  --surface-color: #FFFFFF;
  --card-bg: rgba(241, 245, 249, 0.9);
  --border-color: rgba(139, 92, 246, 0.2);
  --text-main: #0F172A;
  --text-muted: #64748B;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Plus Jakarta Sans', -apple-system, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-main);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  line-height: 1.6;
  transition: background 0.3s ease, color 0.3s ease;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: rgba(11, 15, 25, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 800;
  font-size: 1.25rem;
}

.logo-orb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-cyan), var(--primary));
  box-shadow: 0 0 12px var(--accent-cyan);
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-links a {
  color: var(--text-muted);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
  transition: color 0.2s;
}

.nav-links a:hover {
  color: var(--text-main);
}

.btn {
  padding: 10px 22px;
  border-radius: 24px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary), var(--accent-cyan));
  color: #FFFFFF;
  box-shadow: 0 4px 14px rgba(139, 92, 246, 0.35);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(6, 182, 212, 0.45);
}

.btn-secondary {
  background: transparent;
  color: var(--text-main);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: rgba(139, 92, 246, 0.15);
}

.hero {
  text-align: center;
  padding: 80px 20px 50px;
  max-width: 900px;
  margin: 0 auto;
}

.badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(139, 92, 246, 0.15);
  border: 1px solid var(--primary);
  border-radius: 20px;
  color: var(--accent-cyan);
  font-size: 0.85rem;
  font-weight: 700;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 20px;
}

.hero-title span {
  background: linear-gradient(135deg, var(--primary), var(--accent-cyan));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 1.2rem;
  color: var(--text-muted);
  margin-bottom: 30px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.interactive-card {
  max-width: 750px;
  margin: 30px auto;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
}

.demo-controls {
  display: flex;
  gap: 12px;
  margin: 20px 0;
}

.input-field {
  flex: 1;
  padding: 12px 18px;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  color: var(--text-main);
  font-size: 1rem;
  outline: none;
}

.input-field:focus {
  border-color: var(--accent-cyan);
}

.item-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  border-left: 3px solid var(--accent-cyan);
}

.btn-sm {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 1rem;
}

.btn-sm:hover {
  color: #EF4444;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
  text-align: center;
}

.stat-box h3 {
  font-size: 1.8rem;
  color: var(--accent-cyan);
}

.stat-box p {
  color: var(--text-muted);
  font-size: 0.85rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  max-width: 1000px;
  margin: 40px auto;
  padding: 0 20px;
}

.feature-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  padding: 26px;
  transition: transform 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-card .icon {
  font-size: 2rem;
  margin-bottom: 12px;
}

.footer {
  text-align: center;
  padding: 40px;
  margin-top: auto;
  color: var(--text-muted);
  border-top: 1px solid var(--border-color);
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .navbar { padding: 16px 20px; }
  .nav-links { display: none; }
  .hero-title { font-size: 2.2rem; }
  .demo-controls { flex-direction: column; }
}
""".trimIndent()

        val js = """
function triggerAction() {
  alert("🎉 Experience launched! Generated with Fuga AI Assistant.");
}

function toggleTheme() {
  const current = document.documentElement.getAttribute("data-theme");
  const next = current === "light" ? "dark" : "light";
  document.documentElement.setAttribute("data-theme", next);
}

function addItem() {
  const input = document.getElementById("itemInput");
  const text = input.value.trim();
  if (!text) return;

  const list = document.getElementById("itemList");
  const li = document.createElement("li");
  li.className = "item-entry";
  li.innerHTML = `<span>✨ ` + text + `</span> <button class="btn-sm" onclick="this.parentElement.remove(); updateStats()">✕</button>`;
  list.appendChild(li);

  input.value = "";
  updateStats();
}

function updateStats() {
  const count = document.getElementById("itemList").children.length;
  document.getElementById("statCount").innerText = count;
}
""".trimIndent()

        val readme = """
# $siteTitle

Generated automatically by **Fuga AI Web Builder**.

## Prompt
> $prompt

## Files Included
- `index.html` - Responsive HTML5 layout
- `styles.css` - Futuristic styling, glassmorphism, responsive media queries
- `script.js` - Interactive event handling and local state

## How to Run in Browser
1. Extract `.zip` into any directory.
2. Double-click `index.html` or open in Chrome, Firefox, Safari, or Edge.
""".trimIndent()

        return mapOf(
            "index.html" to html,
            "styles.css" to css,
            "script.js" to js,
            "README.md" to readme
        )
    }
}
