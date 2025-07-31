package com.hindu.pooja.ui.personal

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.R
import com.hindu.pooja.viewmodel.ProfileViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Collect all relevant states
    val firstName by viewModel.fullName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val fatherName by viewModel.fatherName.collectAsState()
    val motherName by viewModel.motherName.collectAsState()
    val spouseName by viewModel.spouseName.collectAsState()
    val maritalStatus by viewModel.maritalStatus.collectAsState()
    val hasChildren by viewModel.hasChildren.collectAsState()
    val numberOfChildren by viewModel.numberOfChildren.collectAsState()
    val childNames by viewModel.childNames.collectAsState()
    val gothram by viewModel.gothram.collectAsState()
    val nakshatram by viewModel.nakshatram.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val birthTime by viewModel.birthTime.collectAsState()
    val birthPlace by viewModel.birthPlace.collectAsState()
    val addressLine1 by viewModel.addressLine1.collectAsState()
    val addressLine2 by viewModel.addressLine2.collectAsState()
    val addressLine3 by viewModel.addressLine3.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedState by viewModel.selectedState.collectAsState()
    val city by viewModel.city.collectAsState()
    val pincode by viewModel.pincode.collectAsState()
    val countryCode by viewModel.countryCode.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()
    val profilePictureUrl by viewModel.profilePictureUrl.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val formValid by viewModel.formValid.collectAsState()
    val countries by viewModel.allCountries.collectAsState()
    val states by viewModel.allStates.collectAsState()

    // Dropdown queries
    val countryQuery = remember { mutableStateOf(selectedCountry) }
    val stateQuery = remember { mutableStateOf(selectedState) }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isStateDropdownExpanded by remember { mutableStateOf(false) }

    // Prefetch countries when screen loads
    LaunchedEffect(Unit) { viewModel.fetchCountries() }

    val filteredCountries = remember(countryQuery.value, countries) {
        if (countryQuery.value.length >= 3)
            countries.filter { it.contains(countryQuery.value, ignoreCase = true) }
        else countries
    }
    val filteredStates = remember(stateQuery.value, states) {
        if (stateQuery.value.length >= 3)
            states.filter { it.contains(stateQuery.value, ignoreCase = true) }
        else states
    }

    // --- Date of Birth picker ---
    val dobCalendar = remember { Calendar.getInstance() }
    val dobDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                viewModel.birthDate.value = formatted
                viewModel.validateForm()
            },
            dobCalendar.get(Calendar.YEAR),
            dobCalendar.get(Calendar.MONTH),
            dobCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // --- Birth Time Dropdowns ---
    val hourOptions = (1..12).map { it.toString().padStart(2, '0') }
    val minuteOptions = (0..59).map { it.toString().padStart(2, '0') }
    val amPmOptions = listOf("AM", "PM")

    var birthHour by remember { mutableStateOf("") }
    var birthMinute by remember { mutableStateOf("") }
    var birthAmPm by remember { mutableStateOf("") }

    // Split birthTime into dropdowns (if present)
    LaunchedEffect(birthTime) {
        val regex = Regex("(\\d{2}):(\\d{2})\\s*(AM|PM)?", RegexOption.IGNORE_CASE)
        val match = regex.find(birthTime)
        if (match != null) {
            birthHour = match.groupValues[1]
            birthMinute = match.groupValues[2]
            birthAmPm = match.groupValues.getOrNull(3)?.uppercase() ?: ""
        }
    }

    if (saveSuccess == true) {
        viewModel.resetSaveState()
        onSaveSuccess()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setProfilePictureUri(uri)
        viewModel.validateForm()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Profile Image Picker ---
        Box(
            modifier = Modifier
                .size(120.dp)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            when {
                profilePictureUri != null -> Image(
                    painter = rememberAsyncImagePainter(profilePictureUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
                profilePictureUrl.isNotBlank() -> Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
                else -> Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Default Profile",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Basic Fields ---
        OutlinedTextField(
            value = firstName,
            onValueChange = { viewModel.fullName.value = it; viewModel.validateForm() },
            label = { Text("First Name *") },
            isError = firstName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { viewModel.lastName.value = it; viewModel.validateForm() },
            label = { Text("Last Name *") },
            isError = lastName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fatherName,
            onValueChange = { viewModel.fatherName.value = it; viewModel.validateForm() },
            label = { Text("Father's Name *") },
            isError = fatherName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = motherName,
            onValueChange = { viewModel.motherName.value = it; viewModel.validateForm() },
            label = { Text("Mother's Name *") },
            isError = motherName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        // Editable Email
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.email.value = it; viewModel.validateForm() },
            label = { Text("Email") },
            isError = !viewModel.isValidEmail(),
            modifier = Modifier.fillMaxWidth()
        )

        // Editable Phone
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = countryCode,
                onValueChange = { viewModel.countryCode.value = it; viewModel.validateForm() },
                label = { Text("Country Code") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.phone.value = it; viewModel.validateForm() },
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = !viewModel.isValidPhone(),
                modifier = Modifier.weight(3f)
            )
        }

        // --- Marital Status ---
        Text("Marital Status *")
        Row {
            listOf("Married", "UnMarried", "Divorced", "Widow").forEach { status ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    RadioButton(
                        selected = maritalStatus == status,
                        onClick = { viewModel.maritalStatus.value = status; viewModel.validateForm() }
                    )
                    Text(status)
                }
            }
        }
        if (maritalStatus == "Married") {
            OutlinedTextField(
                value = spouseName,
                onValueChange = { viewModel.spouseName.value = it; viewModel.validateForm() },
                label = { Text("Spouse Name *") },
                isError = spouseName.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = hasChildren,
                onCheckedChange = { viewModel.hasChildren.value = it; viewModel.validateForm() }
            )
            Text("I have Children")
        }

        if (hasChildren) {
            OutlinedTextField(
                value = numberOfChildren,
                onValueChange = { viewModel.onNumberOfChildrenChanged(it); viewModel.validateForm() },
                label = { Text("Number of Children") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            childNames.forEachIndexed { index, name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onChildNameChanged(index, it); viewModel.validateForm() },
                    label = { Text("Child ${index + 1} Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        OutlinedTextField(
            value = gothram,
            onValueChange = { viewModel.gothram.value = it; viewModel.validateForm() },
            label = { Text("Gothram *") },
            isError = gothram.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = nakshatram,
            onValueChange = { viewModel.nakshatram.value = it; viewModel.validateForm() },
            label = { Text("Nakshatram *") },
            isError = nakshatram.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        // --- Date of Birth (DatePicker) ---
        OutlinedTextField(
            value = birthDate,
            onValueChange = {},
            label = { Text("Date of Birth (DD-MM-YYYY) *") },
            isError = !viewModel.isValidDate(),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { dobDialog.show() },
            readOnly = true
        )

        // --- Birth Time (3 Dropdowns, OPTIONAL) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var hourExpanded by remember { mutableStateOf(false) }
            var minuteExpanded by remember { mutableStateOf(false) }
            var amPmExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = hourExpanded,
                onExpandedChange = { hourExpanded = !hourExpanded }
            ) {
                OutlinedTextField(
                    value = birthHour,
                    onValueChange = {},
                    label = { Text("Hour") },
                    modifier = Modifier.width(90.dp).menuAnchor(), // Set fixed width so it's compact
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(hourExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = hourExpanded,
                    onDismissRequest = { hourExpanded = false }
                ) {
                    hourOptions.forEach { hr ->
                        DropdownMenuItem(
                            text = { Text(hr) },
                            onClick = {
                                birthHour = hr
                                viewModel.birthTime.value = if (birthHour.isBlank() && birthMinute.isBlank() && birthAmPm.isBlank()) "" else "$birthHour:$birthMinute $birthAmPm".trim()
                                viewModel.validateForm()
                                hourExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = minuteExpanded,
                onExpandedChange = { minuteExpanded = !minuteExpanded }
            ) {
                OutlinedTextField(
                    value = birthMinute,
                    onValueChange = {},
                    label = { Text("Minute") },
                    modifier = Modifier.width(90.dp).menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(minuteExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = minuteExpanded,
                    onDismissRequest = { minuteExpanded = false }
                ) {
                    minuteOptions.forEach { min ->
                        DropdownMenuItem(
                            text = { Text(min) },
                            onClick = {
                                birthMinute = min
                                viewModel.birthTime.value = if (birthHour.isBlank() && birthMinute.isBlank() && birthAmPm.isBlank()) "" else "$birthHour:$birthMinute $birthAmPm".trim()
                                viewModel.validateForm()
                                minuteExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = amPmExpanded,
                onExpandedChange = { amPmExpanded = !amPmExpanded }
            ) {
                OutlinedTextField(
                    value = birthAmPm,
                    onValueChange = {},
                    label = { Text("AM/PM") },
                    modifier = Modifier.width(90.dp).menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(amPmExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = amPmExpanded,
                    onDismissRequest = { amPmExpanded = false }
                ) {
                    amPmOptions.forEach { ap ->
                        DropdownMenuItem(
                            text = { Text(ap) },
                            onClick = {
                                birthAmPm = ap
                                viewModel.birthTime.value = if (birthHour.isBlank() && birthMinute.isBlank() && birthAmPm.isBlank()) "" else "$birthHour:$birthMinute $birthAmPm".trim()
                                viewModel.validateForm()
                                amPmExpanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = birthPlace,
            onValueChange = { viewModel.birthPlace.value = it; viewModel.validateForm() },
            label = { Text("Birth Place *") },
            isError = birthPlace.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        // --- Address Fields ---
        OutlinedTextField(
            value = addressLine1,
            onValueChange = { viewModel.addressLine1.value = it; viewModel.validateForm() },
            label = { Text("Address Line 1 *") },
            isError = addressLine1.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = addressLine2,
            onValueChange = { viewModel.addressLine2.value = it; viewModel.validateForm() },
            label = { Text("Address Line 2") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = addressLine3,
            onValueChange = { viewModel.addressLine3.value = it; viewModel.validateForm() },
            label = { Text("Address Line 3") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Country / State Searchable Dropdowns ---
        Spacer(modifier = Modifier.height(12.dp))
        ExposedDropdownMenuBox(
            expanded = isCountryDropdownExpanded,
            onExpandedChange = {
                isCountryDropdownExpanded = !isCountryDropdownExpanded
                if (countryQuery.value.length >= 3) viewModel.fetchCountries()
            }
        ) {
            OutlinedTextField(
                value = countryQuery.value,
                onValueChange = {
                    countryQuery.value = it
                    viewModel.selectedCountry.value = it
                    if (it.length >= 3) viewModel.fetchCountries()
                },
                label = { Text("Country *") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = selectedCountry.isBlank(),
                readOnly = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCountryDropdownExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isCountryDropdownExpanded,
                onDismissRequest = { isCountryDropdownExpanded = false }
            ) {
                filteredCountries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country) },
                        onClick = {
                            viewModel.selectedCountry.value = country
                            countryQuery.value = country
                            stateQuery.value = ""
                            viewModel.fetchStates(country)
                            isCountryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        ExposedDropdownMenuBox(
            expanded = isStateDropdownExpanded,
            onExpandedChange = {
                isStateDropdownExpanded = !isStateDropdownExpanded
                if (selectedCountry.isNotBlank()) viewModel.fetchStates(selectedCountry)
            }
        ) {
            OutlinedTextField(
                value = stateQuery.value,
                onValueChange = {
                    stateQuery.value = it
                    viewModel.selectedState.value = it
                    if (selectedCountry.isNotBlank()) viewModel.fetchStates(selectedCountry)
                },
                label = { Text("State *") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = selectedState.isBlank(),
                readOnly = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStateDropdownExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isStateDropdownExpanded,
                onDismissRequest = { isStateDropdownExpanded = false }
            ) {
                filteredStates.forEach { state ->
                    DropdownMenuItem(
                        text = { Text(state) },
                        onClick = {
                            viewModel.selectedState.value = state
                            stateQuery.value = state
                            isStateDropdownExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = city,
            onValueChange = { viewModel.city.value = it; viewModel.validateForm() },
            label = { Text("City *") },
            isError = city.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = pincode,
            onValueChange = { viewModel.pincode.value = it; viewModel.validateForm() },
            label = { Text("Pincode *") },
            isError = !viewModel.isValidPincode(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Premium badge (optional) ---
        if (isPremium) {
            Text("🌟 Premium User", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Submit Button ---
        Button(
            onClick = { viewModel.saveProfile() },
            enabled = formValid && !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Save Profile")
        }
    }
}
