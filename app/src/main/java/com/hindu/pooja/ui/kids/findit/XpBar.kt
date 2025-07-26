package com.hindu.pooja.ui.kids.findit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun XpBar(modifier: Modifier = Modifier, currentXp: Int, maxXp: Int = 1000) {
    val xpFraction = (currentXp.coerceAtMost(maxXp)).toFloat() / maxXp.toFloat()

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Text(
            text = "XP: $currentXp / $maxXp",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(xpFraction)
                    .background(Color(0xFF4CAF50)) // Green progress bar
            )
        }
    }
}
