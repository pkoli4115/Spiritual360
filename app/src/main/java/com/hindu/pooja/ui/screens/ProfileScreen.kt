package com.hindu.pooja.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val fullName by viewModel.fullName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val profilePictureUrl by viewModel.profilePictureUrl.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile photo
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            if (profilePictureUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Default Profile",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Full Name: $fullName $lastName", style = MaterialTheme.typography.titleMedium)
        Text("Email: $email")
        Text("Phone: $phone")
        Text("DOB: $birthDate")

        Spacer(modifier = Modifier.height(8.dp))

        if (isPremium) {
            Text("🌟 Premium Member", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        } else {
            Button(onClick = { /* navController.navigate("billing") */ }) {
                Text("Upgrade to Premium")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { navController.navigate("edit_profile") },
                enabled = !isSaving
            ) {
                Text("Edit Profile")
            }

            OutlinedButton(
                onClick = {
                    // Logout logic
                    viewModel.logout() // You should add this function in ViewModel (auth.signOut)
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            ) {
                Text("Logout")
            }
        }
    }
}
