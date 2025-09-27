package com.hindu.pooja.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class DonationRecord(
    val id: String = "",                 // transaction reference (document id)
    val upiId: String = "",
    val payeeName: String = "",
    val amount: String = "",
    val note: String = "",
    val status: String = "INITIATED",    // INITIATED|SUCCESS|FAILURE|SUBMITTED|CANCELLED|UNKNOWN
    val txnId: String? = null,
    val approvalRefNo: String? = null,
    val provider: String? = null,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "ProfileVM"
        private const val USERS_COLLECTION = "userProfiles" // or "users" if you’ve migrated
        private const val DONATION_LIMIT = 20
    }

    // --- Firebase ---
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- UI state ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // Editable profile fields
    private val _profileId = MutableStateFlow("")
    val profileId: StateFlow<String> = _profileId.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl.asStateFlow()

    private val _loginProvider = MutableStateFlow("Google")
    val loginProvider: StateFlow<String> = _loginProvider.asStateFlow()

    // Donations
    private val _donations = MutableStateFlow<List<DonationRecord>>(emptyList())
    val donations: StateFlow<List<DonationRecord>> = _donations.asStateFlow()
    private var donationsListener: ListenerRegistration? = null

    init {
        loadProfile()
        startDonationsListener()
    }

    fun loadProfile() {
        val user = auth.currentUser ?: return
        _isLoading.value = true
        _lastError.value = null
        _loginProvider.value = providerName(user)

        db.collection(USERS_COLLECTION)
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _profileId.value = doc.getString("profileId") ?: ensureProfileId()
                    _fullName.value = doc.getString("fullName") ?: (user.displayName ?: "")
                    _email.value = doc.getString("email") ?: (user.email ?: "")
                    _phone.value = doc.getString("phone") ?: (user.phoneNumber ?: "")
                    _profilePictureUrl.value = doc.getString("photoUrl") ?: user.photoUrl?.toString()
                } else {
                    seedFromFirebase(user)
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _lastError.value = it.localizedMessage
                _isLoading.value = false
            }
    }

    fun setFullName(value: String) { _fullName.value = value }
    fun setEmail(value: String)    { _email.value = value }
    fun setPhone(value: String)    { _phone.value = value }
    fun setProfilePictureUri(uri: Uri?) { _profilePictureUrl.value = uri?.toString() }

    fun validateForm(): Boolean {
        val nameOk = _fullName.value.trim().isNotEmpty()
        val emailOk = _email.value.trim().isNotEmpty()
        val phoneOk = _phone.value.isEmpty() || _phone.value.trim().length in 8..15
        return nameOk && emailOk && phoneOk
    }

    fun saveProfile() {
        val user = auth.currentUser ?: return
        if (!validateForm()) {
            _lastError.value = "Please fill required fields."
            return
        }
        _isLoading.value = true
        _lastError.value = null
        _saveSuccess.value = false

        val data = hashMapOf(
            "uid" to user.uid,
            "profileId" to (_profileId.value.ifBlank { ensureProfileId() }),
            "fullName" to _fullName.value.trim(),
            "email" to _email.value.trim(),
            "phone" to _phone.value.trim(),
            "photoUrl" to (_profilePictureUrl.value ?: ""),
            "loginProvider" to _loginProvider.value
        )

        db.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(data)
            .addOnSuccessListener {
                _saveSuccess.value = true
                _isLoading.value = false
            }
            .addOnFailureListener {
                _lastError.value = it.localizedMessage
                _isLoading.value = false
            }
    }

    fun resetSaveSuccess() { _saveSuccess.value = false }

    fun logout(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signOut()
                onResult(true)
            } catch (e: Exception) {
                _lastError.value = e.localizedMessage
                onResult(false)
            }
        }
    }

    // ---------- Donations listener (SUCCESS only) ----------
    fun startDonationsListener(serverFilter: Boolean = true) {
        val uid = auth.currentUser?.uid ?: run {
            Log.w(TAG, "startDonationsListener: no user")
            return
        }

        donationsListener?.remove()

        val base = db.collection(USERS_COLLECTION).document(uid).collection("donations")
        val query = if (serverFilter) {
            // Fast path (needs composite index: status ASC / createdAtMs DESC).
            Log.d(TAG, "Donations: server filter status=SUCCESS + orderBy(createdAtMs DESC)")
            base.whereEqualTo("status", "SUCCESS")
                .orderBy("createdAtMs", Query.Direction.DESCENDING)
                .limit(DONATION_LIMIT.toLong())
        } else {
            // Fallback (no index needed): fetch recent and filter in app.
            Log.d(TAG, "Donations: fallback query (client-side filter)")
            base.orderBy("createdAtMs", Query.Direction.DESCENDING)
                .limit((DONATION_LIMIT * 3).toLong())
        }

        donationsListener = query.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(TAG, "Donations listen failed (index missing?).", err)
                if (serverFilter) {
                    // Remove current listener and retry without the server filter.
                    donationsListener?.remove()
                    donationsListener = null
                    startDonationsListener(serverFilter = false)
                } else {
                    _lastError.value = err.localizedMessage
                }
                return@addSnapshotListener
            }

            val all = snap?.documents?.map { d ->
                DonationRecord(
                    id = d.getString("id") ?: d.id,
                    upiId = d.getString("upiId") ?: "",
                    payeeName = d.getString("payeeName") ?: "",
                    amount = d.getString("amount") ?: "",
                    note = d.getString("note") ?: "",
                    status = d.getString("status") ?: "UNKNOWN",
                    txnId = d.getString("txnId"),
                    approvalRefNo = d.getString("approvalRefNo"),
                    provider = d.getString("provider"),
                    createdAtMs = d.getLong("createdAtMs") ?: 0L,
                    updatedAtMs = d.getLong("updatedAtMs") ?: 0L
                )
            }.orEmpty()

            val successOnly =
                if (serverFilter) all
                else all.filter { it.status.equals("SUCCESS", ignoreCase = true) }
                    .take(DONATION_LIMIT)

            Log.d(TAG, "Donations listen -> successOnly=${successOnly.size}")
            _donations.value = successOnly
        }
    }

    override fun onCleared() {
        donationsListener?.remove()
        super.onCleared()
    }

    // ---------- Helpers ----------
    private fun seedFromFirebase(user: FirebaseUser) {
        _profileId.value = ensureProfileId()
        _fullName.value = user.displayName ?: ""
        _email.value = user.email ?: ""
        _phone.value = user.phoneNumber ?: ""
        _profilePictureUrl.value = user.photoUrl?.toString()
    }

    private fun ensureProfileId(): String {
        if (_profileId.value.isNotBlank()) return _profileId.value
        val fresh = "HP-" + Random.nextBytes(4).joinToString("") { b ->
            val i = (b.toInt() and 0xFF)
            "0123456789ABCDEF"[i % 16].toString()
        }
        _profileId.value = fresh
        return fresh
    }

    private fun providerName(user: FirebaseUser): String {
        val ids = user.providerData.mapNotNull { it.providerId }
        return when {
            ids.any { it.contains("facebook", ignoreCase = true) } -> "Facebook"
            ids.any { it.contains("google", ignoreCase = true) } -> "Google"
            else -> "Email/Password"
        }
    }
}
