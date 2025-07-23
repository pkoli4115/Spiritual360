package com.hindu.pooja.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable

@Composable
fun TextZoomControls(
    fontScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Row {
        IconButton(onClick = onZoomOut) {
            Icon(Icons.Filled.Remove, contentDescription = "Zoom Out")
        }
        Text(text = "${(fontScale * 100).toInt()}%")
        IconButton(onClick = onZoomIn) {
            Icon(Icons.Filled.Add, contentDescription = "Zoom In")
        }
    }
}
