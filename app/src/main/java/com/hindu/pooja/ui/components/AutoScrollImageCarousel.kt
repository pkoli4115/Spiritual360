package com.hindu.pooja.ui.login.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import com.hindu.pooja.util.rememberSafePainter
import kotlinx.coroutines.delay

@Composable
fun AutoScrollImageCarousel(
    imageNames: List<String>,
    modifier: Modifier = Modifier
) {
    // Nothing to show → no modulo crash
    if (imageNames.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    var currentIndex by remember { mutableStateOf(0) }

    // auto-advance
    LaunchedEffect(imageNames) {
        while (true) {
            delay(4000L)
            currentIndex = (currentIndex + 1) % imageNames.size
        }
    }

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "carouselAlpha$currentIndex"
    )

    Box(modifier = modifier) {
        val name = imageNames[currentIndex]
        Image(
            painter = rememberSafePainter(name),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentScale = ContentScale.Crop
        )
    }
}
