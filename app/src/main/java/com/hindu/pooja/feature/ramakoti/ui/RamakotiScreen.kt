@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hindu.pooja.feature.ramakoti.ui

import android.media.MediaPlayer
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.hindu.pooja.R
import com.hindu.pooja.RamakotiAudioKeyBus
import com.hindu.pooja.VolumeEvent
import com.hindu.pooja.model.ramakoti.CellState
import com.hindu.pooja.viewmodel.RamakotiViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

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

    val context = LocalContext.current

    // 🔊 Bell sound
    val playBell: () -> Unit = remember {
        {
            try {
                val mp = MediaPlayer.create(context, R.raw.bell_ram)
                mp?.setOnCompletionListener { it.release() }
                mp?.start()
            } catch (_: Exception) { /* ignore */ }
        }
    }

    // 🔉 Volume key hooks
    LaunchedEffect(audioMode) { RamakotiAudioKeyBus.setActive(audioMode) }
    LaunchedEffect(Unit) {
        RamakotiAudioKeyBus.events.collect { event ->
            if (audioMode) {
                when (event) {
                    VolumeEvent.UP -> {
                        playBell()
                        viewModel.fillNextCellWithMantra()
                    }
                    VolumeEvent.DOWN -> viewModel.undoLastFillNoop()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Ramakoti — Writer",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Clip
                        )
                        Spacer(Modifier.height(4.dp))
                        LanguageChipRow(language = language, onChange = viewModel::switchLanguage)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { viewModel.setAudioMode(!audioMode) },
                        leadingIcon = {
                            Icon(
                                if (audioMode) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null
                            )
                        },
                        label = { Text(if (audioMode) "Audio" else "Tap") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (audioMode)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
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
                        onClick = {
                            playBell()
                            viewModel.fillNextCellWithMantra()
                        },
                        enabled = completed < 108, // VM will roll to next batch when 108 reached
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(buttonText) }
                }
            }
        }
    ) { pad ->
        // Use a Box so the celebration overlays everything
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                ProgressBar108(progress = completed, total = total)
                Spacer(Modifier.height(8.dp))
                WriterGridReadOnly(cells = cells, language = language)
            }

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
        listOf("en" to "EN", "hi" to "HI", "te" to "TE").forEach { (code, label) ->
            AssistChip(
                onClick = { onChange(code) },
                label = { Text(label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor =
                        if (language == code)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
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

/* ----------------- Celebration with GIF + falling petals ----------------- */

@Composable
private fun CelebrationOverlay(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3500)
        visible = false
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(800)),
        exit = fadeOut(animationSpec = tween(800))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.drawable.sri_ram) // drawable/sri_ram.gif
                    .decoderFactory(
                        if (Build.VERSION.SDK_INT >= 28)
                            ImageDecoderDecoder.Factory()
                        else
                            GifDecoder.Factory()
                    )
                    .build(),
                contentDescription = "Sri Ram Celebration",
                modifier = Modifier.size(300.dp)
            )

            FlowerShower()
        }
    }
}

/* ------------------ Flower animation ------------------ */

@Composable
private fun FlowerShower(
    petalCount: Int = 25,
    durationMillis: Int = 4000
) {
    val randomSeeds = remember { List(petalCount) { Random(it) } }

    val anim = rememberInfiniteTransition(label = "flower")
    val fallProgress by anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = "fall"
    )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (i in 0 until petalCount) {
            val rnd = randomSeeds[i]
            val x = rnd.nextFloat() * w
            val y = (fallProgress * h + rnd.nextFloat() * h) % h
            val color = listOf(Color.Magenta, Color.Red, Color.Yellow, Color.Cyan).random(rnd)
            drawCircle(color = color, radius = 8f, center = Offset(x, y))
        }
    }
}
