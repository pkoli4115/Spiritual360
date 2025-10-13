package com.hindu.pooja.feature.profile.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hindu.pooja.feature.ramakoti.data.RamakotiExportUploader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CertificatesScreen(
    modifier: Modifier = Modifier
) {
    val items = remember { mutableStateListOf<ExportItem>() }
    val showAll = remember { androidx.compose.runtime.mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val ctx = LocalContext.current

    LaunchedEffect(showAll.value) {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()
        val col = db.collection("ramakotiExports").document(uid).collection("files")
        val baseQuery = if (showAll.value) {
            col.orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
        } else {
            col.whereEqualTo("type", RamakotiExportUploader.ExportType.CERTIFICATE.name)
                .orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
        }

        baseQuery.get().addOnSuccessListener { snapshot ->
            val list = snapshot.documents.map { d ->
                ExportItem(
                    id = d.id,
                    type = d.getString("type") ?: "",
                    filename = d.getString("filename") ?: "",
                    url = d.getString("downloadUrl") ?: "",
                    createdAtMs = d.getTimestamp("createdAt")?.toDate()?.time,
                    certificateId = d.getString("certificateId")
                )
            }
            items.clear(); items.addAll(list)
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (showAll.value) "Exports (All)" else "Certificates") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showAll.value = !showAll.value }) {
                        Text(if (showAll.value) "Show Certificates Only" else "Show All")
                    }
                }
            }

            if (items.isEmpty()) {
                item { Text("No items yet.") }
            } else {
                items(items) { it ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = it.filename.ifBlank { it.type },
                                style = MaterialTheme.typography.titleMedium
                            )
                            it.certificateId?.let { id ->
                                Spacer(Modifier.height(4.dp))
                                Text("Certificate ID: $id")
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Type: ${it.type}")
                            Text("Created: ${it.createdAtMs?.let { ms -> dateFmt.format(Date(ms)) } ?: "—"}")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { openUrl(ctx, it.url) }) { Text("Open") }
                                OutlinedButton(onClick = { shareText(ctx, "Ramakoti export", it.url) }) { Text("Share") }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ExportItem(
    val id: String,
    val type: String,
    val filename: String,
    val url: String,
    val createdAtMs: Long?,
    val certificateId: String?
)

/** ✅ Non-composable helpers that accept Context (no LocalContext here) */
private fun openUrl(ctx: Context, url: String) {
    if (url.isBlank()) return
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun shareText(ctx: Context, subject: String, url: String) {
    if (url.isBlank()) return
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, url)
    }
    ctx.startActivity(Intent.createChooser(i, "Share"))
}
