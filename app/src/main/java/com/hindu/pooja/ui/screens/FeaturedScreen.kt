package com.hindu.pooja.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.ui.navigation.Screen

private val FEATURED_TILE_HEIGHT = 160.dp

@Composable
fun FeaturedScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        // Ramakoti tile
        FeaturedRamakotiTile(
            onOpen = { navController.navigate(Screen.Ramakoti.route) }
        )
        Spacer(Modifier.height(12.dp))

        // Bala Kanda — simple reader + quiz
        FeaturedBalaKandaTile(
            onOpen = { navController.navigate(Screen.BalaKandaWikiSimple.route) }
        )
        Spacer(Modifier.height(12.dp))

        // Ayodhya Kanda — simple reader + quiz
        FeaturedAyodhyaTile(
            onOpen = { navController.navigate("ramayana/ayodhya/wiki") }
        )
        Spacer(Modifier.height(12.dp))

        // Ramayana Hub — 3 languages, 7 kandas
        FeaturedRamayanaHubTile(
            onOpen = { navController.navigate("ramayana/hub") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedRamakotiTile(onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .height(FEATURED_TILE_HEIGHT)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Ramakoti", style = MaterialTheme.typography.titleLarge)
            Text(
                "Write Sri Rama repeatedly. Streaks & cloud sync.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpen) { Text("Open Ramakoti") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedBalaKandaTile(onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .height(FEATURED_TILE_HEIGHT)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Bala Kanda — Lessons", style = MaterialTheme.typography.titleLarge)
            Text(
                "Read child-friendly Telugu lessons and take the quiz.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpen) { Text("Open Bala Kanda") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedAyodhyaTile(onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .height(FEATURED_TILE_HEIGHT)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Ayodhya Kanda — Lessons", style = MaterialTheme.typography.titleLarge)
            Text(
                "Read Ayodhyakanda in Telugu (reader view) with TTS.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpen) { Text("Open Ayodhya Kanda") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeaturedRamayanaHubTile(onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .height(FEATURED_TILE_HEIGHT)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Ramayana — All Kandas", style = MaterialTheme.typography.titleLarge)
            Text(
                "Browse Bala → Uttara Kanda in Telugu, Hindi & English.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpen) { Text("Open Ramayana Lessons") }
        }
    }
}
