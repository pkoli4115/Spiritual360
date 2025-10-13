package com.hindu.pooja.feature.ramakoti.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hindu.pooja.feature.ramakoti.util.BlessingCardComposer
import com.hindu.pooja.feature.ramakoti.util.ShareHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BlessingCardScreen(
    devoteeName: String,
    message: String,           // e.g., localized "I completed 1 Crore Sri Rama Namas"
    language: String,
    verificationUrl: String
) {
    val context = LocalContext.current
    var latestPath by remember { mutableStateOf<String?>(null) }
    val dateText = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Blessing Card", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("Create a shareable image card for social media.")

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                val file = BlessingCardComposer.createCard(
                    context,
                    BlessingCardComposer.Input(
                        devoteeName = devoteeName,
                        message = message,
                        dateText = dateText,
                        language = language,
                        qrUrl = verificationUrl
                    )
                )
                latestPath = file.absolutePath
            }) { Text("Create Card") }

            if (latestPath != null) {
                Button(onClick = {
                    ShareHelper.openPdf(context, java.io.File(latestPath!!)) // viewer might open image; if not, use share.
                }) { Text("Open") }

                Button(onClick = {
                    // Use generic share via ACTION_SEND
                    val f = java.io.File(latestPath!!)
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", f
                        )
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share card"))
                }) { Text("Share") }
            }
        }
    }
}
