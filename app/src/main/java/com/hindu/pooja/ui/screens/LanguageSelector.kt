package com.hindu.pooja.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LanguageSelector(
    selectedLang: String,
    onLangChange: (String) -> Unit
) {
    Row {
        // Simple horizontal buttons for language switching
        Button(
            onClick = { onLangChange("te") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedLang == "te") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text("తెలుగు")
        }
        Button(
            onClick = { onLangChange("hi") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedLang == "hi") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text("हिन्दी")
        }
        Button(
            onClick = { onLangChange("mr") },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedLang == "mr") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("मराठी")
        }
    }
}
