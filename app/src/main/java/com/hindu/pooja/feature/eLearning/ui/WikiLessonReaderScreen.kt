package com.hindu.pooja.feature.elearning.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

    // TTS
    val tts = remember { TtsHelper(context) }
    LaunchedEffect(Unit) { tts.applyLanguage("te") }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val saffron = Color(0xFFFF9933)
    val pageBg = Color(0xFFFFF3E3)
    val textColor = Color(0xFF2B1E0A)

    BackHandler { onBack() }

    Scaffold(
        containerColor = saffron,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bala Kanda — ",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                        Text("పాఠం", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        tts.stop()
                        tts.speak(lesson.content)
                    }) { Text("చదవండి", color = Color.White) }

                    if (onTakeQuiz != null) {
                        TextButton(onClick = onTakeQuiz) { Text("Take Quiz", color = Color.White) }
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
                .padding(16.dp)
        ) {
            // Big “page”
            Card(
                colors = CardDefaults.cardColors(containerColor = pageBg),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = lesson.content,
                        // big, flashcard-like
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
                    onClick = { if (index > 0) { tts.stop(); index-- } },
                    enabled = index > 0
                ) { Text("మునుపటి పాఠం") }

                Text(
                    text = "పాఠం ${index + 1} / ${module.lessons.size}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = { if (index < module.lessons.lastIndex) { tts.stop(); index++ } },
                    enabled = index < module.lessons.lastIndex
                ) { Text("తర్వాతి పాఠం") }
            }
        }
    }
}
