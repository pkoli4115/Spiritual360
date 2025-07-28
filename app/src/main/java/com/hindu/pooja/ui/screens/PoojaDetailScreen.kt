package com.hindu.pooja.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.data.PoojaContentLoader
import com.hindu.pooja.model.PoojaDetail
import java.util.*

@Composable
fun PoojaDetailScreen(
    navController: NavController,
    fileName: String
) {
    val context = LocalContext.current
    var poojaDetail by remember { mutableStateOf<PoojaDetail?>(null) }

    // Zoom
    var scale by remember { mutableStateOf(1f) }
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    // TTS
    // TTS
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isSpeaking by remember { mutableStateOf(false) }

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
        poojaDetail = PoojaContentLoader.loadPoojaContent(context, fileName)
    }

    poojaDetail?.let { detail ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 3f)
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
                    .align(Alignment.TopStart) // prevent blank top space
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = detail.name,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    detail.content?.forEach { (title, contentText) ->
                        Text(
                            text = "$title\n$contentText",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (isSpeaking) {
                        tts?.stop()
                        isSpeaking = false
                    } else {
                        val fullText = buildString {
                            append(detail.name + "\n")
                            detail.content?.forEach { (title, contentText) ->
                                append("$title\n$contentText\n")
                            }
                        }
                        tts?.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, null)
                        isSpeaking = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(if (isSpeaking) "Stop Reading" else "Read Aloud")
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...", style = MaterialTheme.typography.bodyMedium)
    }
}
