@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hindu.pooja.feature.ramakoti.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hindu.pooja.VolumeEvent
import com.hindu.pooja.RamakotiAudioKeyBus
import com.hindu.pooja.model.ramakoti.CellState
import com.hindu.pooja.viewmodel.RamakotiViewModel

@Composable
fun RamakotiScreen(
    navController: NavController,
    viewModel: RamakotiViewModel = hiltViewModel(),
    initialLanguage: String = "en"
) {
    // initialize once
    LaunchedEffect(Unit) { viewModel.init(initialLanguage) }

    val cells by viewModel.cells.collectAsState()
    val language by viewModel.language.collectAsState()
    val completed by viewModel.completed.collectAsState()
    val total by viewModel.totalCount.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val audioMode by viewModel.audioMode.collectAsState()

    // Hook volume keys when audioMode = true
    LaunchedEffect(audioMode) { RamakotiAudioKeyBus.setActive(audioMode) }
    LaunchedEffect(Unit) {
        RamakotiAudioKeyBus.events.collect { event ->
            if (audioMode) {
                when (event) {
                    VolumeEvent.UP -> viewModel.fillNextCellWithMantra()
                    VolumeEvent.DOWN -> viewModel.undoLastFillNoop()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        "Ramakoti — Writer",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LanguageChipRow(language = language, onChange = viewModel::switchLanguage)
                        Spacer(Modifier.width(8.dp))
                        AssistChip(
                            onClick = { viewModel.setAudioMode(!audioMode) },
                            leadingIcon = {
                                Icon(
                                    if (audioMode) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = null
                                )
                            },
                            label = { Text(if (audioMode) "Audio" else "Tap") }
                        )
                    }
                }
            )
        },
        bottomBar = {
            val buttonText = when (language.lowercase()) {
                "hi" -> "जय श्री राम"
                "te" -> "జై శ్రీ రామ్"
                else -> "Jai Shri Ram"
            }
            Surface(tonalElevation = 2.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.fillNextCellWithMantra() },
                        // Disable after 108; VM won’t create a new batch automatically
                        enabled = completed < 108,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(buttonText) }
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            ProgressBar108(progress = completed, total = total)
            Spacer(Modifier.height(8.dp))
            WriterGridReadOnly(cells = cells, language = language)

            if (showCelebration) {
                CelebrationOverlay(onDismiss = { viewModel.dismissCelebration() })
            }
        }
    }
}

/* ---------- helpers ---------- */

@Composable
private fun LanguageChipRow(language: String, onChange: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { onChange("en") },
            label = { Text("EN") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (language == "en") MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        )
        AssistChip(
            onClick = { onChange("hi") },
            label = { Text("HI") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (language == "hi") MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        )
        AssistChip(
            onClick = { onChange("te") },
            label = { Text("TE") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (language == "te") MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun ProgressBar108(progress: Int, total: Int) {
    Column(Modifier.fillMaxWidth()) {
        Text("Progress: $progress / 108", style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(
            progress = progress.coerceIn(0, 108) / 108f,
            modifier = Modifier.fillMaxWidth()
        )
        if (total > 0) {
            Spacer(Modifier.height(2.dp))
            Text("Lifetime count: $total", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun WriterGridReadOnly(cells: List<CellState>, language: String) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(9),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 64.dp)
    ) {
        items(cells, key = { it.index }) { cell ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        RoundedCornerShape(8.dp)
                    )
                    .background(
                        if (cell.filled) saffron() else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                if (cell.filled) {
                    CellLocked(display = cell.displayText)
                } else {
                    Text(
                        text = ghostHint(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CellLocked(display: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF7A4F00))
        Spacer(Modifier.width(4.dp))
        Text(text = display, style = MaterialTheme.typography.labelMedium)
    }
}

private fun saffron(): Color = Color(0xFFFFE6B3)
private fun ghostHint(lang: String): String = when (lang.lowercase()) {
    "hi" -> "जय श्री राम"
    "te" -> "జై శ్రీ రామ్"
    else -> "Jai Shri Ram"
}

@Composable
private fun CelebrationOverlay(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1800)
        visible = false
        onDismiss()
    }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x99FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("జై శ్రీరామ్!", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("108 పూర్తయ్యాయి ✨")
            }
        }
    }
}
