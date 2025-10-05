package com.hindu.pooja.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Public Featured section used by the Featured tab/page.
 * - Keeps Ramakoti navigation exactly as-is
 * - Adds Bala Kanda (Wiki reader + Quiz) card
 * - No name collisions with HomeScreen helpers
 */
@Composable
fun FeaturedSection(
    onRamakotiClick: () -> Unit,
    onBalaKandaClick: () -> Unit
) {
    Column(Modifier.padding(horizontal = 8.dp)) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Highlights",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        val items = listOf(
            FeaturedCardData(
                title = "Ramakoti",
                subtitle = "Write Jai Sri Ram. Streaks & cloud sync.",
                onClick = onRamakotiClick
            ),
            FeaturedCardData(
                title = "Bala Kanda — Lessons",
                subtitle = "Simple Telugu lessons + Take Quiz (15 Qs, 80% pass).",
                onClick = onBalaKandaClick
            )
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { data ->
                FeaturedCard(data)
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

private data class FeaturedCardData(
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
private fun FeaturedCard(data: FeaturedCardData) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { data.onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(data.title, style = MaterialTheme.typography.titleMedium)
            Text(
                data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = data.onClick) { Text("Open") }
        }
    }
}
