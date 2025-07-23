package com.hindu.pooja.ui.login

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hindu.pooja.utils.SessionManager
import com.hindu.pooja.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onPhoneLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        SessionManager.saveSession(
                            context = context,
                            onSuccess = {
                                onLoginSuccess() // ✅ Navigate to Splash to determine next step
                            },
                            onError = {
                                isLoading = false
                                Toast.makeText(context, "Session save failed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Firebase sign-in failed", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Google account not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            isLoading = false
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("102522233118-tv7ksvbkfdcbnd6nfaful24jgpgaccmr.apps.googleusercontent.com") // ✅ Use your real Web Client ID
                .requestEmail()
                .build()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }) {
                Text("Sign in with Google")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                onPhoneLoginClick()
            }) {
                Text("Sign in with Phone (OTP)")
            }
        }
    }
}
