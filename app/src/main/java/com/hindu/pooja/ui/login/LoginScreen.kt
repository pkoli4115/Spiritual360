package com.hindu.pooja.ui.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.hindu.pooja.R
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.ui.theme.AshokaBlue
import com.hindu.pooja.ui.theme.Cream
import com.hindu.pooja.ui.theme.IndiaGreen
import com.hindu.pooja.ui.theme.Saffron
import com.hindu.pooja.viewmodel.LoginViewModel
import com.hindu.pooja.MainActivity // fb callback manager holder

@Composable
fun LoginScreen(navController: NavController) {
    val vm: LoginViewModel = hiltViewModel()
    LoginScreen(navController = navController, viewModel = vm)
}

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val context = LocalContext.current
    val activity = remember { findActivity(context) }
    val auth = remember { FirebaseAuth.getInstance() }
    var isLoading by remember { mutableStateOf(false) }

    // --- Google client ---
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
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(cred)
                    .addOnSuccessListener {
                        logProviders()
                        ensureMinimalProfileAndRoute(navController) { isLoading = false }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Google sign-in failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Google account not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            isLoading = false
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Facebook login ---
    val fbCallbackManager = remember { MainActivity.fbCallbackManager }
    DisposableEffect(Unit) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                isLoading = true
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        logProviders()
                        ensureMinimalProfileAndRoute(navController) { isLoading = false }
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        Toast.makeText(context, "Facebook sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            override fun onCancel() {
                Toast.makeText(context, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                Toast.makeText(context, "Facebook error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        LoginManager.getInstance().registerCallback(fbCallbackManager, callback)
        onDispose { /* no-op */ }
    }

    // ---------- UI ----------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Saffron, Cream)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🇮🇳 Flag (Compose)
            DrawIndianFlag(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(Modifier.height(20.dp))

            // App icon + (small professional caption)
            Image(
                painter = painterResource(id = R.drawable.applauncher),
                contentDescription = "Spiritual360 App Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(140.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Spiritual360",
                color = Color(0xFF6A1B09), // rich brown that pairs with saffron
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            // 🔒 Secured by Firebase
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Secured by Firebase",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Secured by Firebase",
                    color = Color(0xFF757575),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // Buttons container (glass card)
            Surface(
                color = Color.White.copy(alpha = 0.85f),
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Saffron)
                    } else {
                        // ---- Google button with icon ----
                        Button(
                            onClick = {
                                isLoading = true
                                googleClient.signOut().addOnCompleteListener {
                                    launcher.launch(googleClient.signInIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google logo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Continue with Google",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ---- Facebook button with icon ----
                        Button(
                            onClick = {
                                val act = activity
                                if (act == null) {
                                    Toast.makeText(context, "No activity context", Toast.LENGTH_SHORT).show()
                                } else {
                                    LoginManager.getInstance()
                                        .logInWithReadPermissions(act, listOf("email", "public_profile"))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_facebook_logo),
                                    contentDescription = "Facebook logo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Continue with Facebook",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Footer (Powered by QTI Labs)
            Column(
                modifier = Modifier.padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Powered by", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Image(
                    painter = painterResource(id = R.drawable.qtilabs),
                    contentDescription = "QTI Labs",
                    modifier = Modifier
                        .height(40.dp)
                        .clickable {
                            val i = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://qtilabs.com")
                            )
                            context.startActivity(i)
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/* ---------------- Flag drawing ---------------- */

@Composable
fun DrawIndianFlag(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stripe = size.height / 3f

        // Saffron, White, Green bands
        drawRoundRect(
            color = Saffron,
            topLeft = Offset(0f, 0f),
            size = Size(size.width, stripe),
            cornerRadius = CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(0f, stripe),
            size = Size(size.width, stripe),
            cornerRadius = CornerRadius(0f, 0f)
        )
        drawRoundRect(
            color = IndiaGreen,
            topLeft = Offset(0f, stripe * 2),
            size = Size(size.width, stripe),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // Ashoka Chakra
        val center = Offset(size.width / 2f, stripe + stripe / 2f)
        val radius = stripe / 2.5f
        drawCircle(color = AshokaBlue, radius = radius, center = center, style = Stroke(5f))
        for (i in 0 until 24) {
            val angle = Math.toRadians((i * 15).toDouble())
            val x = center.x + (radius - 6f) * kotlin.math.cos(angle).toFloat()
            val y = center.y + (radius - 6f) * kotlin.math.sin(angle).toFloat()
            drawLine(color = AshokaBlue, start = center, end = Offset(x, y), strokeWidth = 3f)
        }
    }
}

/* ---------------- helpers ---------------- */

private fun findActivity(context: Context): Activity? {
    var ctx: Context? = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun logProviders() {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val providers = user.providerData.joinToString { it.providerId }
    android.util.Log.d("AuthFlow", "Signed in as ${user.uid} via [$providers]")
}

private fun ensureMinimalProfileAndRoute(
    navController: NavController,
    onDone: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser ?: return onDone()
    val db = FirebaseFirestore.getInstance()
    val profile = mapOf(
        "uid" to user.uid,
        "displayName" to (user.displayName ?: ""),
        "email" to (user.email ?: ""),
        "photoUrl" to (user.photoUrl?.toString() ?: ""),
        "updatedAt" to com.google.firebase.Timestamp.now()
    )
    db.collection("users").document(user.uid)
        .set(profile, SetOptions.merge())
        .addOnCompleteListener {
            onDone()
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
}
