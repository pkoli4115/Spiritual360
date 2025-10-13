package com.hindu.pooja.feature.ramakoti.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun CompletionBadgeRow(
    croreCount: Int,
    totalCount: Long
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = {}, label = { Text("$croreCount Crore(s)") })
        AssistChip(onClick = {}, label = { Text("$totalCount Total") })
    }
}
