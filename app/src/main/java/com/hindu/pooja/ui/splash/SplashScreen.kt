package com.hindu.pooja.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hindu.pooja.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val saffron = Color(0xFFFF9933)
    val onSaffron = Color(0xFF2B1E0A)

    // Navigate on success
    LaunchedEffect(state) {
        if (state is SplashState.Ok) {
            // tiny pause for a pleasant transition
            delay(300)
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Fallback auto-advance: if still "Checking" for too long, proceed (useful in Monitoring/debug)
    LaunchedEffect(Unit) {
        delay(1500) // 1.5s splash
        if (state is SplashState.Checking) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(saffron)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Center icon
        Image(
            painter = painterResource(id = R.drawable.applauncher),
            contentDescription = "eRamakoti",
            modifier = Modifier
                .size(172.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.Fit
        )

        // Error (only when failed)
        if (state is SplashState.Failed) {
            val msg = (state as SplashState.Failed).message
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 160.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Security check failed",
                    color = Color(0xFF7A0000),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = msg,
                    color = Color(0xFF7A0000),
                    fontSize = 14.sp
                )
            }
        }

        // Bottom “Powered by QTI Labs”
        PoweredByQtiLabsRow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            labelColor = onSaffron
        )
    }
}

@Composable
private fun PoweredByQtiLabsRow(
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Powered by", color = labelColor, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Image(
            painter = painterResource(id = R.drawable.`qtilabs`),
            contentDescription = "QTI Labs",
            modifier = Modifier
                .height(36.dp)
                .wrapContentWidth()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        val i = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            Uri.parse("https://qtilabs.com")
                        )
                        context.startActivity(i)
                    })
                },
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(4.dp))
        Text("Firebase & Android", color = labelColor.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}
