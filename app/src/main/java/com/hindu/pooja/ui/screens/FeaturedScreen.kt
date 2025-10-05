package com.hindu.pooja.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.ui.navigation.Screen

/**
 * Featured tab/page.
 * Now contains Ramakoti and Bala Kanda Flip Cards.
 */
@Composable
fun FeaturedScreen(navController: NavController) {
    Column(modifier = Modifier.padding(all = 12.dp)) {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        // 🔸 Ramakoti
        FeaturedRamakotiCard {
            navController.navigate(Screen.Ramakoti.route)
        }

        // 🔸 Bala Kanda — Flip Cards (NEW)
        FeaturedBalaKandaCard {
            navController.navigate(Screen.BalaKandaFlip.route)
        }
    }
}

@Composable
private fun FeaturedRamakotiCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Ramakoti",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Write Jai Sri Ram (English / हिंदी / తెలుగు). Unlimited. Daily streaks, reminders & cloud sync.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = onOpen) { Text("Open Ramakoti") }
        }
    }
}

@Composable
private fun FeaturedBalaKandaCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Bala Kanda — Flip Cards",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "20 Bala Kanda lessons with flip cards (EN/TE/HI) and TTS. Learn the story with quick swipes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = onOpen) { Text("Open Bala Kanda") }
        }
    }
}
