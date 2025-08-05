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
import androidx.compose.ui.graphics.Color
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
import com.hindu.pooja.utils.SessionManager
import com.hindu.pooja.viewmodel.LoginViewModel
import com.hindu.pooja.ui.personal.PersonalDetailsScreen
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
    onPhoneLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var isLoading by remember { mutableStateOf(false) }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val db = FirebaseFirestore.getInstance()

                        val userRef = db.collection("userProfiles").document(user?.uid ?: return@addOnSuccessListener)

                        userRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // New user → route to PersonalDetailsScreen
                                navController.navigate(Screen.PersonalDetails.route) {
                                    popUpTo(0)
                                }
                                return@addOnSuccessListener
                            }

                            // Existing user → proceed
                            SessionManager.saveSession(
                                context = context,
                                onSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0)
                                    }
                                },
                                onError = {
                                    isLoading = false
                                    Toast.makeText(context, "Session save failed", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }.addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Firestore error: ${it.message}", Toast.LENGTH_LONG).show()
                        }
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(this.maxHeight),
            contentScale = ContentScale.FillBounds
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.85f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login to Spiritual360", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            isLoading = true
                            googleSignInClient.signOut().addOnCompleteListener {
                                val signInIntent = googleSignInClient.signInIntent
                                launcher.launch(signInIntent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in with Google")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onPhoneLoginClick() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in with Phone (OTP)")
                    }
                }
            }
        }
    }
}
