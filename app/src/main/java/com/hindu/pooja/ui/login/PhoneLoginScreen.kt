package com.hindu.pooja.ui.login

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hindu.pooja.utils.SessionManager
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun PhoneLoginScreen(
    onOtpVerified: () -> Unit
) {
    val context = LocalContext.current
    val auth = Firebase.auth

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isOtpSent by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    SessionManager.saveSession(
                        context = context,
                        onSuccess = onOtpVerified,
                        onError = {
                            Toast.makeText(context, "Session save failed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Auto-login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("PhoneAuth", "Verification failed", e)
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
            isOtpSent = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Phone Login", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Enter Phone Number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        if (isOtpSent) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Enter OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (!isOtpSent) {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber.trim())
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(context as Activity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            } else {
                isVerifying = true
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        SessionManager.saveSession(
                            context = context,
                            onSuccess = onOtpVerified,
                            onError = {
                                Toast.makeText(context, "Session save failed", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "OTP verification failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener { isVerifying = false }
            }
        }) {
            Text(if (!isOtpSent) "Send OTP" else if (isVerifying) "Verifying..." else "Verify OTP")
        }
    }
}
