package com.hindu.pooja.ui.kids.findit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import com.hindu.pooja.R
import com.hindu.pooja.ui.kids.findit.model.HiddenObjectsLevel
import com.hindu.pooja.util.AudioPlayer
import com.hindu.pooja.util.rememberSafePainter
import kotlinx.coroutines.launch

@Composable
fun FindItGameScreen(
    navController: NavController,
    levelFile: String,
    viewModel: FindItGameViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var levelData by remember { mutableStateOf<HiddenObjectsLevel?>(null) }
    val timeLeft by viewModel.timeRemaining.collectAsState()
    val foundObjects by viewModel.foundObjects.collectAsState()

    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    // Load level data
    LaunchedEffect(levelFile) {
        levelData = FindItLevelLoader.loadLevel(context, levelFile)
        levelData?.let {
            viewModel.startGame(it.timeLimitSeconds)
            AudioPlayer.playBackground(context, R.raw.temple_bg_music)
        }
    }

    DisposableEffect(Unit) {
        onDispose { AudioPlayer.stopBackground() }
    }

    levelData?.let { level ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // XP Bar
            XpBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                currentXp = viewModel.currentXp.collectAsState().value,
                maxXp = viewModel.levelTargetXp
            )

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("⏳ Time: $timeLeft sec", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "🔍 Found: ${foundObjects.size}/${level.objects.size}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Object names with wrapping
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                level.objects.forEach { obj ->
                    val found = viewModel.isObjectFound(obj.name)
                    Text(
                        text = if (found) "✅ ${obj.name}" else obj.name,
                        color = if (found) Color(0xFF2E7D32) else Color.Black,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Background image with tap detection and coordinate mapping
            @Suppress("BoxWithConstraintsScope")
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(level) {
                        detectTapGestures { offset ->
                            val actualWidth = imageSize.width
                            val actualHeight = imageSize.height

                            val scaleX = actualWidth / 768f
                            val scaleY = actualHeight / 768f

                            level.objects.forEach { obj ->
                                if (!viewModel.isObjectFound(obj.name)) {
                                    val expectedX = obj.x * scaleX
                                    val expectedY = obj.y * scaleY

                                    val distance = Offset(offset.x, offset.y)
                                        .minus(Offset(expectedX, expectedY))
                                        .getDistance()

                                    if (distance < 60f) {
                                        viewModel.markObjectFound(obj.name)
                                        AudioPlayer.playSoundEffect(context, R.raw.bell_chime)

                                        if (viewModel.allObjectsFound(level.objects)) {
                                            scope.launch {
                                                viewModel.finishGame(level.title)
                                                navController.navigate("game_result/${level.title}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                // Map simple keywords to drawable names; otherwise use the JSON value as-is.
                val mappedName = when (level.sceneImage.lowercase()) {
                    "shiva" -> "shiva"
                    "ganesha" -> "datta"
                    "temple" -> "default_pooja_image"
                    else -> level.sceneImage // supports filenames like "sankasti_ganapathi.png" or webp/jpg
                }

                Image(
                    painter = rememberSafePainter(mappedName),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { imageSize = it.size }
                )
            }
        }
    }
}
