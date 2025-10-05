package com.hindu.pooja.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.ui.navigation.Screen

@Composable
fun FeaturedScreen(navController: NavController) {
    Column {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(12.dp))

        FeaturedSection(
            onRamakotiClick = { navController.navigate(Screen.Ramakoti.route) },
            onBalaKandaClick = { navController.navigate(Screen.BalaKandaWikiSimple.route) }
        )
    }
}
