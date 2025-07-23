package com.hindu.pooja.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.navigation.Screen

@Composable
fun PoojasScreen(navController: NavController) {
    Column(modifier = Modifier.padding(all = 16.dp)) {
        Text(text = "Poojas", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate(
                        Screen.PoojaDetail.createRoute(
                            "satyanarayana_vratham_te.json",
                            "satyanarayana_bg"
                        )
                    )
                }
        ) {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(text = "#Satyanarayana Vratham (సత్యనారాయణ వ్రతం)")
                Text(text = "→ తెలుగు")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "#Festivals Section (పండుగలు) – Content coming soon!")
    }
}
