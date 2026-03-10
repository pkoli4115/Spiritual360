package com.hindu.pooja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.ui.kids.flashcards.FlashCardRoutes
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.util.rememberSafePainter

@Composable
fun KidsScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Text(
            text = "Kids Zone",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Fun devotional games and learning for kids.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Devotional Find-It Game
        KidsGameTile(
            imageName = "kids_find_it",
            title = "Devotional Find-It Game",
            subtitle = "Tap to find hidden sacred objects in temple scenes."
        ) {
            navController.navigate(
                Screen.FindItGame.createRoute("hidden_objects_shiva_scene.json")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Know the Gods
        KidsGameTile(
            imageName = "kids_know_gods",
            title = "Know the Gods",
            subtitle = "Flip cards to learn deities, forms & symbols."
        ) {
            navController.navigate(FlashCardRoutes.knowGods())
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sloka meanings
        KidsGameTile(
            imageName = "kids_sloka_meanings",
            title = "Sloka / Aarti Meanings",
            subtitle = "Understand meanings behind daily slokas & aartis."
        ) {
            navController.navigate(FlashCardRoutes.slokaMeanings())
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ramayana stories
        KidsGameTile(
            imageName = "kids_ramayana_stories",
            title = "Ramayana Stories",
            subtitle = "Short story cards from the Ramayana."
        ) {
            navController.navigate(FlashCardRoutes.ramayanaStories())
        }
    }
}

@Composable
private fun KidsGameTile(
    imageName: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .fillMaxWidth(0.2f)
            ) {
                Image(
                    painter = rememberSafePainter(imageName),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
