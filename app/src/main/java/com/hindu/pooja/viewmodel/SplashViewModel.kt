package com.hindu.pooja.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashState {
    data object Checking : SplashState()
    data object Ok : SplashState()
    data class Failed(val message: String) : SplashState()
}

class SplashViewModel : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Checking)
    val state: StateFlow<SplashState> = _state

    init {
        checkApp()
    }

    fun checkApp() {
        viewModelScope.launch {
            try {
                // Force refresh = false is fine; if you want to always hit server use true.
                FirebaseAppCheck.getInstance()
                    .getAppCheckToken(false)
                    .addOnSuccessListener {
                        _state.value = SplashState.Ok
                    }
                    .addOnFailureListener { e ->
                        _state.value = SplashState.Failed(
                            e.message ?: "App attestation failed."
                        )
                    }
            } catch (e: Exception) {
                _state.value = SplashState.Failed(e.message ?: "App attestation failed.")
            }
        }
    }
}
