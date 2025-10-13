package com.hindu.pooja.ui.ramakoti

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
// Keep ONLY ONE FlowRow import. If you use Accompanist FlowRow, keep the line below:
import com.google.accompanist.flowlayout.FlowRow
// If you prefer the foundation FlowRow instead, replace the line above with:
// import androidx.compose.foundation.layout.FlowRow

@Composable
fun LanguageChipRow(
    language: String,
    onChange: (String) -> Unit
) {
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
    FlowRow(
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        items.forEach { (code, label) ->
            AssistChip(
                onClick = { onChange(code) },
                label = { Text(label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == code)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
