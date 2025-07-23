// File: app/src/main/java/com/hindu/pooja/viewmodel/LoginViewModel.kt
package com.hindu.pooja.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                onResult(it.isSuccessful)
                _isLoggedIn.value = it.isSuccessful
            }
    }

    fun startPhoneNumberVerification(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential) { success ->
                        _isLoggedIn.value = success
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("LoginViewModel", "Verification failed", e)
                    onFailed(e.message ?: "Verification failed")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    onCodeSent()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String, onResult: (Boolean) -> Unit) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId ?: "", otp)
        signInWithPhoneAuthCredential(credential, onResult)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, onResult: (Boolean) -> Unit) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
                _isLoggedIn.value = task.isSuccessful
            }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}
