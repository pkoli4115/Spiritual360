package com.hindu.pooja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hindu.pooja.R

/**
 * Public, reusable Featured section shown on Home and on the Featured tab.
 */
@Composable
fun FeaturedSection(
    onRamakotiClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(Modifier.height(8.dp))

        // You can add more featured items later; starts with Ramakoti
        val items = listOf(
            FeaturedItem(
                title = "Ramakoti",
                subtitle = "Write Jai Sri Ram (EN/HI/TE). Streaks, reminders & cloud sync.",
                imageRes = R.drawable.ic_stat_ramakoti,
                onClick = onRamakotiClick
            )
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { itCard ->
                FeaturedCard(item = itCard)
            }
        }
    }
}

private data class FeaturedItem(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val onClick: () -> Unit
)

@Composable
private fun FeaturedCard(item: FeaturedItem) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .heightIn(min = 160.dp)
            .clickable { item.onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)
                )
                Text(item.title, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = item.onClick) { Text("Open Ramakoti") }
        }
    }
}
