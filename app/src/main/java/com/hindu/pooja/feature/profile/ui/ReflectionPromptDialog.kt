package com.hindu.pooja.feature.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple dialog to capture a reflection after a milestone.
 */
@Composable
fun ReflectionPromptDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val text = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What has this journey meant to you?") },
        text = {
            Column {
                OutlinedTextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    modifier = Modifier.padding(top = 4.dp),
                    singleLine = false,
                    minLines = 3,
                    label = { Text("Your reflection") }
                )
                Spacer(Modifier.height(8.dp))
                Text("This will be saved to your Profile → Reflections.")
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(text.value.trim())
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
