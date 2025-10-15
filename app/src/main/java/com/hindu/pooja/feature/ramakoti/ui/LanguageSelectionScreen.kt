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
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.feature.ramakoti.data.LanguagePreferenceManager
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
import com.hindu.pooja.feature.ramakoti.reminders.ReminderScheduler
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.ui.ramakoti.LanguageChipRow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Singletons (constructors are private)
    val langMgr   = remember { LanguagePreferenceManager.getInstance(ctx) }
    val prefs     = remember { RamakotiPreferences.getInstance(ctx) }
    val scheduler = remember { ReminderScheduler(ctx) }

    // Local UI state
    var lang by remember { mutableStateOf("en") }
    var target by remember { mutableStateOf(10_000_000) } // 1 Crore default

    // Android 13+ notifications permission
    val notifPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    // Load saved choices for current (or null) user
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
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
            LanguageChipRow(
                language = lang,
                onChange = { code -> lang = code }
            )

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
                    val uid = FirebaseAuth.getInstance().currentUser?.uid

                    // Persist choices
                    langMgr.setLanguageFor(uid, lang) // per-user
                    prefs.setTargetCount(target)       // device (your current prefs scope)
                    prefs.setReminderEnabled(true)

                    // Schedule daily reminder at 07:00
                    scheduler.scheduleDaily(hour24 = 7, minute = 0)

                    // Ask POST_NOTIFICATIONS on Android 13+
                    if (Build.VERSION.SDK_INT >= 33) {
                        notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    // Proceed to Ramakoti
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
