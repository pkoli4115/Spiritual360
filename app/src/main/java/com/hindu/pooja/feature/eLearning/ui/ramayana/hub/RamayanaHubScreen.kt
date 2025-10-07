package com.hindu.pooja.ui.ramayana.hub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RamayanaHubScreen(
    onOpenBala: () -> Unit,
    onOpenAyodhya: () -> Unit,
    onOpenRamakoti: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ramayana", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Button(onClick = onOpenBala, modifier = Modifier.fillMaxWidth()) {
            Text("బాలకాండము — కథ")
        }
        Spacer(Modifier.height(8.dp))

        Button(onClick = onOpenAyodhya, modifier = Modifier.fillMaxWidth()) {
            Text("అయోధ్యకాండము — కథ")
        }
        Spacer(Modifier.height(8.dp))

        Button(onClick = onOpenRamakoti, modifier = Modifier.fillMaxWidth()) {
            Text("రామకోటి")
        }
    }
}
