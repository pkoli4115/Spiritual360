package com.hindu.pooja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!loaded) viewModel.loadProfile(onSuccess = { loaded = true }, onFailure = { loaded = true })
    }

    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val pid by viewModel.profileId.collectAsState()
    val photoUrl by viewModel.profilePictureUrl.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val err by viewModel.lastError.collectAsState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            if (photoUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            Text("Profile ID: $pid", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text("Name: $fullName")
            Text("Email: $email")
            Text("Mobile: $phone")

            if (!err.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(err!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) { Text("Edit Profile") }

                OutlinedButton(
                    onClick = { navController.navigate("login") { popUpTo(0) } },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) { Text("Logout") }
            }
        }
    }
}
