package com.hindu.pooja.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BillingScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Upgrade to Premium", style = MaterialTheme.typography.headlineSmall)

        Text("Benefits of Premium:", style = MaterialTheme.typography.titleMedium)
        Text("• Access exclusive Poojas\n• Kids games unlocked\n• No ads\n• Early access to new features")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            // Simulate successful purchase (replace with actual Billing flow)
            navController.navigate("profile") {
                popUpTo("profile") { inclusive = true }
            }
        }) {
            Text("Buy Premium ₹1999 / ₹99 per month")
        }

        OutlinedButton(onClick = { navController.popBackStack() }) {
            Text("Cancel")
        }
    }
}
