package com.hindu.pooja.ui.screens

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hindu.pooja.data.PoojaContentLoader
import com.hindu.pooja.model.PoojaDetail

@Composable
fun PoojaDetailScreen(
    fileName: String
) {
    val context = LocalContext.current
    var poojaDetail by remember { mutableStateOf<PoojaDetail?>(null) }

    var scale by remember { mutableStateOf(1f) }
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3f)
    }

    LaunchedEffect(fileName) {
        poojaDetail = PoojaContentLoader.loadPoojaContent(context, fileName)
    }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .transformable(transformState)
                .padding(12.dp)
                .horizontalScroll(horizontalScroll)
                .verticalScroll(verticalScroll)
        ) {
            Column(
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .fillMaxWidth()
            ) {
                Text(
                    text = poojaDetail?.name ?: "Pooja",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    poojaDetail == null -> {
                        Text("⏳ Loading...", style = MaterialTheme.typography.bodyLarge)
                    }

                    poojaDetail?.slokas?.isEmpty() == true &&
                            poojaDetail?.verses?.isEmpty() == true &&
                            poojaDetail?.kathalu?.isEmpty() == true &&
                            poojaDetail?.content?.isEmpty() == true -> {
                        Text("❌ No content found in this pooja.", style = MaterialTheme.typography.bodyLarge)
                    }

                    else -> {
                        poojaDetail?.slokas?.forEach { sloka ->
                            Text("🔸 $sloka", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        poojaDetail?.verses?.forEach { verse ->
                            Text("🔹 $verse", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        poojaDetail?.content?.forEach { (title, section) ->
                            Text("🪔 $title", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(section, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        poojaDetail?.kathalu?.forEach { katha ->
                            Text("📖 ${katha.title}", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(katha.content, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // Zoom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🔍 Zoom: ${"%.1f".format(scale)}x")
            Button(onClick = { scale = 1f }) {
                Text("Reset Zoom")
            }
        }
    }
}
