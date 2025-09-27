package com.hindu.pooja.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.R
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.viewmodel.ProfileViewModel

// <<< Update these if needed >>>
private const val DONATION_UPI_ID = "9121011887@ybl"
private const val DONATION_PAYEE_NAME = "Koli Prasanth"
private const val DONATION_NOTE = "Donation to Spiritual360 App"
// If your QR file is named differently, change this:
private val QR_DRAWABLE_RES = R.drawable.donation_qr

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onLogout: (() -> Unit)? = null
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val profileId by viewModel.profileId.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val photo by viewModel.profilePictureUrl.collectAsState()
    val provider by viewModel.loginProvider.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.lastError.collectAsState()

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
                            "🙏 Support Spiritual360 App with a voluntary donation",
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

            // Photo
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

            // Info
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

            // Actions
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
                                if (ok) onLogout?.invoke()
                                else Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Logout") }
                }
            }

            // UPI ID block — clickable link + copy/share
            item {
                val clipboard = LocalClipboardManager.current
                Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("UPI ID", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            DONATION_UPI_ID,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                // Open chooser WITHOUT preset amount: user can type any amount
                                launchUpiChooserIntent(
                                    context = context,
                                    upiId = DONATION_UPI_ID,
                                    payeeName = DONATION_PAYEE_NAME,
                                    amount = null, // <- no preset amount
                                    note = DONATION_NOTE
                                )
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = {
                                clipboard.setText(AnnotatedString(DONATION_UPI_ID))
                                Toast.makeText(context, "UPI ID copied", Toast.LENGTH_SHORT).show()
                            }) { Text("Copy UPI ID") }

                            TextButton(onClick = {
                                shareText(context, "UPI ID", DONATION_UPI_ID)
                            }) { Text("Share UPI ID") }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tip: Tap the UPI ID to pick your UPI app and enter any amount.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // QR from drawable
            item {
                Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Scan to Pay (Any UPI App)", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(12.dp))
                        Image(
                            painter = painterResource(QR_DRAWABLE_RES),
                            contentDescription = "UPI QR",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 220.dp)
                                .padding(horizontal = 24.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(DONATION_UPI_ID, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Quick amounts -> chooser WITH preset amount
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Choose an amount:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val donationAmounts = listOf("₹51", "₹101", "₹501", "₹1001", "₹2001", "₹5001")
                    FlowRowHorizontalPills(donationAmounts) { label ->
                        val digits = label.filter { it.isDigit() }
                        launchUpiChooserIntent(
                            context = context,
                            upiId = DONATION_UPI_ID,
                            payeeName = DONATION_PAYEE_NAME,
                            amount = digits,            // <- preset amount path
                            note = DONATION_NOTE
                        )
                    }
                }
            }

            if (isLoading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
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

/** Build UPI URI; includes amount only when provided */
private fun buildUpiUri(
    upiId: String,
    payeeName: String,
    amount: String?, // null => no &am param
    note: String,
    tr: String = "HP-" + System.currentTimeMillis()
): Uri {
    val base = StringBuilder()
        .append("upi://pay")
        .append("?pa=").append(upiId)
        .append("&pn=").append(Uri.encode(payeeName))
        .apply {
            if (!amount.isNullOrBlank()) append("&am=").append(Uri.encode(amount))
        }
        .append("&tn=").append(Uri.encode(note))
        .append("&tr=").append(Uri.encode(tr))
        .append("&cu=INR")
        .toString()
    return Uri.parse(base)
}

/** System chooser so user picks GPay/PhonePe/Paytm/BHIM/etc. */
private fun launchUpiChooserIntent(
    context: android.content.Context,
    upiId: String,
    payeeName: String,
    amount: String?, // pass null for “let user type amount”
    note: String
) {
    val uri = buildUpiUri(upiId, payeeName, amount, note)
    val base = Intent(Intent.ACTION_VIEW, uri)
    val chooser = Intent.createChooser(base, "Pay with UPI")
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No UPI app found. Install Google Pay, PhonePe, or Paytm.", Toast.LENGTH_LONG).show()
    }
}

/** Share plain text (UPI ID) */
private fun shareText(context: android.content.Context, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
