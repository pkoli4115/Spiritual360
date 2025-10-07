package com.hindu.pooja.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class) // ← Opt-in to use Card(onClick)
private val FEATURED_TILE_HEIGHT = 160.dp

@Composable
fun FeaturedScreen(navController: NavController) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = "Featured", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        // Vertical tiles (same height as Home tiles)
        FeaturedRamakotiTile(
            onOpen = { navController.navigate(Screen.Ramakoti.route) }
        )
        Spacer(Modifier.height(12.dp))
        FeaturedBalaKandaTile(
            // Open the simple reader+quiz path as agreed
            onOpen = { navController.navigate(Screen.BalaKandaWikiSimple.route) }
        )
        Spacer(Modifier.height(12.dp))

        // ✅ Ayodhyakanda — Reader (same layout preserved)
        FeaturedAyodhyaTile(
            onOpen = { navController.navigate("ramayana/ayodhya/wiki") }
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
