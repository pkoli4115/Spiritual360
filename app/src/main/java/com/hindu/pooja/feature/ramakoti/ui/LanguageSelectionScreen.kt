@file:OptIn(ExperimentalMaterial3Api::class)

package com.hindu.pooja.feature.ramakoti.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.feature.ramakoti.data.LanguagePreferenceManager
import com.hindu.pooja.ui.ramakoti.LanguageChipRow
import com.hindu.pooja.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val langMgr = remember { LanguagePreferenceManager(context) }
    val scope = rememberCoroutineScope()

    val selected = remember { mutableStateOf("en") }

    // Pre-select the current user's stored language (if any)
    LaunchedEffect(Unit) {
        val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
        val stored: String = langMgr.languageFlowFor(uid).first()   // "" if none
        selected.value = stored.ifBlank { "en" }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Language") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Choose your preferred language", style = MaterialTheme.typography.titleMedium)

            // Your chips for TE/HI/EN
            LanguageChipRow(
                language = selected.value,
                onChange = { code -> selected.value = code }
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    scope.launch {
                        val uid: String? = FirebaseAuth.getInstance().currentUser?.uid
                        // ✅ per-user save
                        langMgr.setLanguageFor(uid, selected.value)

                        // Go back to Ramakoti entry; guard will allow through for this user
                        navController.navigate(Screen.Ramakoti.route) {
                            popUpTo("ramakoti/language") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            ) { Text("Continue") }
        }
    }
}
