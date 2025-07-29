package com.hindu.pooja.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.R
import com.hindu.pooja.data.PoojaContentLoader
import com.hindu.pooja.model.PoojaDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import java.util.*
@Composable
fun PoojaDetailScreen(fileName: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var poojaDetail by remember { mutableStateOf<PoojaDetail?>(null) }
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var cancelReading by remember { mutableStateOf(false) }
    var highlightedIndex by remember { mutableStateOf(-1) }

    var currentSection by remember { mutableStateOf(0) }
    var showHint by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
    val rawScale = remember { mutableStateOf(1f) }
    val rawOffset = remember { mutableStateOf(Offset.Zero) }
    val scale by animateFloatAsState(rawScale.value, tween(300), label = "")
    val offset by animateOffsetAsState(rawOffset.value, tween(300), label = "")

    // TTS setup

    DisposableEffect(key1 = context) {
        var engine: TextToSpeech? = null

        engine = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = engine?.setLanguage(Locale("te", "IN"))
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts = engine
                }
            }
        })

        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }




    // Load content
    LaunchedEffect(fileName) {
        poojaDetail = withContext(Dispatchers.IO) {
            PoojaContentLoader.loadPoojaContent(context, fileName)
        }
    }

    // Hide hint after short delay
    LaunchedEffect(Unit) {
        delay(4000)
        showHint = false
    }

    val sections by remember(poojaDetail) {
        mutableStateOf(
            listOfNotNull(
                poojaDetail?.slokas,
                poojaDetail?.verses,
                poojaDetail?.content?.map { "${it.key}: ${it.value}" }
            )
        )
    }

    val currentLines = sections.getOrNull(currentSection) ?: emptyList()

    // 🧠 Auto-scroll listener for book-style paging
    LaunchedEffect(scrollState.value) {
        if (scrollState.value >= scrollState.maxValue && currentSection < sections.lastIndex) {
            delay(200) // debounce
            currentSection++
        }
    }

    // Reset scroll on page change
    LaunchedEffect(currentSection) {
        scrollState.scrollTo(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { if (currentSection > 0) currentSection-- },
                    enabled = currentSection > 0) {
                    Text("◀ Previous")
                }

                Button(onClick = {
                    if (isSpeaking) {
                        cancelReading = true
                        tts?.stop()
                        isSpeaking = false
                        highlightedIndex = -1
                    } else {
                        cancelReading = false
                        isSpeaking = true
                        val fullText = currentLines.joinToString("\n")
                        scope.launch {
                            highlightedIndex = -1
                            tts?.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, null)
                            delay((fullText.length * 20L).coerceAtLeast(1500L))
                            if (!cancelReading) {
                                isSpeaking = false
                                highlightedIndex = -1
                            }
                        }
                    }
                }) {
                    Text(if (isSpeaking) "🔈 Stop Reading" else "🔊 Read Aloud")
                }

                Button(onClick = { if (currentSection < sections.lastIndex) currentSection++ },
                    enabled = currentSection < sections.lastIndex) {
                    Text("Next ▶")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            rawScale.value = (rawScale.value * zoom).coerceIn(0.8f, 3f)
                            rawOffset.value = if (rawScale.value > 1f) rawOffset.value + pan else Offset.Zero
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            rawScale.value = 1f
                            rawOffset.value = Offset.Zero
                        })
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                            transformOrigin = TransformOrigin.Center
                        )
                ) {
                    Text(
                        text = poojaDetail?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    currentLines.forEachIndexed { i, line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (i == highlightedIndex)
                                        Color.Yellow.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                if (showHint) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📌 Pinch to zoom • Double tap to reset",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

