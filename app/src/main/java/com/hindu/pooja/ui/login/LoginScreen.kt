// app/src/main/java/com/hindu/pooja/ui/login/LoginScreen.kt
package com.hindu.pooja.ui.login

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hindu.pooja.R
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var isLoading by remember { mutableStateOf(false) }

    val googleClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // TODO: replace with your real web client ID
                .requestIdToken("102522233118-tv7ksvbkfdcbnd6nfaful24jgpgaccmr.apps.googleusercontent.com")
                .requestEmail()
                .build()
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            if (account != null) {
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(cred)
                    .addOnSuccessListener {
                        // Decide route based on whether user profile exists
                        ensureMinimalProfileAndRoute(navController) { isLoading = false }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Sign-in failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Google account not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            isLoading = false
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), tonalElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Login to HinduPooja", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(24.dp))
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                isLoading = true
                                googleClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleClient.signInIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Sign in with Google") }
                    }
                }
            }
        }
    }
}

/** Route: if profile doc exists → Home; else → first_profile to capture Name/Email/Mobile. */
private fun ensureMinimalProfileAndRoute(
    navController: androidx.navigation.NavController,
    onDone: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return onDone()
    val ref = FirebaseFirestore.getInstance().collection("userProfiles").document(uid)

    ref.get()
        .addOnSuccessListener { snap ->
            onDone()
            if (!snap.exists()) {
                navController.navigate("first_profile")
            } else {
                // Backfill profileId if missing
                val existingPid = snap.getString("profileId").orEmpty()
                if (existingPid.isBlank()) {
                    val pid = "HP-" + java.util.UUID.randomUUID().toString().substring(0, 8).uppercase()
                    ref.update("profileId", pid).addOnCompleteListener {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                } else {
                    navController.navigate(Screen.Home.route) { popUpTo(0) }
                }
            }
        }
        .addOnFailureListener {
            onDone()
            navController.navigate(Screen.Home.route) { popUpTo(0) }
        }
}
