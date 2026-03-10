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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import com.hindu.pooja.R
import com.hindu.pooja.ui.kids.findit.model.HiddenObjectsLevel
import com.hindu.pooja.util.AudioPlayer
import com.hindu.pooja.util.rememberSafePainter
import kotlinx.coroutines.launch

// BASE RESOLUTION OF ORIGINAL IMAGE (IMPORTANT)
private const val BASE_WIDTH = 1000f
private const val BASE_HEIGHT = 1000f

// Hit radius inside the image coordinate system
private const val BASE_HIT_RADIUS = 80f

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
    val currentXp by viewModel.currentXp.collectAsState()

    // Image position + size
    var imageBounds by remember { mutableStateOf<Rect?>(null) }

    // Load level JSON
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
                currentXp = currentXp,
                maxXp = viewModel.levelTargetXp
            )

            // Header: Timer + Found count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "⏳ Time: $timeLeft sec", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "🔍 Found: ${foundObjects.size}/${level.objects.size}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Object List
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                level.objects.forEach { obj ->
                    val isFound = viewModel.isObjectFound(obj.name)
                    Text(
                        text = if (isFound) "✅ ${obj.name}" else obj.name,
                        color = if (isFound) Color(0xFF2E7D32) else Color.Black,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Main Game Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(level) {
                        detectTapGestures { tapOffset ->

                            val bounds = imageBounds ?: return@detectTapGestures

                            // Convert tap location → image-local coordinates
                            val tapInImage = Offset(
                                x = tapOffset.x - bounds.left,
                                y = tapOffset.y - bounds.top
                            )

                            // SCALE FACTORS (CRITICAL FIX)
                            val scaleX = bounds.width / BASE_WIDTH
                            val scaleY = bounds.height / BASE_HEIGHT

                            // Use smaller scale for accurate radius
                            val scale = if (scaleX < scaleY) scaleX else scaleY
                            val hitRadius = BASE_HIT_RADIUS * scale

                            var matched = false

                            // Check each object
                            level.objects.forEach { obj ->
                                if (!viewModel.isObjectFound(obj.name)) {

                                    // Expected location inside drawn image
                                    val expected = Offset(
                                        x = obj.x * scaleX,
                                        y = obj.y * scaleY
                                    )

                                    val distance = tapInImage
                                        .minus(expected)
                                        .getDistance()

                                    if (distance <= hitRadius) {
                                        matched = true

                                        // Mark found
                                        viewModel.markObjectFound(obj.name)
                                        AudioPlayer.playSoundEffect(context, R.raw.bell_chime)

                                        if (viewModel.allObjectsFound(level.objects)) {
                                            scope.launch {
                                                viewModel.finishGame(level.title)
                                                navController.navigate("game_result/${level.title}")
                                            }
                                        }

                                        return@detectTapGestures
                                    }
                                }
                            }

                            // Optional wrong tap sound
                            if (!matched) {
                                // AudioPlayer.playSoundEffect(context, R.raw.wrong_tap)
                            }
                        }
                    }
            ) {

                // Map sceneImage → drawable
                val mappedName = when (level.sceneImage.lowercase()) {
                    "shiva" -> "shiva"
                    "ganesha" -> "datta"
                    "temple" -> "default_pooja_image"
                    else -> level.sceneImage // supports filename-like keys
                }

                Image(
                    painter = rememberSafePainter(mappedName),
                    contentDescription = level.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coordinates ->
                            // Image bounds relative to this Box
                            imageBounds = coordinates.boundsInParent()
                        }
                )
            }
        }
    }
}
