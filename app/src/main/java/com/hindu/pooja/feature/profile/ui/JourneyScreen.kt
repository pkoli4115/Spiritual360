package com.hindu.pooja.feature.profile.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class Achievement(
    val targetCount: Int,
    val completedAt: com.google.firebase.Timestamp?,
    val certificateUrl: String,
    val language: String
)

@Composable
fun JourneyScreen(onOpenCertificates: () -> Unit = {}) {
    val ctx = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var items by remember { mutableStateOf<List<Achievement>>(emptyList()) }

    LaunchedEffect(uid) {
        if (uid == null) return@LaunchedEffect
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("ramakoti_achievements")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map {
                    Achievement(
                        targetCount = (it.getLong("targetCount") ?: 0L).toInt(),
                        completedAt = it.getTimestamp("completedAt"),
                        certificateUrl = it.getString("certificateUrl") ?: "",
                        language = it.getString("language") ?: "en"
                    )
                } ?: emptyList()
                items = list
            }
    }
    @OptIn(ExperimentalMaterial3Api::class)

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("My Ramakoti Journey") }) }
    ) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No completed targets yet. Keep going!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { ach ->
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(titleForTarget(ach.targetCount), style = MaterialTheme.typography.titleMedium)
                            val date = ach.completedAt?.toDate()
                            if (date != null) {
                                val fmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                Text("Completed on ${fmt.format(date)}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("Language: ${ach.language}", style = MaterialTheme.typography.bodySmall)

                            if (ach.certificateUrl.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                OutlinedButton(onClick = {
                                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(ach.certificateUrl))
                                    ctx.startActivity(i)
                                }) { Text("View Certificate") }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun titleForTarget(target: Int): String = when (target) {
    100_000 -> "1 Lakh Sri Rama Namas — Completed"
    1_000_000 -> "10 Lakh Sri Rama Namas — Completed"
    10_000_000 -> "1 Crore Sri Rama Namas — Completed"
    else -> "Target $target — Completed"
}
