package com.hindu.pooja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.data.PoojaLoader
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.util.rememberSafePainter
import java.net.URLEncoder

@Composable
fun VrathamsScreen(
    fileName: String,
    navController: NavController
) {
    val context = LocalContext.current
    val poojaList = remember { PoojaLoader.loadPoojaIndex(context, fileName) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Available Vrathams / Nomulu",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(poojaList) { pooja ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            val encodedFile = URLEncoder.encode(pooja.file, "UTF-8")
                            navController.navigate(Screen.PoojaDetail.createRoute(encodedFile))
                        }
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Image(
                            painter = rememberSafePainter(pooja.image), // handles .png/.webp/.jpg + case
                            contentDescription = pooja.name,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = pooja.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
