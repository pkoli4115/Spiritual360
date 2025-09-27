// app/src/main/java/com/hindu/pooja/ui/personal/FirstTimeProfileScreen.kt
package com.hindu.pooja.ui.personal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@Composable
fun FirstTimeProfileScreen(
    navController: NavController,
    onCompletedRoute: String
) {
    val user = FirebaseAuth.getInstance().currentUser
    val prefillName = user?.displayName.orEmpty()
    val prefillEmail = user?.email.orEmpty()

    var name by remember { mutableStateOf(prefillName) }
    var email by remember { mutableStateOf(prefillEmail) } // read-only
    var mobile by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Set up your profile", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                isError = name.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email (from Google)") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it.filter { ch -> ch.isDigit() } },
                label = { Text("Mobile *") },
                isError = mobile.isBlank(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine = true
            )

            if (!err.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(err!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val uid = user?.uid
                    if (uid.isNullOrBlank()) { err = "No user"; return@Button }
                    if (name.isBlank() || mobile.isBlank()) { err = "Please fill required fields"; return@Button }

                    isSaving = true; err = null
                    val pid = "HP-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
                    val data = mapOf(
                        "fullName" to name.trim(),
                        "email" to email.trim(),
                        "phone" to mobile.trim(),
                        "profileId" to pid
                    )
                    FirebaseFirestore.getInstance()
                        .collection("userProfiles").document(uid)
                        .set(data)
                        .addOnSuccessListener {
                            isSaving = false
                            navController.navigate(onCompletedRoute) { popUpTo(0) }
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            err = e.message ?: "Save failed"
                        }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Continue")
            }
        }
    }
}
