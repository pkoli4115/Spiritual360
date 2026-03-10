package com.hindu.pooja.feature.ramakoti.ui
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.R
import com.hindu.pooja.feature.ramakoti.RamakotiViewModel
import com.hindu.pooja.feature.ramakoti.i18n.RamakotiLanguages
import com.hindu.pooja.feature.ramakoti.prefs.LanguagePreferenceManager
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
    onPickNextTarget: (() -> Unit)? = null
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { RamakotiPreferences.getInstance(ctx) }
    val reminderEnabled by prefs.reminderEnabled.collectAsState(initial = false)
    // Ensure Firestore listeners are attached whenever we land here
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

    // ---- LANGUAGE: prefer run language; fall back to saved preference for safety ----
    val auth = remember { FirebaseAuth.getInstance() }
    val effectiveLang by produceState(initialValue = ui.language) {
        if (value.isBlank() || value == "en") {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val mgr = LanguagePreferenceManager.getInstance(ctx)
                val saved = mgr.getLanguageFor(uid) // suspend ok in produceState
                if (!saved.isNullOrBlank()) value = saved
            }
        }
    }

    // Localized mantra (cells + active button label)
    val mantra by remember(effectiveLang) {
        mutableStateOf(RamakotiLanguages.mantraFor(effectiveLang))
    }

    // ---- Adaptive grid: 6x18 on phones, 12x9 on larger screens/tablets ----
    val config = LocalConfiguration.current
    val columns = if (config.screenWidthDp < 600) 6 else 12
    val cellHeight = 38.dp // rectangular rows; helps Indic scripts fit

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ramakoti") },
                actions = {
                    Box(
                        modifier = Modifier.padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (reminderEnabled) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = Color(0xFFFFE8C7),
                                        shape = CircleShape
                                    )
                            )
                        }

                        IconButton(
                            onClick = {
                                showTimePickerDialog(
                                    context = ctx,
                                    prefs = prefs,
                                    scheduler = reminderVm.scheduler,
                                    scope = scope
                                )
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Set reminder",
                                tint = if (reminderEnabled) Color(0xFFB87333) else LocalContentColor.current
                            )
                        }
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
            // Header line (RUN progress)
            Text(
                text = "Count: ${ui.runTotal} • Batch ${ui.currentBatchNumber} (${ui.currentBatchCount}/108) • Target: ${ui.runTotal} / ${ui.targetCount}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            // 108 grid (adaptive columns)
            val cells = remember { (1..108).toList() }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(cells) { index ->
                    GridCell(
                        filled = index <= ui.currentBatchCount,
                        height = cellHeight,
                        text = if (index <= ui.currentBatchCount) mantra else ""
                    )
                }
            }

            // Batch progress (RUN)
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

            // Target progress (RUN)
            Spacer(Modifier.height(16.dp))
            Text(
                "Steps toward target",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = (ui.runTotal.toFloat() / ui.targetCount.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(6.dp)
            )
            Text(
                "${ui.runTotal} / ${ui.targetCount}",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            // Primary button (localized label when actively writing)
            val btnLabel = when {
                ui.isIssuingCertificate -> "Generating..."
                ui.targetReached && ui.certificateUrl != null -> "View Certificate"
                ui.targetReached -> "Target Completed"
                else -> RamakotiLanguages.writerButtonLabel(effectiveLang)
            }

            val enabled = when {
                ui.isIssuingCertificate -> false
                ui.targetReached && ui.certificateUrl == null -> false
                ui.targetReached && ui.certificateUrl != null -> true
                else -> !ui.isIncrementBusy
            }

            Button(
                onClick = {
                    when {
                        ui.isIssuingCertificate -> Unit
                        ui.targetReached && ui.certificateUrl != null -> vm.openCertificate(ctx)
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
            ) { Text(btnLabel) }

            // Secondary: available after completion
            if (ui.canPickNextTarget) {
                OutlinedButton(
                    onClick = { onPickNextTarget?.invoke() },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                ) { Text("Set new target") }
            }
        }
    }

    // Blessing then celebration on 108
    if (ui.showCelebration) {
        var showBlessing by remember { mutableStateOf(true) }
        if (showBlessing) {
            BlessingCardDialog(onClose = { showBlessing = false })
        } else {
            AlertDialog(
                onDismissRequest = { vm.clearCelebration() },
                title = { Text("Batch Completed!") },
                text = { Text("You completed ${ui.currentBatchNumber - 1} batches of 108!") },
                confirmButton = { TextButton(onClick = { vm.clearCelebration() }) { Text("OK") } }
            )
        }
    }
    @Composable
    fun CertificateGeneratingOverlay(
        step: Int,
        stepLabel: String
    ) {
        val progress = when (step) {
            1 -> 0.25f
            2 -> 0.70f
            3 -> 0.95f
            else -> 0f
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Generating your certificate",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Step $step of 3",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = stepLabel.ifBlank { "Please wait..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Sri Rama Jayam",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB87333)
                    )
                }
            }
        }
    }
    // Certificate generation overlay
    if (ui.isIssuingCertificate) {
        CertificateGeneratingOverlay(
            step = ui.certificateStep,
            stepLabel = ui.certificateStepLabel
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
private fun GridCell(
    filled: Boolean,
    height: Dp,
    text: String = ""
) {
    // Excel-like: sharp rectangle, thin border. Grid controls width.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(BorderStroke(1.dp, Color(0xFFE0E0E0)))
            .background(if (filled) Color(0xFFFFE8C7) else Color(0xFFFDF6EE)),
        contentAlignment = Alignment.Center
    ) {
        if (text.isNotBlank()) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2, // allow wrap; Indic scripts can be taller
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BlessingCardDialog(onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Blessings") },
        text = { Text("May Sri Rama’s grace be with you. Keep going!") },
        confirmButton = { TextButton(onClick = onClose) { Text("Continue") } }
    )
}

@HiltViewModel
class ReminderVm @Inject constructor(
    val scheduler: ReminderScheduler
) : androidx.lifecycle.ViewModel()

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
        true
    )
    dialog.show()
}
