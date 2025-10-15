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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamakotiWriterScreen(
    vm: RamakotiViewModel = hiltViewModel(),
    reminderVm: ReminderVm = hiltViewModel(),
    onPickNextTarget: (() -> Unit)? = null // pass nav: { navController.navigate("ramakoti/language") }
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
            val btnLabel = when {
                ui.isIssuingCertificate -> "Generating Certificate…"
                ui.targetReached && ui.certificateUrl != null -> "View Certificate"
                ui.targetReached -> "Target Completed (Retry)"
                else -> "Jai Sri Ram"
            }

            val enabled = when {
                ui.isIssuingCertificate -> false
                ui.targetReached && ui.certificateUrl == null -> true   // allow retry
                ui.targetReached && ui.certificateUrl != null -> true   // allow open
                else -> !ui.isIncrementBusy
            }

            Button(
                onClick = {
                    when {
                        ui.isIssuingCertificate -> Unit
                        ui.targetReached && ui.certificateUrl != null -> vm.openCertificate(ctx)
                        ui.targetReached -> vm.retryCertificate()
                        else -> {
                            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
                            vm.tickNext()
                        }
                    }
                },
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

    // Celebration dialog for 108 completion (optional)
    if (ui.showCelebration) {
        AlertDialog(
            onDismissRequest = { vm.clearCelebration() },
            title = { Text("Batch Completed!") },
            text = { Text("You completed ${ui.currentBatchNumber - 1} batches of 108!") },
            confirmButton = { TextButton(onClick = { vm.clearCelebration() }) { Text("OK") } }
        )
    }

    // Certificate error
    ui.certificateError?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.dismissCertError() },
            title = { Text("Certificate") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { vm.dismissCertError() }) { Text("OK") } }
        )
    }

    // Prompt to continue to next target
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
            // Save & schedule in a coroutine
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
