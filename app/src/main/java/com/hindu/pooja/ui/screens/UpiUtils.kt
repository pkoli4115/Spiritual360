package com.hindu.pooja.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun buildUpiUri(
    upiId: String,
    payeeName: String,
    amount: String?,
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

fun launchUpiChooserIntent(
    context: android.content.Context,
    upiId: String,
    payeeName: String,
    amount: String?,
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
            "No UPI app found. Please install Google Pay, PhonePe, or Paytm.",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Could not open UPI. ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
