package com.hindu.pooja.ui.login.components
import androidx.compose.ui.layout.ContentScale

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.hindu.pooja.R


@Composable
fun AutoScrollImageCarousel(
    imageNames: List<String>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000L)
            currentIndex = (currentIndex + 1) % imageNames.size
        }
    }

    val alphaAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000)
    )

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = getImageResId(imageNames[currentIndex])),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim)
            )
    }
}

private fun getImageResId(name: String): Int {
    return when (name) {
        "datta" -> R.drawable.datta
        "ganesh_family" -> R.drawable.ganesh_family
        "lakshmi_vishnu" -> R.drawable.lakshmi_vishnu
        "family_pooja" -> R.drawable.family_pooja
        else -> error("Image not found: $name")
    }
}
