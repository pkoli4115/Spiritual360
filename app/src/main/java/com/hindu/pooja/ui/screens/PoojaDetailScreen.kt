package com.hindu.pooja.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.data.PoojaContentLoader
import com.hindu.pooja.model.PoojaDetail
import com.hindu.pooja.model.Katha
import androidx.compose.ui.platform.LocalContext

@Composable
fun PoojaDetailScreen(
    navController: NavController,
    fileName: String // Pass the file name like "daily/hanuman_chalisa.json"
) {
    val context = LocalContext.current

    var poojaDetail by remember { mutableStateOf<PoojaDetail?>(null) }

    LaunchedEffect(fileName) {
        poojaDetail = PoojaContentLoader.loadPoojaContent(context, fileName)
    }

    poojaDetail?.let { detail ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = detail.name,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (detail.content.isNotBlank()) {
                    Text(
                        text = detail.content,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // Slokas section
            detail.slokas?.takeIf { it.isNotEmpty() }?.let { slokas ->
                item {
                    Text(
                        text = "Slokas",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(slokas) { sloka ->
                    Text(
                        text = sloka,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Kathalu section
            detail.kathalu?.takeIf { it.isNotEmpty() }?.let { kathalu ->
                item {
                    Text(
                        text = "Katha / Stories",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(kathalu) { katha ->
                    if (katha.title.isNotBlank()) {
                        Text(
                            text = katha.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    if (katha.content.isNotBlank()) {
                        Text(
                            text = katha.content,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            // Verses section
            detail.verses?.takeIf { it.isNotEmpty() }?.let { verses ->
                item {
                    Text(
                        text = "Verses",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(verses) { verse ->
                    Text(
                        text = verse,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    } ?: run {
        // Loading or not found
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = "Loading...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
