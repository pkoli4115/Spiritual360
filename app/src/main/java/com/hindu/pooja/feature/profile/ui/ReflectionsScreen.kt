package com.hindu.pooja.feature.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hindu.pooja.feature.profile.ReflectionsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReflectionsScreen(
    modifier: Modifier = Modifier,
    vm: ReflectionsViewModel = viewModel()
) {
    val ui = vm.ui.collectAsState().value
    val showDialog = remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    @OptIn(ExperimentalMaterial3Api::class)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reflections") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (ui.error != null) {
                Text(ui.error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (ui.loading && ui.items.isEmpty()) {
                CircularProgressIndicator()
            } else if (ui.items.isEmpty()) {
                Text("No reflections yet. Tap + to add one.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ui.items) { r ->
                        Card {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    r.text,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    r.createdAtMs?.let { dateFmt.format(Date(it)) } ?: "—",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { vm.deleteReflection(r.id) }) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog.value) {
        ReflectionPromptDialog(
            onDismiss = { showDialog.value = false },
            onSave = { text -> vm.addReflection(text) { showDialog.value = false } }
        )
    }
}
