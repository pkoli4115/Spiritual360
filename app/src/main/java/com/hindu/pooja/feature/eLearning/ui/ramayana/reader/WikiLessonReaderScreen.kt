package com.hindu.pooja.ui.ramayana.reader.WikiReaderScreen

import com.hindu.pooja.ui.ramayana.reader.Lesson
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.runtime.mutableIntStateOf

@Composable
fun WikiReaderScreen(
    lessons: List<Lesson>,
    initialIndex: Int,
    ttsHelper: TtsHelper,
    title: String,
    languageCode: String? = null,
    onBack: () -> Unit,
    onLastPage: () -> Unit = {} // non-nullable default no-op
) {
    val saffron = Color(0xFFFF9933)
    val cream = Color(0xFFFFF3E3)
    val textColor = Color(0xFF2B1E0A)

    val safe = remember(lessons) { lessons.ifEmpty { emptyList() } }
    var index by rememberSaveable(safe) {
        mutableIntStateOf(initialIndex.coerceIn(0, safe.lastIndex.coerceAtLeast(0)))
    }
    val curr = safe.getOrNull(index)

    // TTS
    var isSpeaking by remember { mutableStateOf(false) }
    LaunchedEffect(languageCode) { languageCode?.let { runCatching { ttsHelper.applyLanguage(it) } } }
    LaunchedEffect(index) { runCatching { ttsHelper.stop() }; isSpeaking = false }
    DisposableEffect(Unit) { onDispose { runCatching { ttsHelper.stop() } } }

    // Scroll to top on page change
    val scrollState = rememberScrollState()
    LaunchedEffect(index) { scrollState.scrollTo(0) }

    // Back button (system): delegate to onBack
    BackHandler {
        runCatching { ttsHelper.stop() }; isSpeaking = false
        onBack()
    }

    // Softer swipe
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 20.dp.toPx() }  // smaller = easier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(saffron)
            .pointerInput(index, safe.size) {
                var totalDx = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDx = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDx += dragAmount },
                    onDragEnd = {
                        when {
                            // Swipe right: previous page
                            totalDx > swipeThresholdPx && index > 0 -> {
                                runCatching { ttsHelper.stop() }
                                isSpeaking = false
                                index--
                            }
                            // Swipe left: next page (not last)
                            totalDx < -swipeThresholdPx && index < safe.lastIndex -> {
                                runCatching { ttsHelper.stop() }
                                isSpeaking = false
                                index++
                            }
                            // Swipe left on LAST page → trigger quiz
                            totalDx < -swipeThresholdPx && index == safe.lastIndex -> {
                                runCatching { ttsHelper.stop() }
                                isSpeaking = false
                                onLastPage()
                            }
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        runCatching { ttsHelper.stop() }; isSpeaking = false
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontSize = 18.sp
                            ),
                            maxLines = 2
                        )
                        Text(
                            text = if (safe.isNotEmpty())
                                "Page ${index + 1} / ${safe.size}"
                            else
                                "",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                }

                // TTS + next
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (curr == null) return@IconButton
                        if (isSpeaking) {
                            runCatching { ttsHelper.stop() }
                            isSpeaking = false
                        } else {
                            val text = stripOnlySequentialPageNumbers(curr.content)
                            runCatching { ttsHelper.speak(text) }
                            isSpeaking = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = "Toggle TTS",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        runCatching { ttsHelper.stop() }
                        isSpeaking = false
                        if (index < safe.lastIndex) {
                            index++
                        } else {
                            onLastPage()
                        }
                    }) {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White
                        )
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

        if (safe.isEmpty()) {
            Text(
                text = "No lessons found",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

/* ------------------ TTS sanitizer: strip only our auto numbers ------------------ */
/* We only remove auto-added ASCII sequential prefixes like "1. ", "2) ", ...      */
/* Telugu digits (౦–౯) and non-sequential numbers are left intact for TTS.         */

private fun parseLeadingAsciiNumber(line: String): Pair<Int, String>? {
    val m = Regex("^\\s*([0-9]+)[\\.)\\-:]?\\s+(.*)$").matchEntire(line) ?: return null
    val num = m.groupValues[1].toIntOrNull() ?: return null
    val rest = m.groupValues[2]
    return num to rest
}

private fun stripOnlySequentialPageNumbers(text: String): String {
    val lines = text.lines()
    if (lines.isEmpty()) return text

    val first = parseLeadingAsciiNumber(lines.firstOrNull().orEmpty()) ?: return text
    val second = lines.drop(1).firstNotNullOfOrNull { parseLeadingAsciiNumber(it) } ?: return text
    if (second.first != first.first + 1) return text

    var expected = first.first
    val out = ArrayList<String>(lines.size)
    for (line in lines) {
        val p = parseLeadingAsciiNumber(line)
        if (p != null && p.first == expected) {
            out += p.second
            expected += 1
        } else {
            out += line
        }
    }
    return out.joinToString("\n")
}
