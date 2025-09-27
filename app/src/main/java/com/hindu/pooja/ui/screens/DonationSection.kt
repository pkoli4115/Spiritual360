package com.hindu.pooja.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.hindu.pooja.R

/** Central config used by Home & Profile */
object DonationConfig {
    const val upiId: String = "9121011887@ybl"          // <-- your UPI ID
    const val payeeName: String = "Koli Prasanth"
    const val note: String = "Donation to Spiritual360 App"
    val qrRes: Int = R.drawable.donation_qr                  // jpg/png, name only (no ext)
}

/** Reusable donation section (UPI link, copy/share, QR, quick amounts) */
@Composable
fun DonationSection(
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    amounts: List<String> = listOf("₹51", "₹101", "₹501", "₹1001", "₹2001", "₹5001")
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showTitle) {
            Text("Support with a Donation", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
        }

        // --- UPI ID (tap to open chooser without preset amount) ---
        Text(
            DonationConfig.upiId,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                launchUpiChooserIntent(
                    context = context,
                    upiId = DonationConfig.upiId,
                    payeeName = DonationConfig.payeeName,
                    amount = null, // user will type the amount
                    note = DonationConfig.note
                )
            }
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = {
                clipboard.setText(AnnotatedString(DonationConfig.upiId))
                Toast.makeText(context, "UPI ID copied", Toast.LENGTH_SHORT).show()
            }) { Text("Copy UPI ID") }

            TextButton(onClick = {
                shareText(context, "UPI ID", DonationConfig.upiId)
            }) { Text("Share UPI ID") }
        }

        Spacer(Modifier.height(12.dp))

        // --- QR from drawable (works best in drawable-nodpi) ---
        Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.medium) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Scan to Pay (Any UPI App)", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = painterResource(DonationConfig.qrRes),
                    contentDescription = "UPI QR",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(DonationConfig.upiId, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- Quick amounts -> chooser WITH preset amount ---
        Text("Quick Amounts", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        FlowRowHorizontalPills(amounts) { label ->
            // ✅ Plain computation (no remember here — this lambda is not @Composable)
            val digits = label.filter { it.isDigit() }
            launchUpiChooserIntent(
                context = context,
                upiId = DonationConfig.upiId,
                payeeName = DonationConfig.payeeName,
                amount = digits,
                note = DonationConfig.note
            )
        }
    }
}

/* ----------------- Helpers (same behavior as Profile) ----------------- */

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

private fun buildUpiUri(
    upiId: String,
    payeeName: String,
    amount: String?, // null => no &am param
    note: String,
    tr: String = "HP-" + System.currentTimeMillis()
): Uri {
    val sb = StringBuilder()
        .append("upi://pay")
        .append("?pa=").append(upiId)
        .append("&pn=").append(Uri.encode(payeeName))
    if (!amount.isNullOrBlank()) sb.append("&am=").append(Uri.encode(amount))
    sb.append("&tn=").append(Uri.encode(note))
        .append("&tr=").append(Uri.encode(tr))
        .append("&cu=INR")
    return Uri.parse(sb.toString())
}

private fun launchUpiChooserIntent(
    context: android.content.Context,
    upiId: String,
    payeeName: String,
    amount: String?, // pass null to let user type amount
    note: String
) {
    val uri = buildUpiUri(upiId, payeeName, amount, note)
    val base = Intent(Intent.ACTION_VIEW, uri)
    val chooser = Intent.createChooser(base, "Pay with UPI")
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No UPI app found. Install Google Pay, PhonePe, or Paytm.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun shareText(context: android.content.Context, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
