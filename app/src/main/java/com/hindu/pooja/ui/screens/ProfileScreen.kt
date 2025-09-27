package com.hindu.pooja.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.R
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onLogout: (() -> Unit)? = null
) {
    val context = LocalContext.current

    // Refresh each time we land on this screen so data reflects latest login merge
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val profileId by viewModel.profileId.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val photo by viewModel.profilePictureUrl.collectAsState()
    val provider by viewModel.loginProvider.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.lastError.collectAsState()

    // Keep these as STRINGS to match FlowRowHorizontalPills signature
    val donationAmounts = listOf("₹51", "₹101", "₹501", "₹1001", "₹2001", "₹5001")

    Scaffold { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "🙏 Support HinduPooja App with a voluntary donation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "(All features are free for everyone)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val painter = if (!photo.isNullOrBlank())
                        rememberAsyncImagePainter(model = photo)
                    else painterResource(R.drawable.ic_profile_placeholder)

                    Image(
                        painter = painter,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.2f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Profile ID: $profileId", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Name: $fullName", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: $email", style = MaterialTheme.typography.bodyLarge)
                    if (phone.isNotBlank()) Text("Mobile: $phone", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Logged in using $provider", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.EditProfile.route) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Edit Profile") }

                    OutlinedButton(
                        onClick = {
                            viewModel.logout { ok ->
                                if (ok) onLogout?.invoke() else {
                                    Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Logout") }
                }
            }

            // Donations UI — unchanged (just launches UPI)
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Choose an amount:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    FlowRowHorizontalPills(donationAmounts) { label ->
                        val digits = label.filter { it.isDigit() }
                        launchUpiIntent(
                            context = context,
                            upiId = "qtilabs@okhdfcbank",
                            payeeName = "HinduPooja App",
                            amount = digits,
                            note = "Donation to HinduPooja App"
                        )
                    }
                }
            }

            if (isLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            error?.let {
                item { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FlowRowHorizontalPills(
    labels: List<String>,
    onClick: (String) -> Unit
) {
    Column {
        var row = mutableListOf<String>()
        var count = 0
        val maxPerRow = 3
        labels.forEachIndexed { i, label ->
            row.add(label); count++
            val end = (count == maxPerRow) || (i == labels.lastIndex)
            if (end) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { text ->
                        AssistChip(onClick = { onClick(text) }, label = { Text(text) })
                    }
                }
                row = mutableListOf(); count = 0
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private fun launchUpiIntent(
    context: android.content.Context,
    upiId: String,
    payeeName: String,
    amount: String,
    note: String
) {
    val base = "upi://pay?pa=$upiId&pn=${Uri.encode(payeeName)}&cu=INR&tn=${Uri.encode(note)}&am=${Uri.encode(amount)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(base))
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No UPI app found. Please install GPay/PhonePe/Paytm.", Toast.LENGTH_LONG).show()
    }
}
