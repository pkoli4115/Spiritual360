package com.hindu.pooja.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hindu.pooja.billing.BillingManager
import kotlinx.coroutines.launch

@Composable
fun BillingScreen(
    onPremiumUnlocked: () -> Unit // Call this on success
) {
    val context = LocalContext.current
    val activity = context as Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Remember BillingManager instance
    val billingManager = remember {
        BillingManager(
            context = context,
            onPremiumGranted = {
                coroutineScope.launch { snackbarHostState.showSnackbar("Premium Unlocked!") }
                onPremiumUnlocked()
            },
            onBillingError = { msg ->
                coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
            }
        )
    }

    // Connect to billing on first launch
    LaunchedEffect(Unit) {
        isLoading = true
        billingManager.startConnection {
            isLoading = false
        }
    }

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
                onClick = {
                    isLoading = true
                    billingManager.launchPurchaseFlow(activity, "premium_lifetime", false)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buy Lifetime - ₹1999")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    isLoading = true
                    billingManager.launchPurchaseFlow(activity, "premium_monthly", true)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subscribe Monthly - ₹99")
            }
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = {
                isLoading = true
                billingManager.restorePurchases()
            }) {
                Text("Restore Purchase")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
