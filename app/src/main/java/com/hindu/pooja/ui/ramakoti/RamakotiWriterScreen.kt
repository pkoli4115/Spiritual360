@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.hindu.pooja.ui.ramakoti

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hindu.pooja.dev.DevHelpers
import com.hindu.pooja.viewmodel.RamakotiViewModel

@Composable
fun RamakotiWriterScreen(
    viewModel: RamakotiViewModel = hiltViewModel()
) {
    val completed by viewModel.completed.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        val devVisible = DevHelpers.DEV_VISIBLE

        Button(
            onClick = { viewModel.fillNextCellWithMantra() },
            enabled = completed < 108,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(56.dp)
                .then(
                    if (devVisible)
                        Modifier.combinedClickable(
                            onClick = { viewModel.fillNextCellWithMantra() },
                            onLongClick = { DevHelpers.fillRemaining(viewModel) }
                        )
                    else Modifier
                )
        ) {
            Text("Jai Shri Ram")
        }

        Spacer(Modifier.height(12.dp))
        Text("Progress: $completed / 108")

        if (showCelebration) {
            Text("🎉 Jai Shri Ram! Completed 108! 🎉")
        }
    }
}
