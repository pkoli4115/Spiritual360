package com.hindu.pooja.ui.ramakoti

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProgressBar108(progress: Int) {
    Column(Modifier.fillMaxWidth()) {
        Text("Progress: $progress / 108", style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(
            progress = progress.coerceIn(0,108) / 108f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
