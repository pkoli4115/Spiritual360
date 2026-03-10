package com.hindu.pooja.ui.ramayana.ramakoti

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.feature.ramakoti.prefs.LanguagePreferenceManager
import com.hindu.pooja.util.TtsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamakotiIntroScreen(
    navController: NavController,
    onNextRoute: String
) {
    val saffron = Color(0xFFFF9933)
    val cream = Color(0xFFFFF3E3)
    val textColor = Color(0xFF2B1E0A)

    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val langMgr = remember { LanguagePreferenceManager.getInstance(context) }
    var selectedLang by remember { mutableStateOf("") }
    var introText by remember { mutableStateOf<String?>(null) }

    val tts = remember { TtsHelper(context) }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        selectedLang = langMgr.languageFlowFor(uid).first().ifBlank { "te" }

        introText = withContext(Dispatchers.IO) {
            loadRamakotiIntroFromAssets(context, selectedLang)
        }

        repeat(40) {
            if (tts.isReady()) return@repeat
            delay(50)
        }

        ttsReady = tts.isReady()
        if (ttsReady) {
            runCatching { tts.applyLanguage(selectedLang) }
        }
    }

    LaunchedEffect(introText, selectedLang) {
        if (ttsReady) {
            runCatching {
                tts.stop()
                tts.applyLanguage(selectedLang)
            }
        }
        isSpeaking = false
    }

    DisposableEffect(Unit) {
        onDispose {
            if (ttsReady) runCatching { tts.shutdown() }
        }
    }

    fun speakOrStop() {
        val txt = introText ?: return
        if (!ttsReady) return
        if (txt.trim().startsWith("⚠️")) return

        if (isSpeaking) {
            runCatching { tts.stop() }
            isSpeaking = false
        } else {
            runCatching {
                tts.stop()
                tts.speak(txt)
            }
            isSpeaking = true
        }
    }

    val titleText = when (selectedLang) {
        "hi" -> "हमारा संकल्प"
        "en" -> "Our Intention"
        else -> "మా ఆశయం"
    }

    val buttonLabel = when (selectedLang) {
        "hi" -> "रामकोटि प्रारंभ करें"
        "en" -> "Start Ramakoti"
        else -> "రామకోటి ప్రారంభించండి"
    }

    Scaffold(
        containerColor = saffron,
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (ttsReady) runCatching { tts.stop() }
                            isSpeaking = false
                            navController.navigate(onNextRoute)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(buttonLabel)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
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
                    val fabEnabled =
                        ttsReady && !introText.isNullOrBlank() && !introText!!.trim().startsWith("⚠️")

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

                    when (val txt = introText) {
                        null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = saffron)
                            }
                        }

                        else -> {

                            val scroll = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            ) {

                                // 🌍 Language selector
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    FilterChip(
                                        selected = selectedLang == "te",
                                        onClick = {
                                            selectedLang = "te"
                                            introText = loadRamakotiIntroFromAssets(context, "te")
                                            if (ttsReady) runCatching { tts.applyLanguage("te") }
                                        },
                                        label = { Text("తెలుగు") }
                                    )

                                    FilterChip(
                                        selected = selectedLang == "en",
                                        onClick = {
                                            selectedLang = "en"
                                            introText = loadRamakotiIntroFromAssets(context, "en")
                                            if (ttsReady) runCatching { tts.applyLanguage("en") }
                                        },
                                        label = { Text("English") }
                                    )

                                    FilterChip(
                                        selected = selectedLang == "hi",
                                        onClick = {
                                            selectedLang = "hi"
                                            introText = loadRamakotiIntroFromAssets(context, "hi")
                                            if (ttsReady) runCatching { tts.applyLanguage("hi") }
                                        },
                                        label = { Text("हिन्दी") }
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // 📜 Maa Asayam text
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
                                        .verticalScroll(scroll)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* -------------------------- Asset loader --------------------------- */

private fun loadRamakotiIntroFromAssets(
    ctx: android.content.Context,
    languageCode: String
): String {
    val dir = "eLearning"
    val normalizedLang = languageCode.lowercase(Locale.ROOT).ifBlank { "te" }

    val preferredFiles = listOf(
        "RamakotiIntro_${normalizedLang}.txt",
        "RamakotiIntro_${normalizedLang}.md",
        "RamakotiIntro_${normalizedLang}"
    )

    preferredFiles.forEach { name ->
        val path = "$dir/$name"
        if (assetExists(ctx, path)) return readAsset(ctx, path)
    }

    // Fallback order
    val fallbackFiles = listOf(
        "RamakotiIntro_te.txt",
        "RamakotiIntro_en.txt",
        "RamakotiIntro_hi.txt",
        "RamakotiIntro.txt",
        "RamakotiIntro"
    )

    fallbackFiles.forEach { name ->
        val path = "$dir/$name"
        if (assetExists(ctx, path)) return readAsset(ctx, path)
    }

    val files = runCatching { ctx.assets.list(dir)?.toList().orEmpty() }
        .getOrElse { emptyList() }

    return buildString {
        appendLine("⚠️ Unable to load intro file.")
        appendLine("Expected files under assets/$dir/")
        appendLine("Example: RamakotiIntro_te.txt / RamakotiIntro_en.txt / RamakotiIntro_hi.txt")
        appendLine()
        if (files.isNotEmpty()) {
            appendLine("Found in $dir/:")
            files.forEach { appendLine("• $it") }
        } else {
            appendLine("No files found under assets/$dir/")
        }
    }.trim()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntroLangChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}
private fun assetExists(ctx: android.content.Context, path: String): Boolean =
    runCatching {
        ctx.assets.open(path).close()
        true
    }.getOrDefault(false)

private fun readAsset(ctx: android.content.Context, path: String): String =
    ctx.assets.open(path).use { it.readBytes().toString(Charsets.UTF_8) }.trim()