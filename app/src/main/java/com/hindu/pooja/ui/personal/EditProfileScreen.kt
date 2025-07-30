package com.hindu.pooja.ui.personal

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.R
import com.hindu.pooja.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val fullName by viewModel.fullName.collectAsState(initial = "")
    val phone by viewModel.phone.collectAsState(initial = "")
    val photoUrl by viewModel.photoUrl.collectAsState(initial = "")

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val painter: Painter = if (photoUrl.isNotBlank()) {
            rememberAsyncImagePainter(model = photoUrl)
        } else {
            painterResource(id = R.drawable.ic_profile_placeholder)
        }

        Image(
            painter = painter,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .clickable {
                    // TODO: Add image picker logic here
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { viewModel.onFullNameChanged(it) },
            label = { Text(text = "Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { viewModel.onPhoneChanged(it) },
            label = { Text(text = "Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.saveProfile(
                        photoUrl = imageUri?.toString() ?: photoUrl,
                        onSuccess = {
                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        },
                        onFailure = {
                            Toast.makeText(context, "Error saving profile", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = fullName.isNotBlank() && phone.isNotBlank()
        ) {
            Text("Save Changes")
        }
    }
}
