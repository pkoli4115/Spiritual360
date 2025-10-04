package com.hindu.pooja.ui.ramakoti

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun LanguageChipRow(language: String, onChange: (String) -> Unit) {
    AssistChipGroup(
        items = listOf("en" to "EN", "hi" to "HI", "te" to "TE"),
        selected = language.lowercase(),
        onChange = onChange
    )
}

@Composable
private fun AssistChipGroup(
    items: List<Pair<String, String>>,
    selected: String,
    onChange: (String) -> Unit
) {
    FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
        items.forEach { (code, label) ->
            AssistChip(
                onClick = { onChange(code) },
                label = { Text(label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == code) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
