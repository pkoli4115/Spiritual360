@file:OptIn(ExperimentalMaterial3Api::class)

package com.hindu.pooja.feature.ramakoti.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hindu.pooja.feature.ramakoti.prefs.LanguagePreferenceManager
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
import com.hindu.pooja.feature.ramakoti.reminders.ReminderScheduler
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.ui.ramakoti.LanguageChipRow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LanguageSelectionScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val langMgr   = remember { LanguagePreferenceManager.getInstance(ctx) }
    val prefs     = remember { RamakotiPreferences.getInstance(ctx) }
    val scheduler = remember { ReminderScheduler(ctx) }
    val db        = remember { FirebaseFirestore.getInstance() }
    val auth      = remember { FirebaseAuth.getInstance() }

    var lang by remember { mutableStateOf("en") }
    var target by remember { mutableStateOf(10_000_000) }

    val notifPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        lang = langMgr.languageFlowFor(uid).first().ifBlank { "en" }
        target = prefs.targetCount.first()
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Select Language & Target") }) }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Choose your preferred language", style = MaterialTheme.typography.titleMedium)
            LanguageChipRow(language = lang, onChange = { lang = it })

            Spacer(Modifier.height(8.dp))

            Text("Select your Ramakoti target", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TargetChip(selected = target == 100_000,   label = "1 Lakh")   { target = 100_000 }
                TargetChip(selected = target == 1_000_000, label = "10 Lakh")  { target = 1_000_000 }
                TargetChip(selected = target == 10_000_000,label = "1 Crore")  { target = 10_000_000 }
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                scope.launch {
                    val uid = auth.currentUser?.uid ?: return@launch

                    // Persist per-user language & selected target (same as today)
                    langMgr.setLanguageFor(uid, lang)
                    prefs.setTargetCount(target)

                    // Create a NEW RUN document → resets Writer to 0/108 for this run
                    val runId = "run-${UUID.randomUUID()}"
                    val runDoc = db.collection("users").document(uid)
                        .collection("ramakotiRuns").document(runId)

                    val runData = hashMapOf(
                        "runId" to runId,
                        "uid" to uid,
                        "language" to lang,
                        "targetCount" to target,
                        "runTotal" to 0,                // shows 0/108, 0/target
                        "status" to "ACTIVE",           // ACTIVE | COMPLETED | CANCELLED
                        "startedAt" to Timestamp.now()
                    )
                    runDoc.set(runData).addOnSuccessListener {
                        // Save as current run
                        scope.launch { prefs.setCurrentRunId(runId) }
                    }

                    // Enable a default reminder at 07:00
                    prefs.setReminderEnabled(true)
                    scheduler.scheduleDaily(hour24 = 7, minute = 0)

                    if (Build.VERSION.SDK_INT >= 33) {
                        notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    // Go through your canonical entry so Intro shows if you still keep it
                    navController.navigate(Screen.Ramakoti.route) {
                        popUpTo("ramakoti/language") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }) { Text("Continue") }
        }
    }
}

@Composable
private fun TargetChip(selected: Boolean, label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
