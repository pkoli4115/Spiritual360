package com.hindu.pooja.ui.ramayana.hub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Ramayana Hub — entry screen for all Kanda story & Ramakoti flows.
 *
 * Shows ALL 7 kandas (Telugu labels):
 *  - బాలకాండము
 *  - అయోధ్యకాండము
 *  - అరణ్యకాండము
 *  - కిష్కిందకాండము
 *  - సుందరకాండము
 *  - యుద్ధకాండము
 *  - ఉత్తరకాండము
 */
@Composable
fun RamayanaHubScreen(
    onOpenBala: () -> Unit,
    onOpenAyodhya: () -> Unit,
    onOpenRamakoti: () -> Unit,
    onOpenAranya: () -> Unit = {},
    onOpenKishkindha: () -> Unit = {},
    onOpenSundara: () -> Unit = {},
    onOpenYuddha: () -> Unit = {},
    onOpenUttara: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "రామాయణ ప్రయాణం",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "ఏ కాండం నుంచి కావాలంటే అక్కడినుంచి ప్రారంభించండి. ప్రతి కాండం చివరలో చిన్న క్విజ్ కూడా ఉంటుంది.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(8.dp))

        // Bala Kanda
        KandaCard(
            title = "బాలకాండము",
            description = "Sri Rama jananam, Vishwamitra yatra, Sita swayamvaram and early life stories.",
            buttonLabel = "Open Bala Kanda",
            onClick = onOpenBala
        )

        // Ayodhya Kanda
        KandaCard(
            title = "అయోధ్యకాండము",
            description = "Kaikeyi boons, Rama vanavasam, Bharata meeting in Nandigram and more.",
            buttonLabel = "Open Ayodhya Kanda",
            onClick = onOpenAyodhya
        )

        // Aranya Kanda
        KandaCard(
            title = "అరణ్యకాండము",
            description = "Forest life, Surpanakha, Mareecha, Sita apaharanam and Jatayu moksham.",
            buttonLabel = "Open Aranya Kanda",
            onClick = onOpenAranya
        )

        // Kishkindha Kanda
        KandaCard(
            title = "కిష్కిందకాండము",
            description = "Sugriva–Vali story, Hanuman meeting Rama and search party for Sita.",
            buttonLabel = "Open Kishkindha Kanda",
            onClick = onOpenKishkindha
        )

        // Sundara Kanda
        KandaCard(
            title = "సుందరకాండము",
            description = "Hanuman’s leap to Lanka, meeting Sita, burning Lanka and bringing Choodamani.",
            buttonLabel = "Open Sundara Kanda",
            onClick = onOpenSundara
        )

        // Yuddha Kanda
        KandaCard(
            title = "యుద్ధకాండము",
            description = "Setu bandhanam, great war in Lanka and Ravana vadham.",
            buttonLabel = "Open Yuddha Kanda",
            onClick = onOpenYuddha
        )

        // Uttara Kanda
        KandaCard(
            title = "ఉత్తరకాండము",
            description = "Final chapter: Lava–Kusha, Rama pattabhishekam, and Rama’s return to Vaikuntham.",
            buttonLabel = "Open Uttara Kanda",
            onClick = onOpenUttara
        )

        // Optional: Ramakoti shortcut
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📿 Ramakoti",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Write Jai Sri Ram and track your crore journey. Connected to Ramayana section.",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = onOpenRamakoti,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Open Ramakoti")
                }
            }
        }
    }
}

@Composable
private fun KandaCard(
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(buttonLabel)
            }
        }
    }
}
