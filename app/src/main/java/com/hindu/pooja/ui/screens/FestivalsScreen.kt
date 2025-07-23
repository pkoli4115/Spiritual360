package com.hindu.pooja.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.hindu.pooja.viewmodel.*

@Composable
fun FestivalsScreen(baseViewModel: BaseViewModel) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Text(
            text = "Festivals Section (పండుగలు) – Content coming soon!",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(32.dp)
        )
    }
}
