package com.hindu.pooja.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hindu.pooja.data.CountryStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PersonalInfoViewModel @Inject constructor() : ViewModel() {

    // Name
    var firstName by mutableStateOf("")
    var middleName by mutableStateOf("")
    var lastName by mutableStateOf("")

    var fatherName by mutableStateOf("")
    var motherName by mutableStateOf("")

    // Contact
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var countryCode by mutableStateOf("+91") // Default India

    // Marital
    var maritalStatus by mutableStateOf("")
    var spouseName by mutableStateOf("")
    var hasChildren by mutableStateOf(false)
    var numberOfChildren by mutableStateOf("")
    var childNames = mutableStateListOf<String>()

    // Astro
    var gothram by mutableStateOf("")
    var nakshatram by mutableStateOf("")
    var birthDate by mutableStateOf("")
    var birthTime by mutableStateOf("")
    var birthPlace by mutableStateOf("")

    // Address
    var addressLine1 by mutableStateOf("")
    var addressLine2 by mutableStateOf("")
    var addressLine3 by mutableStateOf("")
    var selectedCountry by mutableStateOf("")
    var selectedState by mutableStateOf("")
    var city by mutableStateOf("")
    var pincode by mutableStateOf("")

    private val _allCountries = MutableStateFlow<List<String>>(emptyList())
    val allCountries: StateFlow<List<String>> = _allCountries

    private val _allStates = MutableStateFlow<List<String>>(emptyList())
    val allStates: StateFlow<List<String>> = _allStates

    private val _saveSuccess = MutableStateFlow<Boolean?>(null)
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess

    var isSaving by mutableStateOf(false)

    fun resetSaveState() {
        _saveSuccess.value = null
    }

    fun isEmailOrPhoneProvided(): Boolean {
        return email.isNotBlank() || phone.isNotBlank()
    }

    fun isValidPincode(): Boolean {
        return pincode.length in 4..8
    }

    fun isValidEmail(): Boolean {
        return email.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidDate(): Boolean {
        val regex = Regex("""\d{2}-\d{2}-\d{4}""")
        return birthDate.matches(regex)
    }

    fun isFormValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                fatherName.isNotBlank() &&
                motherName.isNotBlank() &&
                isEmailOrPhoneProvided() &&
                isValidEmail() &&
                maritalStatus.isNotBlank() &&
                (maritalStatus != "Married" || spouseName.isNotBlank()) &&
                gothram.isNotBlank() &&
                nakshatram.isNotBlank() &&
                birthDate.isNotBlank() && isValidDate() &&
                birthTime.isNotBlank() &&
                birthPlace.isNotBlank() &&
                addressLine1.isNotBlank() &&
                selectedCountry.isNotBlank() &&
                selectedState.isNotBlank() &&
                city.isNotBlank() &&
                isValidPincode()
    }

    fun fetchCountries() {
        _allCountries.value = CountryStateProvider.getAllCountries()
    }

    fun fetchStates(country: String) {
        _allStates.value = CountryStateProvider.getStatesForCountry(country)
    }

    fun onNumberOfChildrenChanged(newCount: String) {
        numberOfChildren = newCount
        val count = newCount.toIntOrNull() ?: 0
        childNames = MutableList(count) { index ->
            childNames.getOrNull(index) ?: ""
        }.toMutableStateList()
    }

    fun onChildNameChanged(index: Int, name: String) {
        if (index in childNames.indices) {
            childNames[index] = name
        }
    }

    fun savePersonalInfo() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        isSaving = true

        val profile = hashMapOf(
            "firstName" to firstName,
            "middleName" to middleName,
            "lastName" to lastName,
            "fatherName" to fatherName,
            "motherName" to motherName,
            "email" to email,
            "phone" to "$countryCode$phone",
            "maritalStatus" to maritalStatus,
            "spouseName" to spouseName,
            "hasChildren" to hasChildren,
            "numberOfChildren" to numberOfChildren,
            "childNames" to childNames,
            "gothram" to gothram,
            "nakshatram" to nakshatram,
            "birthDate" to birthDate,
            "birthTime" to birthTime,
            "birthPlace" to birthPlace,
            "addressLine1" to addressLine1,
            "addressLine2" to addressLine2,
            "addressLine3" to addressLine3,
            "country" to selectedCountry,
            "state" to selectedState,
            "city" to city,
            "pincode" to pincode
        )

        FirebaseFirestore.getInstance()
            .collection("userProfiles")
            .document(user.uid)
            .set(profile)
            .addOnSuccessListener {
                isSaving = false
                _saveSuccess.value = true
            }
            .addOnFailureListener {
                isSaving = false
                _saveSuccess.value = false
            }
    }
}
