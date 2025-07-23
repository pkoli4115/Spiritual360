package com.hindu.pooja.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuWithSearch(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(TextFieldValue(selectedOption)) }
    val focusManager = LocalFocusManager.current

    Column {
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                expanded = true
            },
            label = { Text("Select") },
            trailingIcon = {
                Row {
                    if (searchText.text.isNotEmpty()) {
                        IconButton(onClick = {
                            searchText = TextFieldValue("")
                            onOptionSelected("")
                            expanded = false
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) expanded = true
                },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            val filtered = options.filter {
                it.contains(searchText.text, ignoreCase = true)
            }

            if (filtered.isNotEmpty()) {
                filtered.take(20).forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            searchText = TextFieldValue(option)
                            onOptionSelected(option)
                            expanded = false
                            focusManager.clearFocus()
                        }
                    )
                }
            } else {
                DropdownMenuItem(
                    text = { Text("No match found") },
                    onClick = {}
                )
            }
        }
    }
}
