package com.hindu.pooja.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun BillingScreen(
    onPurchaseClick: (String) -> Unit,
    onRestoreClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Unlock Premium", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("• Unlimited Access\n• Premium Content\n• No Ads\n• Priority Support")
            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { onPurchaseClick("premium_lifetime") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buy Lifetime - ₹1999")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onPurchaseClick("premium_monthly") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subscribe Monthly - ₹99")
            }
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onRestoreClick) {
                Text("Restore Purchase")
            }
        }

        // Simple snackbar for demo (remove or replace when integrating real billing)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
