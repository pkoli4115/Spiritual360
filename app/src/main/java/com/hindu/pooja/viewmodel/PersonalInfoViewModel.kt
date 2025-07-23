package com.hindu.pooja.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class Country(val name: String)
data class State(val name: String)
data class CountriesResponse(val data: List<Country>)
data class StatesResponse(val data: StatesData)
data class StatesData(val name: String, val states: List<State>)

class PersonalInfoViewModel : ViewModel() {

    var fullName by mutableStateOf("")
    var fatherName by mutableStateOf("")
    var motherName by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var maritalStatus by mutableStateOf("Unmarried")
    var spouseName by mutableStateOf("")
    var hasChildren by mutableStateOf(false)
    var numberOfChildren by mutableStateOf("0")
    var childNames by mutableStateOf<List<String>>(emptyList())
    var gothram by mutableStateOf("")
    var nakshatram by mutableStateOf("")
    var addressLine1 by mutableStateOf("")
    var addressLine2 by mutableStateOf("")
    var addressLine3 by mutableStateOf("")
    var selectedCountry by mutableStateOf("")
    var selectedState by mutableStateOf("")

    var countries by mutableStateOf<List<String>>(emptyList())
    var states by mutableStateOf<List<String>>(emptyList())
    var isCountriesLoading by mutableStateOf(false)
    var isStatesLoading by mutableStateOf(false)

    var isSaving by mutableStateOf(false)
    var saveSuccess by mutableStateOf<Boolean?>(null)

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun fetchCountries() {
        isCountriesLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://countriesnow.space/api/v0.1/countries/positions")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val countriesResponse = Gson().fromJson(response, CountriesResponse::class.java)
                val countryNames = countriesResponse.data.map { it.name }.sorted()

                withContext(Dispatchers.Main) {
                    countries = countryNames
                }
            } catch (e: Exception) {
                Log.e("FetchCountries", "Error: ${e.localizedMessage}")
            } finally {
                withContext(Dispatchers.Main) {
                    isCountriesLoading = false
                }
            }
        }
    }

    fun fetchStates(countryName: String) {
        isStatesLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://countriesnow.space/api/v0.1/countries/states")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val body = """{"country":"$countryName"}"""
                connection.outputStream.use { it.write(body.toByteArray()) }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val statesResponse = Gson().fromJson(response, StatesResponse::class.java)
                val stateNames = statesResponse.data.states.map { it.name }.sorted()

                withContext(Dispatchers.Main) {
                    states = stateNames
                }
            } catch (e: Exception) {
                Log.e("FetchStates", "Error: ${e.localizedMessage}")
            } finally {
                withContext(Dispatchers.Main) {
                    isStatesLoading = false
                }
            }
        }
    }

    fun onNumberOfChildrenChanged(countStr: String) {
        numberOfChildren = countStr
        val count = countStr.toIntOrNull() ?: 0
        childNames = List(count) { index ->
            childNames.getOrNull(index) ?: ""
        }
    }

    fun onChildNameChanged(index: Int, name: String) {
        childNames = childNames.toMutableList().also {
            if (index < it.size) it[index] = name
        }
    }

    fun savePersonalInfo() {
        isSaving = true
        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "fullName" to fullName,
            "fatherName" to fatherName,
            "motherName" to motherName,
            "email" to email,
            "phone" to phone,
            "maritalStatus" to maritalStatus,
            "spouseName" to spouseName,
            "hasChildren" to hasChildren,
            "numberOfChildren" to numberOfChildren,
            "childNames" to childNames,
            "gothram" to gothram,
            "nakshatram" to nakshatram,
            "addressLine1" to addressLine1,
            "addressLine2" to addressLine2,
            "addressLine3" to addressLine3,
            "country" to selectedCountry,
            "state" to selectedState
        )

        db.collection("userProfiles").document(uid)
            .set(data)
            .addOnSuccessListener {
                isSaving = false
                saveSuccess = true
            }
            .addOnFailureListener {
                isSaving = false
                saveSuccess = false
                Log.e("Firestore", "Save failed: ${it.localizedMessage}")
            }
    }

    fun loadExistingProfile(onLoaded: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("userProfiles").document(uid).get()
            .addOnSuccessListener { document ->
                document?.let {
                    fullName = it.getString("fullName") ?: ""
                    fatherName = it.getString("fatherName") ?: ""
                    motherName = it.getString("motherName") ?: ""
                    email = it.getString("email") ?: ""
                    phone = it.getString("phone") ?: ""
                    maritalStatus = it.getString("maritalStatus") ?: "Unmarried"
                    spouseName = it.getString("spouseName") ?: ""
                    hasChildren = it.getBoolean("hasChildren") ?: false
                    numberOfChildren = it.getString("numberOfChildren") ?: "0"
                    childNames = it.get("childNames") as? List<String> ?: emptyList()
                    gothram = it.getString("gothram") ?: ""
                    nakshatram = it.getString("nakshatram") ?: ""
                    addressLine1 = it.getString("addressLine1") ?: ""
                    addressLine2 = it.getString("addressLine2") ?: ""
                    addressLine3 = it.getString("addressLine3") ?: ""
                    selectedCountry = it.getString("country") ?: ""
                    selectedState = it.getString("state") ?: ""

                    if (selectedCountry.isNotEmpty()) {
                        fetchStates(selectedCountry)
                    }
                    onLoaded()
                }
            }
    }
}
