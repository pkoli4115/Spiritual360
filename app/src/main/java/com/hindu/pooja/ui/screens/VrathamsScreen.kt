package com.hindu.pooja.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hindu.pooja.ui.navigation.Screen

@Composable
    fun VrathamsScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🙏 Vrathams content coming soon!",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }