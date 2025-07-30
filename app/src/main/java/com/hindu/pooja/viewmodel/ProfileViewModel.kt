package com.hindu.pooja.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _photoUrl = MutableStateFlow("")
    val photoUrl: StateFlow<String> = _photoUrl

    fun onFullNameChanged(newName: String) {
        _fullName.value = newName
    }

    fun onPhoneChanged(newPhone: String) {
        _phone.value = newPhone
    }

    fun loadUserProfile(
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val userId = auth.currentUser?.uid ?: return onFailure()

        firestore.collection("userProfiles").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _fullName.value = doc.getString("fullName") ?: ""
                    _email.value = doc.getString("email") ?: auth.currentUser?.email.orEmpty()
                    _phone.value = doc.getString("phone") ?: ""
                    _photoUrl.value = doc.getString("photoUrl") ?: ""
                    onSuccess()
                } else {
                    onFailure()
                }
            }
            .addOnFailureListener { onFailure() }
    }

    fun saveProfile(
        photoUrl: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val user = auth.currentUser ?: return onFailure()

        val profileData = mapOf(
            "fullName" to _fullName.value,
            "email" to _email.value.ifBlank { user.email ?: "" },
            "phone" to _phone.value,
            "photoUrl" to photoUrl
        )

        firestore.collection("userProfiles").document(user.uid)
            .set(profileData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }
}
