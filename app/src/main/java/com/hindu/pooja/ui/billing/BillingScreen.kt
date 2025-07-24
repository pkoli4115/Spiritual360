package com.hindu.pooja.ui.billing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Upgrade to Premium") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Billing screen coming soon!", style = MaterialTheme.typography.titleMedium)
        }
    }
}
