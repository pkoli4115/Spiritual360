package com.hindu.pooja.feature.ramakoti.ui

import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.hindu.pooja.util.TtsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun RamakotiIntroScreen(
    navController: NavController,
    onNextRoute: String
) {
    val saffron = Color(0xFFFF9933)
    val cream = Color(0xFFFFF3E3)
    val textColor = Color(0xFF2B1E0A)

    val context = LocalContext.current
    var introText by remember { mutableStateOf<String?>(null) } // null = loading

    // ---- TTS with readiness guards ----
    val tts = remember { TtsHelper(context) }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        introText = withContext(Dispatchers.IO) { loadRamakotiIntroFromAssets(context) }

        // wait for engine to bind
        repeat(40) {
            if (tts.isReady()) return@repeat
            delay(50)
        }
        ttsReady = tts.isReady()
        if (ttsReady) runCatching { tts.applyLanguage("te") }
    }

    LaunchedEffect(introText) {
        if (ttsReady) runCatching { tts.stop() }
        isSpeaking = false
    }

    DisposableEffect(Unit) {
        onDispose { if (ttsReady) runCatching { tts.shutdown() } }
    }

    fun speakOrStop() {
        val txt = introText ?: return
        if (!ttsReady) return
        if (txt.trim().startsWith("⚠️")) return
        if (isSpeaking) {
            runCatching { tts.stop() }
            isSpeaking = false
        } else {
            runCatching { tts.stop(); tts.speak(txt) }
            isSpeaking = true
        }
    }

    Scaffold(
        containerColor = saffron,
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Box(Modifier.fillMaxWidth().padding(12.dp)) {
                    Button(
                        onClick = {
                            if (ttsReady) runCatching { tts.stop() }
                            isSpeaking = false
                            navController.navigate(onNextRoute)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("NEXT")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .background(saffron)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cream),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(Modifier.fillMaxSize()) {

                    // Big, always-on-top TTS button (click guarded; visually dim when disabled)
                    val fabEnabled = ttsReady && !introText.isNullOrBlank() && !introText!!.trim().startsWith("⚠️")
                    SmallFloatingActionButton(
                        onClick = { if (fabEnabled) speakOrStop() },
                        containerColor = saffron,
                        contentColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(56.dp)
                            .zIndex(1f)
                            .then(if (!fabEnabled) Modifier.alpha(0.45f) else Modifier)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop reading" else "Read aloud"
                        )
                    }

                    // Content
                    when (val txt = introText) {
                        null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = saffron)
                            }
                        }
                        else -> {
                            val scroll = rememberScrollState()
                            Text(
                                text = txt,
                                color = textColor,
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 20.sp,
                                    lineHeight = 28.sp
                                ),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                                    .verticalScroll(scroll)
                            )
                        }
                    }
                }
            }
        }
    }
}

/* -------------------------- Asset loader (same) --------------------------- */

private fun loadRamakotiIntroFromAssets(ctx: android.content.Context): String {
    val dir = "eLearning"
    val targetBase = "ramakotiintro"

    val candidates = listOf("RamakotiIntro", "RamakotiIntro.txt", "RamakotiIntro.md")
    for (cand in candidates) {
        val path = "$dir/$cand"
        if (assetExists(ctx, path)) return readAsset(ctx, path)
    }

    val files = runCatching { ctx.assets.list(dir)?.toList().orEmpty() }.getOrElse { emptyList() }
    val match = files.firstOrNull { it.lowercase(Locale.ROOT).startsWith(targetBase) }
    if (match != null) return readAsset(ctx, "$dir/$match")

    return buildString {
        appendLine("⚠️ Unable to load intro file.")
        appendLine("Place a file named \"RamakotiIntro\" (any case, optional .txt/.md) in assets/$dir/")
        appendLine()
        if (files.isNotEmpty()) {
            appendLine("Found in $dir/:")
            files.forEach { appendLine("• $it") }
        } else {
            appendLine("No files found under assets/$dir/")
        }
    }.trim()
}

private fun assetExists(ctx: android.content.Context, path: String): Boolean =
    runCatching { ctx.assets.open(path).close(); true }.getOrDefault(false)

private fun readAsset(ctx: android.content.Context, path: String): String =
    ctx.assets.open(path).use { it.readBytes().toString(Charsets.UTF_8) }.trim()
