package com.hindu.pooja.feature.elearning.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hindu.pooja.feature.elearning.*
import com.hindu.pooja.util.TtsHelper

@Composable
fun FlipCardScreen(
    initialLessonIndex: Int = 0,
    initialLang: String? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val module = remember { FlipCardRepository.loadModule(context) }

    val availableLangs = remember(module) { supportedLanguages(module) }
    var selectedLang by rememberSaveableState(availableLangs.firstOrNull() ?: (initialLang ?: "en"))
    var selectedLessonIndex by rememberSaveable { mutableStateOf(initialLessonIndex.coerceIn(0, module.lessons.lastIndex)) }
    val selectedLesson = module.lessons.getOrNull(selectedLessonIndex) ?: module.lessons.first()
    val lessonTitle = titleFor(selectedLesson.title, selectedLang)

    // TTS
    val tts = remember { TtsHelper(context) }
    LaunchedEffect(selectedLang) { tts.applyLanguage(selectedLang) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    BackHandler(enabled = onBack != null) { onBack?.invoke() }

    Scaffold(
        topBar = {
            // ---- Custom stable app bar (no experimental API) ----
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
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                    Text(
                        text = "Bala Kanda — $lessonTitle",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        maxLines = 1
                    )
                    LanguageToggle(
                        languages = availableLangs,
                        selected = selectedLang,
                        onSelect = { selectedLang = it }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LessonPickerRow(
                lessons = module.lessons,
                selectedIndex = selectedLessonIndex,
                onChange = { selectedLessonIndex = it }
            )

            val cards = remember(selectedLesson, selectedLang) { cardsFor(selectedLesson, selectedLang) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(cards, key = { idx, _ -> "$selectedLessonIndex-$selectedLang-$idx" }) { _, card ->
                    FlipCardItem(
                        frontTitle = card.front.title,
                        frontHint = card.front.hint,
                        backText = card.back,
                        onSpeak = { tts.speak(card.back) },
                        onStopSpeak = { tts.stop() }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageToggle(
    languages: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        languages.forEach { code ->
            FilterChip(
                selected = (code == selected),
                onClick = { onSelect(code) },
                label = { Text(languageDisplayName(code)) },
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}

/**
 * Stable lesson picker (no experimental ExposedDropdown APIs).
 * Uses a FilledTonalButton to open a regular DropdownMenu.
 */
@Composable
private fun LessonPickerRow(
    lessons: List<Lesson>,
    selectedIndex: Int,
    onChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedTitle = titleFor(lessons[selectedIndex].title, "en")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Lesson:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))

        Box {
            FilledTonalButton(onClick = { expanded = true }) {
                Text(
                    selectedTitle,
                    modifier = Modifier.padding(end = 6.dp),
                    maxLines = 1
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                lessons.forEachIndexed { index, lesson ->
                    DropdownMenuItem(
                        text = { Text(titleFor(lesson.title, "en")) },
                        onClick = {
                            onChange(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FlipCardItem(
    frontTitle: String,
    frontHint: String,
    backText: String,
    onSpeak: () -> Unit,
    onStopSpeak: () -> Unit
) {
    var isFront by remember { mutableStateOf(true) }
    val rotation by animateFloatAsState(targetValue = if (isFront) 0f else 180f, label = "flip")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isFront = !isFront }
    ) {
        // Front
        Column(
            modifier = Modifier
                .padding(16.dp)
                .alpha(if (rotation <= 90f) 1f else 0f)
                .rotate(rotation)
        ) {
            Text(frontTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (frontHint.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(frontHint, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            AssistChipsRow(onSpeak = onSpeak, onStopSpeak = onStopSpeak, isBack = false)
        }

        // Back
        Column(
            modifier = Modifier
                .padding(16.dp)
                .alpha(if (rotation > 90f) 1f else 0f)
                .rotate(rotation - 180f)
        ) {
            Text(backText, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            AssistChipsRow(onSpeak = onSpeak, onStopSpeak = onStopSpeak, isBack = true)
        }
    }
}

@Composable
private fun AssistChipsRow(
    onSpeak: () -> Unit,
    onStopSpeak: () -> Unit,
    isBack: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { if (isBack) onSpeak() else {} },
            label = { Text(if (isBack) "Speak" else "Flip") }
        )
        AssistChip(
            onClick = onStopSpeak,
            label = { Text("Stop") }
        )
    }
}

/* ----------------------------
   Small helpers for saveables
   ---------------------------- */
@Composable
private fun <T> rememberSaveableState(initial: T): MutableState<T> {
    return rememberSaveable { mutableStateOf(initial) }
}

/* ----------------------------
   Language display name helper
   ---------------------------- */
private fun languageDisplayName(code: String): String = when (code.lowercase()) {
    "en" -> "English"
    "te" -> "తెలుగు"
    "hi" -> "हिन्दी"
    "ta" -> "தமிழ்"
    else -> code.uppercase()
}
