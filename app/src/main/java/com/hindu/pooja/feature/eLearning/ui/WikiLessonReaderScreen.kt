package com.hindu.pooja.feature.elearning.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hindu.pooja.util.TtsHelper

@Composable
fun WikiReaderScreen(
    lessons: List<Lesson>,
    initialIndex: Int,
    ttsHelper: TtsHelper,
    title: String,
    languageCode: String? = null,
    onBack: () -> Unit,
    onLastPage: (() -> Unit)? = null
) {
    val saffron = Color(0xFFFF9933)
    val cream = Color(0xFFFFF3E3)
    val textColor = Color(0xFF2B1E0A)

    val safe = remember(lessons) { lessons.ifEmpty { emptyList() } }
    var index by remember(safe) { mutableIntStateOf(initialIndex.coerceIn(0, safe.lastIndex.coerceAtLeast(0))) }
    val curr = safe.getOrNull(index)

    // TTS
    var isSpeaking by remember { mutableStateOf(false) }
    LaunchedEffect(languageCode) { languageCode?.let { runCatching { ttsHelper.applyLanguage(it) } } }
    LaunchedEffect(index) { runCatching { ttsHelper.stop() }; isSpeaking = false }
    DisposableEffect(Unit) { onDispose { runCatching { ttsHelper.stop() } } }

    // Scroll to top on page change
    val scrollState = rememberScrollState()
    LaunchedEffect(index) { scrollState.scrollTo(0) }

    // Back button (system): delegate to onBack (your NavHost currently sends the user home)
    BackHandler {
        runCatching { ttsHelper.stop() }; isSpeaking = false
        onBack()
    }

    // ▶ Softer swipe: accumulate horizontal distance; small dp threshold (20.dp)
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 20.dp.toPx() }  // smaller = easier
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(saffron)
            // before: .pointerInput(index) { ... }
            .pointerInput(index, safe.size) {
                var totalDx = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDx = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDx += dragAmount },
                    onDragEnd = {
                        when {
                            totalDx > swipeThresholdPx && index > 0 -> {
                                runCatching { ttsHelper.stop() }; isSpeaking = false; index--
                            }
                            totalDx < -swipeThresholdPx && index < safe.lastIndex -> {
                                runCatching { ttsHelper.stop() }; isSpeaking = false; index++
                            }
                            totalDx < -swipeThresholdPx && index == safe.lastIndex -> {
                                runCatching { ttsHelper.stop() }; isSpeaking = false; onLastPage?.invoke()
                            }
                        }
                    }
                )
            }

    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header (saffron)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    runCatching { ttsHelper.stop() }; isSpeaking = false
                    if (index > 0) index-- else onBack()
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )

                Row {
                    IconButton(onClick = {
                        if (isSpeaking) {
                            runCatching { ttsHelper.stop() }; isSpeaking = false
                        } else {
                            curr?.let { l ->
                                runCatching { ttsHelper.stop(); ttsHelper.speak(l.content) }
                                isSpeaking = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop" else "Read",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        runCatching { ttsHelper.stop() }; isSpeaking = false
                        if (index < safe.lastIndex) index++ else onLastPage?.invoke()
                    }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                    }
                }
            }

            // Content Card — full height with small outer padding
            Card(
                colors = CardDefaults.cardColors(containerColor = cream),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = curr?.title.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 26.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = curr?.content.orEmpty(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 22.sp,
                            lineHeight = 30.sp,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}
