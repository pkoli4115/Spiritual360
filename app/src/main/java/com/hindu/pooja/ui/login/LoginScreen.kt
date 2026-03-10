package com.hindu.pooja.ui.login
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
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
import com.google.firebase.Timestamp
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
import com.hindu.pooja.MainActivity
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlin.math.cos
import kotlin.math.sin
import java.security.MessageDigest

private const val TAG = "AuthFlow"

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

    // -------- One-time environment summary + App Check token ----------
    LaunchedEffect(Unit) {
        logFacebookKeyHashes(context)
        // Quick App Check token probe (non-forced)
        FirebaseAppCheck.getInstance().getToken(false)
            .addOnSuccessListener { r ->
                val token = r.token ?: ""
                Log.i(TAG, "AppCheck token (${token.length} chars): ${token.take(20)}...")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "AppCheck token fetch failed: ${e.javaClass.simpleName}: ${e.message}")
            }
    }

    // ---------------- Google sign-in ----------------
    val googleClient = remember {
        val webClientId = context.getString(R.string.default_web_client_id)
        Log.d(TAG, "Google client init with webClientId present=${webClientId.isNotBlank()} value='${safeEllipsize(webClientId)}'")
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google launcher returned: resultCode=${result.resultCode} data=${result.data != null}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            Log.d(TAG, "GoogleSignIn task success=${account != null}, email='${account?.email}', id=${account?.id}")
            if (account != null) {
                val idTokenPresent = !account.idToken.isNullOrBlank()
                Log.d(TAG, "Google idToken present=$idTokenPresent len=${account.idToken?.length ?: 0}")
                val cred = GoogleAuthProvider.getCredential(account.idToken, null)
                isLoading = true
                auth.signInWithCredential(cred)
                    .addOnSuccessListener {
                        Log.i(TAG, "Firebase signInWithCredential(Google) SUCCESS uid=${auth.currentUser?.uid}")
                        logProviders()
                        ensureMinimalProfileAndRoute(navController) { isLoading = false }
                    }
                    .addOnFailureListener { err ->
                        isLoading = false
                        Log.e(TAG, "Firebase signInWithCredential(Google) FAILED: ${err.javaClass.simpleName}: ${err.message}", err)
                        Toast.makeText(context, "Google login failed: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.w(TAG, "Google account is null (user canceled?)")
                Toast.makeText(context, "Google sign-in cancelled", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            isLoading = false
            Log.e(TAG, "GoogleSignIn task exception: ${e.javaClass.simpleName}: ${e.message}", e)
            Toast.makeText(context, "Google error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- Facebook login ----------------
    val fbCallbackManager = remember { MainActivity.fbCallbackManager }
    DisposableEffect(Unit) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "Facebook onSuccess: token present=${result.accessToken?.token?.isNotBlank() == true}")
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                isLoading = true
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener {
                        Log.i(TAG, "Firebase signInWithCredential(Facebook) SUCCESS uid=${auth.currentUser?.uid}")
                        logProviders()
                        ensureMinimalProfileAndRoute(navController) { isLoading = false }
                    }
                    .addOnFailureListener { err ->
                        isLoading = false
                        Log.e(TAG, "Firebase signInWithCredential(Facebook) FAILED: ${err.javaClass.simpleName}: ${err.message}", err)
                        Toast.makeText(context, "Facebook login failed: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancel() {
                Log.w(TAG, "Facebook login cancelled")
                Toast.makeText(context, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "Facebook login error: ${error.javaClass.simpleName}: ${error.message}", error)
                Toast.makeText(context, "Facebook error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d(TAG, "Registering Facebook callback")
        LoginManager.getInstance().registerCallback(fbCallbackManager, callback)
        onDispose {
            Log.d(TAG, "LoginScreen disposed (Facebook callback remains registered in CallbackManager)")
        }
    }
// ---------------- UI ----------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Saffron, Cream)))
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "bannerGlow")

        val glowAlpha1 by infiniteTransition.animateFloat(
            initialValue = 0.14f,
            targetValue = 0.34f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha1"
        )

        val glowAlpha2 by infiniteTransition.animateFloat(
            initialValue = 0.08f,
            targetValue = 0.20f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha2"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DrawIndianFlag(Modifier.fillMaxWidth().height(100.dp))

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft divine aura
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F).copy(alpha = glowAlpha1),
                                    Color(0xFFFFE082).copy(alpha = glowAlpha2),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Sparkles around banner
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    val sparkles = listOf(
                        Offset(size.width * 0.14f, size.height * 0.20f),
                        Offset(size.width * 0.86f, size.height * 0.18f),
                        Offset(size.width * 0.12f, size.height * 0.78f),
                        Offset(size.width * 0.88f, size.height * 0.70f),
                        Offset(size.width * 0.52f, size.height * 0.10f)
                    )

                    sparkles.forEachIndexed { index, center ->
                        val alpha = if (index % 2 == 0) glowAlpha1 else glowAlpha2

                        drawCircle(
                            color = Color(0xFFFFF3B0).copy(alpha = alpha),
                            radius = 7f,
                            center = center
                        )

                        drawLine(
                            color = Color(0xFFFFF8DC).copy(alpha = alpha),
                            start = Offset(center.x - 12f, center.y),
                            end = Offset(center.x + 12f, center.y),
                            strokeWidth = 2f
                        )

                        drawLine(
                            color = Color(0xFFFFF8DC).copy(alpha = alpha),
                            start = Offset(center.x, center.y - 12f),
                            end = Offset(center.x, center.y + 12f),
                            strokeWidth = 2f
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.applauncher),
                    contentDescription = "Ramakoti Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Digital Sri Rama Nama Writing",
                color = Color(0xFF7A4A1A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Jai Shri Ram ✨",
                color = Color(0xFF8B4513),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(22.dp))

            Surface(
                color = Color.White.copy(alpha = 0.90f),
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(18.dp))
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Saffron)
                    } else {
                        Button(
                            onClick = {
                                Log.d(TAG, "Google button clicked → signOut then launch signIn intent")
                                isLoading = true
                                googleClient.signOut().addOnCompleteListener {
                                    Log.d(TAG, "Google signOut complete, launching intent")
                                    googleLauncher.launch(googleClient.signInIntent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Continue with Google", color = Color.Black)
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                Log.d(TAG, "Facebook button clicked → LoginManager.logInWithReadPermissions")
                                val act = activity
                                if (act != null) {
                                    LoginManager.getInstance()
                                        .logInWithReadPermissions(act, listOf("email", "public_profile"))
                                } else {
                                    Log.e(TAG, "No Activity context found for Facebook login")
                                    Toast.makeText(context, "No activity context", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_facebook_logo),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Continue with Facebook", color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Text("Powered by", style = MaterialTheme.typography.labelLarge)

            Image(
                painter = painterResource(id = R.drawable.qtilabs),
                contentDescription = null,
                modifier = Modifier
                    .height(40.dp)
                    .clickable {
                        Log.d(TAG, "Visit QTI Labs clicked")
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://qtilabs.com"))
                        )
                    }
            )
        }
    }
}

/* --------------------------- Helpers --------------------------- */

@Composable
fun DrawIndianFlag(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stripe = size.height / 3f

        drawRoundRect(Saffron, Offset.Zero, Size(size.width, stripe), CornerRadius(16f))
        drawRect(Color.White, Offset(0f, stripe), Size(size.width, stripe))
        drawRoundRect(IndiaGreen, Offset(0f, stripe * 2), Size(size.width, stripe), CornerRadius(16f))

        val c = Offset(size.width / 2, stripe * 1.5f)
        val r = stripe / 2.5f
        drawCircle(AshokaBlue, r, c, style = Stroke(5f))
        repeat(24) {
            val angle = Math.toRadians((it * 15).toDouble())
            drawLine(
                AshokaBlue,
                c,
                Offset(
                    c.x + (r - 6) * cos(angle).toFloat(),
                    c.y + (r - 6) * sin(angle).toFloat()
                ),
                3f
            )
        }
    }
}

private fun findActivity(context: Context): Activity? {
    var ctx: Context? = context
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun logProviders() {
    val u = FirebaseAuth.getInstance().currentUser
    if (u == null) {
        Log.w(TAG, "logProviders: user=null")
        return
    }
    val providers = u.providerData.joinToString { it.providerId }
    Log.d(TAG, "Signed in: uid=${u.uid} providers=$providers email=${u.email} phone=${u.phoneNumber}")
}

private fun ensureMinimalProfileAndRoute(nav: NavController, done: () -> Unit) {
    val u = FirebaseAuth.getInstance().currentUser
    if (u == null) {
        Log.w(TAG, "ensureMinimalProfileAndRoute: user=null")
        done()
        return
    }

    val doc = FirebaseFirestore.getInstance().collection("users").document(u.uid)
    val data = mapOf(
        "uid" to u.uid,
        "displayName" to (u.displayName ?: ""),
        "email" to (u.email ?: ""),
        "photoUrl" to (u.photoUrl?.toString() ?: ""),
        "updatedAt" to Timestamp.now()
    )

    Log.d(TAG, "Writing minimal profile for uid=${u.uid}")

    doc.set(data, SetOptions.merge())
        .addOnSuccessListener {
            Log.i(TAG, "Profile write SUCCESS → navigate Home")
            done()
            nav.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Profile write FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
            done()
            nav.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
}
/* ---------- Extra diagnostics ---------- */

private fun logFacebookKeyHashes(context: Context) {
    try {
        val pm = context.packageManager
        val pkg = context.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val info = pm.getPackageInfo(
                pkg,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )
            val signingInfo = info.signingInfo
            val signatures = signingInfo?.apkContentsSigners
            if (signatures.isNullOrEmpty()) {
                Log.w("FacebookKeyHash", "No signatures found (API 33+). signingInfo=$signingInfo")
            } else {
                for (sig in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(sig.toByteArray())
                    Log.d("FacebookKeyHash", Base64.encodeToString(md.digest(), Base64.NO_WRAP))
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = info.signingInfo
            val signatures = signingInfo?.apkContentsSigners
            if (signatures.isNullOrEmpty()) {
                Log.w("FacebookKeyHash", "No signatures found (API 28-32). signingInfo=$signingInfo")
            } else {
                for (sig in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(sig.toByteArray())
                    Log.d("FacebookKeyHash", Base64.encodeToString(md.digest(), Base64.NO_WRAP))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            val signatures = info.signatures
            if (signatures == null || signatures.isEmpty()) {
                Log.w("FacebookKeyHash", "No legacy signatures found (< API 28)")
            } else {
                @Suppress("DEPRECATION")
                for (sig in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(sig.toByteArray())
                    Log.d("FacebookKeyHash", Base64.encodeToString(md.digest(), Base64.NO_WRAP))
                }
            }
        }
    } catch (e: Exception) {
        Log.e("KeyHashError", "Key hash gen failed", e)
    }
}

private fun safeEllipsize(s: String?, keep: Int = 6): String {
    if (s.isNullOrBlank()) return ""
    return if (s.length <= keep * 2) s else "${s.take(keep)}…${s.takeLast(keep)}"
}
