package com.hindu.pooja.ui.personal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!loaded) {
            viewModel.loadProfile(onSuccess = { loaded = true }, onFailure = { loaded = true })
        }
    }

    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val pid by viewModel.profileId.collectAsState()
    val photoUrl by viewModel.profilePictureUrl.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val formValid by viewModel.formValid.collectAsState()
    val err by viewModel.lastError.collectAsState()

    if (saveSuccess) { viewModel.resetSaveState(); onSaveSuccess() }

    val ctx = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.setProfilePictureUri(uri) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Edit Profile", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text("Profile ID: $pid", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(16.dp))

            if (photoUrl.isNotBlank() || viewModel.profilePictureUri.value != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        viewModel.profilePictureUri.value ?: photoUrl
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                Text("Change Photo")
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { viewModel.fullName.value = it; viewModel.validateForm() },
                label = { Text("Name *") },
                isError = fullName.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.email.value = it; viewModel.validateForm() },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.phone.value = it; viewModel.validateForm() },
                label = { Text("Mobile") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!err.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(err!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.saveProfileWithPhoto(
                        context = ctx,
                        onSuccess = { scope.launch { snackbarHostState.showSnackbar("Saved!") } },
                        onFailure = { scope.launch { snackbarHostState.showSnackbar(err ?: "Save failed") } }
                    )
                },
                enabled = formValid && !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Save") }
        }
    }
}
