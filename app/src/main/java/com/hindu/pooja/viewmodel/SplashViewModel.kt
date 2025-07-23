package com.hindu.pooja.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class SplashNavigation {
    object ToLogin : SplashNavigation()
    object ToPersonalDetails : SplashNavigation()
    object ToHome : SplashNavigation()
}

class SplashViewModel : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigation?>(null)
    val navigationState: StateFlow<SplashNavigation?> = _navigationState

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    init {
        checkLoginAndProfile()
    }

    private fun checkLoginAndProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _navigationState.value = SplashNavigation.ToLogin
        } else {
            val uid = currentUser.uid
            db.collection("userProfiles").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        _navigationState.value = SplashNavigation.ToHome
                    } else {
                        _navigationState.value = SplashNavigation.ToPersonalDetails
                    }
                }
                .addOnFailureListener {
                    _navigationState.value = SplashNavigation.ToLogin
                }
        }
    }
}
