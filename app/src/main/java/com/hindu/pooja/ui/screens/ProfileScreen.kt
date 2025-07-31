package com.hindu.pooja.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.R
import com.hindu.pooja.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Fetch from Firestore when the screen is first composed
    var loaded by remember { mutableStateOf(false) }
    if (!loaded) {
        LaunchedEffect(Unit) {
            viewModel.loadProfile(
                onSuccess = { loaded = true },
                onFailure = { loaded = true }
            )
        }
    }

    val fullName by viewModel.fullName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val profilePictureUrl by viewModel.profilePictureUrl.collectAsState()
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showImagePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setProfilePictureUri(uri)
        viewModel.saveProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Profile Image with Edit Pencil ---
        Box(
            modifier = Modifier
                .size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            val imagePainter = when {
                profilePictureUri != null -> rememberAsyncImagePainter(profilePictureUri)
                profilePictureUrl.isNotBlank() -> rememberAsyncImagePainter(profilePictureUrl)
                else -> painterResource(id = R.drawable.ic_profile_placeholder)
            }
            Image(
                painter = imagePainter,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            // Pencil Icon for editing
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile Photo",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = (-8).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { galleryLauncher.launch("image/*") }
                    .padding(6.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Profile Fields (display only) ---
        Text("Full Name: $fullName $lastName", style = MaterialTheme.typography.titleMedium)
        Text("Email: $email")
        Text("Phone: $phone")
        Text("DOB: $birthDate")

        Spacer(modifier = Modifier.height(16.dp))

        if (isPremium) {
            Text("🌟 Premium Member", style = MaterialTheme.typography.titleMedium)
        } else {
            Button(
                onClick = { navController.navigate("billing") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upgrade to Premium")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                navController.navigate("edit_profile")
            }) {
                Text("Edit Profile")
            }
            OutlinedButton(onClick = {
                viewModel.logout()
                navController.navigate("login") {
                    popUpTo(0)
                }
            }) {
                Text("Logout")
            }
        }
    }
}
