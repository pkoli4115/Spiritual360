package com.hindu.pooja.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val fullName = MutableStateFlow("")
    val email = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val profileId = MutableStateFlow("")
    val profilePictureUrl = MutableStateFlow("")
    val profilePictureUri = MutableStateFlow<Uri?>(null)

    val isSaving = MutableStateFlow(false)
    val saveSuccess = MutableStateFlow(false)
    val formValid = MutableStateFlow(false)
    val lastError = MutableStateFlow<String?>(null)

    private fun generateProfileId(): String =
        "HP-" + UUID.randomUUID().toString().substring(0, 8).uppercase()

    fun setProfilePictureUri(uri: Uri?) {
        profilePictureUri.value = uri
    }

    fun validateForm() {
        val emailOk = email.value.isBlank() ||
                Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(email.value)
        val phoneOk = phone.value.isBlank() ||
                Regex("^\\+?[0-9]{8,15}$").matches(phone.value)
        formValid.value = fullName.value.isNotBlank() && emailOk && phoneOk
    }

    fun loadProfile(onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return onFailure()
        firestore.collection("userProfiles").document(uid).get()
            .addOnSuccessListener { d ->
                if (!d.exists()) { onFailure(); return@addOnSuccessListener }
                fullName.value = d.getString("fullName") ?: ""
                email.value = d.getString("email") ?: ""
                phone.value = d.getString("phone") ?: ""
                profilePictureUrl.value = d.getString("profilePictureUrl") ?: ""
                val pid = d.getString("profileId").orEmpty()
                if (pid.isBlank()) {
                    val newId = generateProfileId()
                    profileId.value = newId
                    firestore.collection("userProfiles").document(uid)
                        .update("profileId", newId)
                } else profileId.value = pid
                validateForm()
                onSuccess()
            }
            .addOnFailureListener { e -> lastError.value = e.message; onFailure() }
    }

    fun saveProfileWithPhoto(
        context: Context,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val user = auth.currentUser ?: return onFailure()
        isSaving.value = true
        lastError.value = null

        val pid = if (profileId.value.isBlank()) generateProfileId() else profileId.value
        profileId.value = pid

        val saveData: (String) -> Unit = { photoUrl ->
            val data = mapOf(
                "fullName" to fullName.value,
                "email" to email.value,
                "phone" to phone.value,
                "profileId" to pid,
                "profilePictureUrl" to photoUrl
            )
            firestore.collection("userProfiles").document(user.uid)
                .set(data)
                .addOnSuccessListener { isSaving.value = false; saveSuccess.value = true; onSuccess() }
                .addOnFailureListener { e ->
                    isSaving.value = false; lastError.value = e.message; onFailure()
                }
        }

        val picked = profilePictureUri.value
        if (picked != null) {
            val ref = FirebaseStorage.getInstance()
                .reference.child("profile_pictures/${user.uid}/profile.jpg")
            val metadata = storageMetadata { contentType = "image/jpeg" }
            ref.putFile(picked, metadata)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        profilePictureUrl.value = url.toString()
                        saveData(url.toString())
                    }
                }
                .addOnFailureListener { e ->
                    isSaving.value = false; lastError.value = e.message; onFailure()
                }
        } else {
            saveData(profilePictureUrl.value) // keep existing if no new one
        }
    }

    fun resetSaveState() { saveSuccess.value = false }
}
