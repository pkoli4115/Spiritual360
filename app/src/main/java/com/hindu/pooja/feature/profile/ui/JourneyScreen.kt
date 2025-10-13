package com.hindu.pooja.feature.profile.ui
import androidx.compose.material3.ExperimentalMaterial3Api
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hindu.pooja.feature.profile.JourneyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JourneyScreen(
    modifier: Modifier = Modifier,
    vm: JourneyViewModel = viewModel(),
    onOpenCertificates: (() -> Unit)? = null
) {
    val ui = vm.ui.collectAsState().value
    val ctx = LocalContext.current
    val dateFmt = rememberDateFormat()
    @OptIn(ExperimentalMaterial3Api::class)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ramakoti Journey") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Totals",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Total Rama Namas: ${ui.totalCount}")
                        Text("Current Batch: ${ui.currentBatchCount}/108")
                        Text("Current Crore: ${ui.currentCrore}")
                        Spacer(Modifier.height(8.dp))
                        if (ui.badges.isNotEmpty()) {
                            Divider()
                            Spacer(Modifier.height(8.dp))
                            Text("Badges", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ui.badges.forEach {
                                    BadgeChip(it)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { vm.refresh() }) {
                        Text("Refresh")
                    }
                    if (onOpenCertificates != null) {
                        Button(onClick = onOpenCertificates) { Text("Certificates") }
                    }
                }
            }

            item {
                Text(
                    "Completed Crores",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (ui.history.isEmpty()) {
                item {
                    Text("No completed crores yet.")
                }
            } else {
                items(ui.history) { h ->
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Crore #${h.croreNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("Total at Completion: ${h.totalAtCompletion}")
                            Text("Completed: ${h.completedAtMs?.let { dateFmt.format(Date(it)) } ?: "—"}")
                            h.certificateId?.let { Text("Certificate ID: $it") }
                            Spacer(Modifier.height(8.dp))
                            h.certificateUrl?.let { url ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(onClick = { openUrl(ctx, url) }) { Text("Open Certificate") }
                                    OutlinedButton(onClick = { shareText(ctx, "My Ramakoti certificate", url) }) {
                                        Text("Share")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

private fun openUrl(ctx: android.content.Context, url: String) {
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun shareText(ctx: android.content.Context, subject: String, url: String) {
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, url)
    }
    ctx.startActivity(Intent.createChooser(i, "Share"))
}

@Composable
private fun rememberDateFormat(): SimpleDateFormat =
    androidx.compose.runtime.remember {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    }
