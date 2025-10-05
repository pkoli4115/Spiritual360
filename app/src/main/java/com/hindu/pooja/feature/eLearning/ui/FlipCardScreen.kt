package com.hindu.pooja.feature.elearning.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hindu.pooja.feature.elearning.*
import com.hindu.pooja.util.TtsHelper

/* ----------------------------
   Simple, book-like reader UI
   ---------------------------- */

@Composable
fun FlipCardScreen(
    initialLessonIndex: Int = 0,
    initialLang: String? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val module = remember { FlipCardRepository.loadModule(context) }

    // Colors
    val saffron = Color(0xFFFF9933)     // screen background
    val pageBg = Color(0xFFFFF3E3)      // light cream page
    val pageText = Color(0xFF2B1E0A)    // dark brown text (always readable)
    val pageTextMuted = Color(0xFF6F5438)

    val allLangs = remember(module) { supportedLanguages(module) }
    var lang by rememberSaveableState(allLangs.firstOrNull() ?: (initialLang ?: "en"))

    var lessonIndex by rememberSaveable {
        mutableStateOf(initialLessonIndex.coerceIn(0, module.lessons.lastIndex))
    }
    var pageIndex by rememberSaveable { mutableStateOf(0) }

    val lesson = module.lessons[lessonIndex]
    val lessonTitle = titleFor(lesson.title, lang)
    val pages = remember(lesson, lang) { cardsFor(lesson, lang) }

    // Reset page on lesson/lang change
    LaunchedEffect(lessonIndex, lang) { pageIndex = 0 }

    // TTS
    val tts = remember { TtsHelper(context) }
    LaunchedEffect(lang) { tts.applyLanguage(lang) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    BackHandler(enabled = onBack != null) { onBack?.invoke() }

    val lessonText = remember(pages) { pages.joinToString("\n\n") { it.back } }

    Scaffold(
        containerColor = saffron,
        topBar = {
            PlainTopBar(
                title = "Bala Kanda",
                onBack = onBack,
                actions = {
                    AssistChip(onClick = { lang = "en" }, label = { Text("EN") }, colors = chipColors(lang == "en"))
                    AssistChip(onClick = { lang = "te" }, label = { Text("TE") }, colors = chipColors(lang == "te"))
                    AssistChip(onClick = { lang = "hi" }, label = { Text("HI") }, colors = chipColors(lang == "hi"))
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (lessonText.isNotBlank()) tts.speak(lessonText)
                        else Toast.makeText(context, "Nothing to read", Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Filled.VolumeUp, contentDescription = "Read") }
                    IconButton(onClick = { tts.stop() }) { Icon(Icons.Filled.Stop, contentDescription = "Stop") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            // PAGE (Surface sets its own content color, but we still set Text colors explicitly)
            Surface(
                color = pageBg,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        tts.stop() // stop narration when turning page
                        val lastPage = pages.lastIndex
                        if (pageIndex < lastPage) {
                            pageIndex += 1
                        } else {
                            // next lesson
                            val lastLesson = module.lessons.lastIndex
                            lessonIndex = if (lessonIndex < lastLesson) lessonIndex + 1 else 0
                            pageIndex = 0
                        }
                    }
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Crossfade(
                        targetState = Pair(lessonIndex, pageIndex),
                        modifier = Modifier.fillMaxSize(),
                        label = "page-crossfade"
                    ) { (_, idx) ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                // Lesson title
                                Text(
                                    text = lessonTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = pageText,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))

                                // Optional tag (Summary / Key Point / Story)
                                val tag = pages.getOrNull(idx)?.front?.title
                                if (!tag.isNullOrBlank()) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = pageTextMuted
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }

                                // Body
                                val body = pages.getOrNull(idx)?.back.orEmpty()
                                SelectionContainer {
                                    Text(
                                        text = body,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = pageText
                                    )
                                }
                            }

                            // Footer: language + page position
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = languageDisplayName(lang),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = pageTextMuted
                                )
                                Text(
                                    text = "${idx + 1} / ${pages.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = pageTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Lesson navigation (no dropdown)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        tts.stop()
                        lessonIndex = if (lessonIndex > 0) lessonIndex - 1 else module.lessons.lastIndex
                        pageIndex = 0
                    }
                ) { Text("Previous Lesson") }

                TextButton(
                    onClick = {
                        tts.stop()
                        lessonIndex = if (lessonIndex < module.lessons.lastIndex) lessonIndex + 1 else 0
                        pageIndex = 0
                    }
                ) { Text("Next Lesson") }
            }
        }
    }
}

/* ----------------------------
   Stable custom top bar
   ---------------------------- */
@Composable
private fun PlainTopBar(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

/* ----------------------------
   Small helpers
   ---------------------------- */
@Composable
private fun chipColors(selected: Boolean) =
    AssistChipDefaults.assistChipColors(
        containerColor = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        labelColor = if (selected)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant
    )

@Composable
private fun <T> rememberSaveableState(initial: T): MutableState<T> {
    return rememberSaveable { mutableStateOf(initial) }
}
