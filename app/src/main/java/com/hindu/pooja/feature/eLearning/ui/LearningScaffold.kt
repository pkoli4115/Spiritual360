package com.hindu.pooja.feature.elearning.ui

import androidx.compose.material3.*
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Stable M3 scaffold with a centered top bar and consistent back handling.
 * No experimental APIs used.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val activity = LocalContext.current as? Activity

    BackHandler {
        onBack?.invoke() ?: activity?.finish()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { onBack?.invoke() ?: activity?.finish() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = content
    )
}
