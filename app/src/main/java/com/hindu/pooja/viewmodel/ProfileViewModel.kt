package com.hindu.pooja.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- Profile Fields ---
    val fullName = MutableStateFlow("")
    val lastName = MutableStateFlow("")
    val fatherName = MutableStateFlow("")
    val motherName = MutableStateFlow("")
    val spouseName = MutableStateFlow("")
    val maritalStatus = MutableStateFlow("")
    val hasChildren = MutableStateFlow(false)
    val numberOfChildren = MutableStateFlow("")
    val childNames = MutableStateFlow<List<String>>(emptyList())
    val gothram = MutableStateFlow("")
    val nakshatram = MutableStateFlow("")
    val birthDate = MutableStateFlow("")
    val birthTime = MutableStateFlow("")
    val birthPlace = MutableStateFlow("")
    val addressLine1 = MutableStateFlow("")
    val addressLine2 = MutableStateFlow("")
    val addressLine3 = MutableStateFlow("")
    val selectedCountry = MutableStateFlow("")
    val selectedState = MutableStateFlow("")
    val city = MutableStateFlow("")
    val pincode = MutableStateFlow("")
    val countryCode = MutableStateFlow("+91")
    val email = MutableStateFlow("")
    val phone = MutableStateFlow("")
    val isPremium = MutableStateFlow(false)

    // Profile Photo
    val profilePictureUrl = MutableStateFlow("")
    val profilePictureUri = MutableStateFlow<Uri?>(null)
    val allCountries = MutableStateFlow<List<String>>(emptyList())
    val allStates = MutableStateFlow<List<String>>(emptyList())

    // --- Firestore Real-Time Listener for Premium ---
    init {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("userProfiles")
                .document(userId)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        isPremium.value = doc.getBoolean("isPremium") ?: false
                    }
                }
        }
    }

    fun fetchCountries() {
        allCountries.value = com.hindu.pooja.data.CountryStateProvider.getAllCountries()
    }
    fun fetchStates(country: String) {
        allStates.value = com.hindu.pooja.data.CountryStateProvider.getStatesForCountry(country)
    }

    // --- UI State ---
    val isSaving = MutableStateFlow(false)
    val saveSuccess = MutableStateFlow(false)
    val formValid = MutableStateFlow(false)

    // --- Validation Logic ---
    fun isValidEmail(): Boolean =
        email.value.isBlank() || Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        ).matcher(email.value).matches()

    fun isValidPhone(): Boolean =
        phone.value.isBlank() || Pattern.compile("^\\+?[0-9]{8,15}\$").matcher(phone.value).matches()

    fun isValidPincode(): Boolean =
        pincode.value.isBlank() || Pattern.compile("^[1-9][0-9]{5}\$").matcher(pincode.value).matches()

    fun isValidDate(): Boolean =
        birthDate.value.isBlank() || Pattern.compile("^\\d{2}-\\d{2}-\\d{4}\$").matcher(birthDate.value).matches()

    fun isEmailOrPhoneProvided(): Boolean =
        email.value.isNotBlank() || phone.value.isNotBlank()

    fun validateForm() {
        formValid.value =
            fullName.value.isNotBlank() &&
                    lastName.value.isNotBlank() &&
                    fatherName.value.isNotBlank() &&
                    motherName.value.isNotBlank() &&
                    maritalStatus.value.isNotBlank() &&
                    (maritalStatus.value != "Married" || spouseName.value.isNotBlank()) &&
                    gothram.value.isNotBlank() &&
                    nakshatram.value.isNotBlank() &&
                    birthDate.value.isNotBlank() && isValidDate() &&
                    birthTime.value.isNotBlank() &&
                    birthPlace.value.isNotBlank() &&
                    addressLine1.value.isNotBlank() &&
                    selectedCountry.value.isNotBlank() &&
                    selectedState.value.isNotBlank() &&
                    city.value.isNotBlank() &&
                    isValidPincode() &&
                    isEmailOrPhoneProvided() &&
                    isValidEmail() &&
                    isValidPhone()
    }

    // --- Children helpers ---
    fun onNumberOfChildrenChanged(value: String) {
        numberOfChildren.value = value
        val count = value.toIntOrNull() ?: 0
        childNames.value = List(count) { childNames.value.getOrNull(it) ?: "" }
    }

    fun onChildNameChanged(index: Int, value: String) {
        childNames.value = childNames.value.toMutableList().also { list ->
            if (index < list.size) list[index] = value
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun setProfilePictureUri(uri: Uri?) {
        profilePictureUri.value = uri
    }

    fun setProfilePictureUrl(url: String) {
        profilePictureUrl.value = url
    }

    // --- Profile Load/Save Logic ---
    fun loadProfile(onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return onFailure()
        firestore.collection("userProfiles").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    fullName.value = doc.getString("fullName") ?: ""
                    lastName.value = doc.getString("lastName") ?: ""
                    fatherName.value = doc.getString("fatherName") ?: ""
                    motherName.value = doc.getString("motherName") ?: ""
                    spouseName.value = doc.getString("spouseName") ?: ""
                    maritalStatus.value = doc.getString("maritalStatus") ?: ""
                    hasChildren.value = doc.getBoolean("hasChildren") ?: false
                    numberOfChildren.value = doc.getString("numberOfChildren") ?: ""
                    childNames.value = (doc.get("childNames") as? List<*>)?.map { it.toString() } ?: emptyList()
                    gothram.value = doc.getString("gothram") ?: ""
                    nakshatram.value = doc.getString("nakshatram") ?: ""
                    birthDate.value = doc.getString("birthDate") ?: ""
                    birthTime.value = doc.getString("birthTime") ?: ""
                    birthPlace.value = doc.getString("birthPlace") ?: ""
                    addressLine1.value = doc.getString("addressLine1") ?: ""
                    addressLine2.value = doc.getString("addressLine2") ?: ""
                    addressLine3.value = doc.getString("addressLine3") ?: ""
                    selectedCountry.value = doc.getString("country") ?: ""
                    selectedState.value = doc.getString("state") ?: ""
                    city.value = doc.getString("city") ?: ""
                    pincode.value = doc.getString("pincode") ?: ""
                    countryCode.value = doc.getString("countryCode") ?: "+91"
                    email.value = doc.getString("email") ?: auth.currentUser?.email.orEmpty()
                    phone.value = doc.getString("phone") ?: ""
                    isPremium.value = doc.getBoolean("isPremium") ?: false
                    profilePictureUrl.value = doc.getString("profilePictureUrl") ?: ""
                    onSuccess()
                } else {
                    onFailure()
                }
            }
            .addOnFailureListener { onFailure() }
    }

    /**
     * Use this for both photo and other fields update!
     */
    fun saveProfileWithPhoto(onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        val user = auth.currentUser ?: return onFailure()
        isSaving.value = true
        val photoUri = profilePictureUri.value

        if (photoUri != null) {
            val storageRef = FirebaseStorage.getInstance()
                .reference.child("profile_pictures/${user.uid}/profile.jpg")

            storageRef.putFile(photoUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        profilePictureUrl.value = url.toString()
                        saveProfile(onSuccess, onFailure)
                    }.addOnFailureListener {
                        isSaving.value = false
                        onFailure()
                    }
                }
                .addOnFailureListener {
                    isSaving.value = false
                    onFailure()
                }
        } else {
            saveProfile(onSuccess, onFailure)
        }
    }

    fun saveProfile(onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        val user = auth.currentUser ?: return onFailure()
        val data = mapOf(
            "fullName" to fullName.value,
            "lastName" to lastName.value,
            "fatherName" to fatherName.value,
            "motherName" to motherName.value,
            "spouseName" to spouseName.value,
            "maritalStatus" to maritalStatus.value,
            "hasChildren" to hasChildren.value,
            "numberOfChildren" to numberOfChildren.value,
            "childNames" to childNames.value,
            "gothram" to gothram.value,
            "nakshatram" to nakshatram.value,
            "birthDate" to birthDate.value,
            "birthTime" to birthTime.value,
            "birthPlace" to birthPlace.value,
            "addressLine1" to addressLine1.value,
            "addressLine2" to addressLine2.value,
            "addressLine3" to addressLine3.value,
            "country" to selectedCountry.value,
            "state" to selectedState.value,
            "city" to city.value,
            "pincode" to pincode.value,
            "countryCode" to countryCode.value,
            "email" to email.value,
            "phone" to phone.value,
            "isPremium" to isPremium.value,
            "profilePictureUrl" to profilePictureUrl.value
        )
        firestore.collection("userProfiles").document(user.uid)
            .set(data)
            .addOnSuccessListener {
                isSaving.value = false
                saveSuccess.value = true
                onSuccess()
            }
            .addOnFailureListener {
                isSaving.value = false
                saveSuccess.value = false
                onFailure()
            }
    }

    fun resetSaveState() {
        saveSuccess.value = false
    }

    fun setPremium(isPremium: Boolean) {
        this.isPremium.value = isPremium
    }
}
