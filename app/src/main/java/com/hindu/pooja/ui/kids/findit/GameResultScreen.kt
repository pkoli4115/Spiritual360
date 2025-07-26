package com.hindu.pooja.ui.kids.findit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.util.AudioPlayer
import kotlinx.coroutines.launch
import com.hindu.pooja.data.XpManager
@Composable
fun GameResultScreen(levelName: String, navController: NavController) {
    val context = LocalContext.current
    var totalXp by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        totalXp = XpManager.getXp(context)
        AudioPlayer.playSoundEffect(context, com.hindu.pooja.R.raw.bell_chime)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDE7F6)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎉 Level Completed!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "XP so far: $totalXp",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Play Another")
            }
        }
    }
}
