package com.hindu.pooja.feature.ramakoti.ui

import android.app.TimePickerDialog
import android.media.AudioAttributes
import android.media.SoundPool
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hindu.pooja.R
import com.hindu.pooja.feature.ramakoti.RamakotiViewModel
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
import com.hindu.pooja.feature.ramakoti.reminders.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Import your BlessingCardScreen
import com.hindu.pooja.feature.ramakoti.ui.BlessingCardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamakotiWriterScreen(
    vm: RamakotiViewModel = hiltViewModel(),
    reminderVm: ReminderVm = hiltViewModel(),
    onPickNextTarget: (() -> Unit)? = null // pass { navController.navigate("ramakoti/language") }
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Re-attach Firestore listeners / one-shot loads every time we land here
    LaunchedEffect(Unit) { vm.refreshFromServer() }

    /* ---------- SoundPool (tap chime) ---------- */
    var soundPool by remember { mutableStateOf<SoundPool?>(null) }
    var soundId by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val sp = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            ).build()
        soundPool = sp
        soundId = sp.load(ctx, R.raw.jaisriramtone, 1)
    }
    DisposableEffect(Unit) { onDispose { soundPool?.release() } }

    // -------- Blessing → Celebration flow controller --------
    // When a batch completes (ui.showCelebration == true), we first show BlessingCard,
    // then show the celebration dialog, then clear celebration in VM.
    var showBlessing by remember { mutableStateOf(false) }
    var showPostBlessingCelebration by remember { mutableStateOf(false) }

    // Gate the sequence whenever a new batch completion arrives
    LaunchedEffect(ui.showCelebration) {
        if (ui.showCelebration) {
            showBlessing = true
            showPostBlessingCelebration = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ramakoti") },
                actions = {
                    IconButton(onClick = {
                        showTimePickerDialog(
                            context = ctx,
                            prefs = RamakotiPreferences.getInstance(ctx),
                            scheduler = reminderVm.scheduler,
                            scope = scope
                        )
                    }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Set reminder")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFFF7EA))
        ) {
            // Header line
            Text(
                text = "Total: ${ui.lifetimeCount} • Batch ${ui.currentBatchNumber} (${ui.currentBatchCount}/108) • Target: ${ui.lifetimeCount} / ${ui.targetCount}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            // 12 x 9 = 108 grid
            val cells = remember { (1..108).toList() }
            LazyVerticalGrid(
                columns = GridCells.Fixed(12),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cells) { index ->
                    GridCell(filled = index <= ui.currentBatchCount, size = 28.dp)
                }
            }

            // Batch progress
            Text(
                text = "Progress: ${ui.currentBatchCount} / 108",
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 6.dp),
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = (ui.currentBatchCount / 108f).coerceIn(0f, 1f),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(4.dp)
            )

            // Target progress
            Spacer(Modifier.height(16.dp))
            Text(
                "Steps toward 1 Lakh",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = (ui.lifetimeCount.toFloat() / ui.targetCount.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(6.dp)
            )
            Text(
                "${ui.lifetimeCount} / ${ui.targetCount}",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            // Button label + behavior based on certificate states
            val (btnLabel, enabled, onClick) = remember(ui.isIssuingCertificate, ui.certificateUrl, ui.certificateError, ui.targetReached, ui.isIncrementBusy) {
                when {
                    ui.isIssuingCertificate -> Triple("Generating Certificate…", false, {})
                    ui.certificateUrl != null -> Triple("View Certificate", true, { vm.openCertificate(ctx) })
                    ui.certificateError != null -> Triple("Retry", true, { vm.retryCertificate() })
                    ui.targetReached -> Triple("Target Completed", false, {})
                    else -> Triple("Jai Sri Ram", !ui.isIncrementBusy, {
                        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
                        vm.tickNext()
                    })
                }
            }

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(btnLabel)
            }
        }
    }

    // ---------------- Blessing → Celebration sequence ----------------

    // 1) Blessing Card (shown first when a batch completes)
    if (ui.showCelebration && showBlessing) {
        // Use a full-screen dialog to host the BlessingCardScreen + controls
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            // If dismissed, still continue to celebration
            showBlessing = false
            showPostBlessingCelebration = true
        }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Blessing message for the just-completed batch
                    val message = "Completed ${ui.currentBatchNumber - 1} × 108 Sri Rama Namas"
                    BlessingCardScreen(
                        devoteeName = "Devotee", // or plumb from profile/auth if you prefer
                        message = message,
                        language = ui.language,
                        verificationUrl = "" // not needed for blessing card
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showBlessing = false
                            showPostBlessingCelebration = true
                        }) { Text("Continue") }
                    }
                }
            }
        }
    }

    // 2) Celebration dialog (shown AFTER blessing)
    if (ui.showCelebration && !showBlessing && showPostBlessingCelebration) {
        AlertDialog(
            onDismissRequest = {
                showPostBlessingCelebration = false
                vm.clearCelebration()
            },
            title = { Text("Batch Completed!") },
            text = { Text("You completed ${ui.currentBatchNumber - 1} full batches of 108!") },
            confirmButton = {
                TextButton(onClick = {
                    showPostBlessingCelebration = false
                    vm.clearCelebration()
                }) { Text("OK") }
            }
        )
    }

    // 3) Certificate error
    ui.certificateError?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.dismissCertError() },
            title = { Text("Certificate") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { vm.dismissCertError() }) { Text("OK") } }
        )
    }

    // 4) Prompt to continue to next target (wired to navigation)
    if (ui.showNextTargetPrompt) {
        AlertDialog(
            onDismissRequest = { vm.onNextTargetDecision(false) {} },
            title = { Text("Continue your Ramakoti?") },
            text = { Text("Would you like to pick the next target and continue your Ramakoti journey?") },
            dismissButton = {
                TextButton(onClick = { vm.onNextTargetDecision(false) {} }) { Text("Not now") }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.onNextTargetDecision(true) {
                        onPickNextTarget?.invoke()
                    }
                }) { Text("Choose next target") }
            }
        )
    }

    // Optional generic error dialog
    ui.error?.let { err ->
        AlertDialog(
            onDismissRequest = { /* keep open until user closes */ },
            title = { Text("Oops") },
            text = { Text(err) },
            confirmButton = { TextButton(onClick = { /* no-op */ }) { Text("Close") } }
        )
    }
}

@Composable
private fun GridCell(filled: Boolean, size: Dp) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(if (filled) Color(0xFFFFDCA8) else Color(0xFFFDF6EE))
            .border(BorderStroke(1.dp, Color(0xFFE7D7C7)), shape)
    )
}

/** Small helper VM to obtain ReminderScheduler via Hilt inside the Composable scope. */
@HiltViewModel
class ReminderVm @Inject constructor(
    val scheduler: ReminderScheduler
) : androidx.lifecycle.ViewModel()

/** 24-hour TimePicker that saves to DataStore and schedules the alarm (uses a regular CoroutineScope). */
private fun showTimePickerDialog(
    context: android.content.Context,
    prefs: RamakotiPreferences,
    scheduler: ReminderScheduler,
    scope: CoroutineScope
) {
    val now = java.util.Calendar.getInstance()
    val hourNow = now.get(java.util.Calendar.HOUR_OF_DAY)
    val minuteNow = now.get(java.util.Calendar.MINUTE)

    val dialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            scope.launch {
                prefs.setReminderEnabled(true)
                prefs.setReminderHour(selectedHour)
                prefs.setReminderMinute(selectedMinute)
                scheduler.scheduleDaily(selectedHour, selectedMinute)
            }
            Toast.makeText(
                context,
                "Daily reminder set for %02d:%02d".format(selectedHour, selectedMinute),
                Toast.LENGTH_SHORT
            ).show()
        },
        hourNow,
        minuteNow,
        true // 24-hour mode
    )
    dialog.show()
}
