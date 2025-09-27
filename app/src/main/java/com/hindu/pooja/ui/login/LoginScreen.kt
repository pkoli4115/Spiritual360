package com.hindu.pooja.ui.login

import android.app.Activity
import android.util.Log
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
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hindu.pooja.R
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.viewmodel.LoginViewModel

// Facebook SDK
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.hindu.pooja.MainActivity

/** Wrapper to avoid ambiguous default parameter calls */
@Composable
fun LoginScreen(
    navController: NavController
) {
    val vm: LoginViewModel = hiltViewModel()
    LoginScreen(navController = navController, viewModel = vm)
}

/** Main composable – explicit VM param (no default) */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val context = LocalContext.current
    val activity = remember { activityFrom(context) }
    val auth = FirebaseAuth.getInstance()
    var isLoading by remember { mutableStateOf(false) }

    // --- Google Sign-In client ---
    val googleClient = remember {
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
                Log.d("AuthFlow", "Google login SUCCESS: account=${account.email}")
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(cred)
                    .addOnSuccessListener {
                        Log.d("AuthFlow", "Firebase Auth with Google OK, uid=${it.user?.uid}")
                        logProviders()
                        ensureMinimalProfileAndRoute(navController) { isLoading = false }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Log.e("AuthFlow", "Firebase Auth with Google FAILED", it)
                        Toast.makeText(context, "Google sign-in failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Google account not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            isLoading = false
            Log.e("AuthFlow", "Google Sign-In exception", e)
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Facebook Login setup ---
    val fbCallbackManager = remember { MainActivity.fbCallbackManager }

    DisposableEffect(Unit) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d("AuthFlow", "Facebook login SUCCESS: accessToken=${result.accessToken.userId}")
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        Log.d("AuthFlow", "Firebase Auth with Facebook OK, uid=${it.user?.uid}")
                        logProviders()
                        ensureMinimalProfileAndRoute(navController) { isLoading = false }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        Log.e("AuthFlow", "Firebase Auth with Facebook FAILED", e)
                        Toast.makeText(context, "Facebook sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            override fun onCancel() {
                Log.d("AuthFlow", "Facebook login CANCELLED")
                Toast.makeText(context, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                Log.e("AuthFlow", "Facebook login ERROR", error)
                Toast.makeText(context, "Facebook error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        LoginManager.getInstance().registerCallback(fbCallbackManager, callback)
        onDispose { }
    }

    // --- UI ---
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
                    Text("Login to Spiritual360", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(24.dp))
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        // Google
                        Button(
                            onClick = {
                                isLoading = true
                                Log.d("AuthFlow", "Google button clicked → launching intent")
                                googleClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleClient.signInIntent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Continue with Google") }

                        Spacer(Modifier.height(12.dp))

                        // Facebook
                        Button(
                            onClick = {
                                val act = activity
                                if (act == null) {
                                    Toast.makeText(context, "No activity context", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.d("AuthFlow", "Facebook button clicked → starting FB login flow")
                                    LoginManager.getInstance()
                                        .logInWithReadPermissions(act, listOf("email", "public_profile"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Continue with Facebook") }
                    }
                }
            }
        }
    }
}

/** Upsert a minimal profile in `users/{uid}` every login, then navigate to Home. */
private fun ensureMinimalProfileAndRoute(
    navController: NavController,
    onDone: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return onDone()
    val user = auth.currentUser
    val ref = FirebaseFirestore.getInstance().collection("users").document(uid)

    val provider = when {
        user?.providerData?.any { it.providerId.contains("facebook") } == true -> "Facebook"
        user?.providerData?.any { it.providerId.contains("google") } == true -> "Google"
        else -> "Email/Password"
    }

    val freshPid = "HP-" + java.util.UUID.randomUUID().toString().substring(0, 8).uppercase()
    val base = hashMapOf(
        "uid" to uid,
        "profileId" to freshPid, // will keep existing if present
        "fullName" to (user?.displayName ?: ""),
        "email" to (user?.email ?: ""),
        "phone" to (user?.phoneNumber ?: ""),
        "photoUrl" to (user?.photoUrl?.toString() ?: ""),
        "loginProvider" to provider
    )

    ref.get()
        .addOnSuccessListener { snap ->
            val data = if (snap.exists()) {
                val existingPid = snap.getString("profileId")
                if (!existingPid.isNullOrBlank()) base.apply { put("profileId", existingPid) } else base
            } else base

            ref.set(data, SetOptions.merge())
                .addOnCompleteListener {
                    onDone()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
        }
        .addOnFailureListener {
            ref.set(base, SetOptions.merge())
                .addOnCompleteListener {
                    onDone()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
        }
}

private fun activityFrom(context: android.content.Context): Activity? =
    when (context) {
        is Activity -> context
        is android.content.ContextWrapper -> activityFrom(context.baseContext)
        else -> null
    }

/** Log which providers are linked to the current Firebase user */
private fun logProviders() {
    val user = FirebaseAuth.getInstance().currentUser
    user?.providerData?.forEach {
        Log.d("AuthFlow", "Provider: ${it.providerId}, UID=${it.uid}, email=${it.email}")
    }
}
