package com.hindu.pooja.feature.ramakoti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hindu.pooja.feature.ramakoti.RamakotiViewModel
import com.hindu.pooja.ui.ramakoti.CelebrationOverlay
import com.hindu.pooja.ui.ramakoti.ProgressBar108

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamakotiScreen(
    modifier: Modifier = Modifier,
    vm: RamakotiViewModel = viewModel()
) {
    val ui = vm.ui.collectAsState().value
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        com.hindu.pooja.RamakotiAudioKeyBus.setActive(true)
        com.hindu.pooja.RamakotiAudioKeyBus.events.collect { e ->
            if (e == com.hindu.pooja.VolumeEvent.UP) vm.tickNext()
        }
    }
    DisposableEffect(Unit) { onDispose { com.hindu.pooja.RamakotiAudioKeyBus.setActive(false) } }

    LaunchedEffect(ui.showCelebration) {
        if (ui.showCelebration) snackbar.showSnackbar("🎉 Completed 108! Jai Sri Ram!")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ramakoti") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(modifier = modifier.padding(padding).fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total: ${ui.totalCount}   •   Batch: ${ui.currentBatchCount}/108   •   Crore: ${ui.currentCrore}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                val filled = ui.currentBatchCount.coerceIn(0, 108)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(9),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = true
                ) {
                    items(108) { idx ->
                        val isFilled = idx < filled
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.primaryContainer
                                    else Color(0xFFF3EFE9)
                                )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                ProgressBar108(progress = ui.currentBatchCount)

                Spacer(Modifier.height(10.dp))
                val life = ui.lifetimeCount.coerceAtLeast(0)
                val croreTarget = 100_000_000f
                val lifeProgress = (life / croreTarget).coerceIn(0f, 1f)
                Text(
                    text = "Lifetime: $life",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                LinearProgressIndicator(
                    progress = lifeProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { vm.tickNext() }, modifier = Modifier.weight(1f)) {
                        Text("Jai Shri Ram")
                    }

                    OutlinedButton(
                        onClick = { vm.exportGrid() }   // ← updated
                    ) { Text("Export Grid PDF") }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val milestone = "Completed 1 Crore Sri Rama Namas"
                            vm.generateCertificate(milestone)
                        }
                    ) { Text("Generate Certificate") }

                    if (ui.canStartSecondCrore) {
                        Button(onClick = { vm.startSecondCrore() }) {
                            Text("Start Second Crore")
                        }
                    }
                }

                ui.error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (ui.showCelebration) {
                CelebrationOverlay(onDismiss = { vm.onCelebrationShown() })
            }
        }
    }
}
