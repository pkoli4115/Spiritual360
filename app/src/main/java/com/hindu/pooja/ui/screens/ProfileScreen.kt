package com.hindu.pooja.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.hindu.pooja.viewmodel.BaseViewModel

@Composable
fun ProfileScreen(baseViewModel: BaseViewModel) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = "Profile Section – Personal info and settings will appear here.",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(32.dp)
        )
    }
}
