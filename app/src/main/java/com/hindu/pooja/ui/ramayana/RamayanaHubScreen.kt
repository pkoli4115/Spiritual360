package com.hindu.pooja.ui.ramayana

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RamayanaHubScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Ramayana",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Choose language",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(20.dp))

        RamayanaLangTile("Telugu", "te", navController)
        RamayanaLangTile("Hindi", "hi", navController)
        RamayanaLangTile("English", "en", navController)
    }
}

@Composable
private fun RamayanaLangTile(
    title: String,
    lang: String,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate(RamayanaRoutes.kandas(lang))
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
