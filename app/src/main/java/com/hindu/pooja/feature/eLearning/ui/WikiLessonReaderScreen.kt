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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hindu.pooja.feature.elearning.SimpleLesson
import com.hindu.pooja.feature.elearning.SimpleLessonRepo
import com.hindu.pooja.util.TtsHelper

@Composable
fun WikiLessonReaderScreen(
    onBack: () -> Unit,
    initialIndex: Int = 0,
    onTakeQuiz: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val module = remember { SimpleLessonRepo.loadTeWikiSimple(context) }

    var index by remember { mutableIntStateOf(initialIndex.coerceIn(0, module.lessons.lastIndex)) }
    val lesson: SimpleLesson = module.lessons[index]

    // TTS
    val tts = remember { TtsHelper(context) }
    var isSpeaking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { tts.applyLanguage("te") }
    LaunchedEffect(index) { tts.stop(); isSpeaking = false }

    // Ensure new page starts scrolled to top
    val scrollState = rememberScrollState()
    LaunchedEffect(index) { scrollState.scrollTo(0) }

    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val saffron = Color(0xFFFF9933)
    val cream = Color(0xFFFFF3E3)
    val textColor = Color(0xFF2B1E0A)

    // Back: go to previous lesson if possible, else exit
    BackHandler {
        if (index > 0) {
            tts.stop(); isSpeaking = false
            index--
        } else {
            tts.stop(); isSpeaking = false
            onBack()
        }
    }

    // --- Softer swipe: accumulate gesture distance & use a DP threshold ---
    val density = LocalDensity.current
    // lower this (e.g., 28.dp) to make it even easier
    val swipeThresholdPx = with(density) { 36.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(saffron)
            .pointerInput(index) {
                var totalDx = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDx = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDx += dragAmount
                    },
                    onDragEnd = {
                        when {
                            totalDx > swipeThresholdPx && index > 0 -> {
                                tts.stop(); isSpeaking = false
                                index--
                            }
                            totalDx < -swipeThresholdPx && index < module.lessons.lastIndex -> {
                                tts.stop(); isSpeaking = false
                                index++
                            }
                            totalDx < -swipeThresholdPx && index == module.lessons.lastIndex -> {
                                tts.stop(); isSpeaking = false
                                onTakeQuiz?.invoke()
                            }
                        }
                    }
                )
            }
    ) {
        Column(Modifier.fillMaxSize()) {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (index > 0) {
                        tts.stop(); isSpeaking = false
                        index--
                    } else {
                        tts.stop(); isSpeaking = false
                        onBack()
                    }
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "బాలకాండము కథ",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row {
                    IconButton(onClick = {
                        if (isSpeaking) {
                            tts.stop(); isSpeaking = false
                        } else {
                            tts.stop(); tts.speak(lesson.content); isSpeaking = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = "Read / Stop",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        tts.stop(); isSpeaking = false
                        if (index < module.lessons.lastIndex) index++ else onTakeQuiz?.invoke()
                    }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                    }
                }
            }

            // CONTENT
            Card(
                colors = CardDefaults.cardColors(containerColor = cream),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 26.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = lesson.content,
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
