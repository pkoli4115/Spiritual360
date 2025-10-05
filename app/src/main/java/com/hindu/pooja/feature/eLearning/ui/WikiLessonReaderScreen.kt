package com.hindu.pooja.feature.elearning.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hindu.pooja.feature.elearning.SimpleLesson
import com.hindu.pooja.feature.elearning.SimpleLessonRepo
import com.hindu.pooja.util.TtsHelper

@OptIn(ExperimentalMaterial3Api::class)
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

    // --- TTS with toggle ---
    val tts = remember { TtsHelper(context) }
    var isSpeaking by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { tts.applyLanguage("te") }
    // Stop & reset when lesson changes
    LaunchedEffect(index) {
        tts.stop()
        isSpeaking = false
    }
    // Cleanup
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val saffron = Color(0xFFFF9933)
    val cream = Color(0xFFFFF3E3)
    val textColor = Color(0xFF2B1E0A)

    BackHandler {
        // ensure TTS stopped when leaving
        tts.stop()
        isSpeaking = false
        onBack()
    }

    Scaffold(
        containerColor = saffron,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        tts.stop()
                        isSpeaking = false
                        onBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Text(
                        text = "బాలకాండము కథ",
                        color = Color.White,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!isSpeaking) {
                                tts.stop()
                                tts.speak(lesson.content)
                                isSpeaking = true
                            } else {
                                tts.stop()
                                isSpeaking = false
                            }
                        }
                    ) {
                        val icon = if (isSpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp
                        val label = if (isSpeaking) "Stop reading" else "Read"
                        Icon(icon, contentDescription = label, tint = Color.White)
                    }
                    if (onTakeQuiz != null) {
                        TextButton(onClick = onTakeQuiz) {
                            Text("Take Quiz", color = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = saffron)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(saffron)
                .padding(padding)
                .padding(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cream),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = lesson.title,
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = lesson.content,
                        fontSize = 22.sp,
                        lineHeight = 30.sp,
                        color = textColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (index > 0) {
                            tts.stop()
                            isSpeaking = false
                            index--
                        }
                    },
                    enabled = index > 0
                ) { Text("మునుపటి పాఠం", fontSize = 16.sp) }

                Text(
                    text = "పాఠం ${index + 1} / ${module.lessons.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = {
                        tts.stop()
                        isSpeaking = false
                        if (index < module.lessons.lastIndex) {
                            index++
                        } else {
                            onTakeQuiz?.invoke()
                        }
                    }
                ) {
                    Text(
                        if (index < module.lessons.lastIndex) "తర్వాతి పాఠం" else "Start Quiz",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
