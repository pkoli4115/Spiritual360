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
    const val upiId: String = "9121011887@ybl"      // your UPI ID
    const val payeeName: String = "Koli Prasanth"
    const val note: String = "Donation to eRamakoti App"
    val qrRes: Int = R.drawable.donation_qr         // jpg/png in res/drawable
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

        // UPI ID (tap to open chooser without preset amount)
        Text(
            DonationConfig.upiId,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
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

        // QR from drawable (best in drawable-nodpi)
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

        // Quick amounts -> chooser WITH preset amount
        Text("Quick Amounts", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        DonationAmountPills(amounts) { label ->
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

/** Public, shared pills — renamed to avoid name clash */
@Composable
fun DonationAmountPills(
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

private fun shareText(context: android.content.Context, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, title))
}