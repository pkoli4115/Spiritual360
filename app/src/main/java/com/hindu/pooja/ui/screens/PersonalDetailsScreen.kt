package com.hindu.pooja.ui.personal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hindu.pooja.viewmodel.PersonalInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    viewModel: PersonalInfoViewModel = hiltViewModel(),
    onSubmitSuccess: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val countryQuery = remember { mutableStateOf("") }
    val stateQuery = remember { mutableStateOf("") }

    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isStateDropdownExpanded by remember { mutableStateOf(false) }

    val countries by viewModel.allCountries.collectAsState()
    val states by viewModel.allStates.collectAsState()

    val filteredCountries = remember(countryQuery.value, countries) {
        if (countryQuery.value.length >= 3)
            countries.filter { it.contains(countryQuery.value, ignoreCase = true) }
        else emptyList()
    }

    val filteredStates = remember(stateQuery.value, states) {
        if (stateQuery.value.length >= 3)
            states.filter { it.contains(stateQuery.value, ignoreCase = true) }
        else emptyList()
    }

    if (viewModel.saveSuccess.value == true) {
        viewModel.resetSaveState()
        onSubmitSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Personal Details", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.firstName,
            onValueChange = { viewModel.firstName = it },
            label = { Text("First Name *") },
            isError = viewModel.firstName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.middleName,
            onValueChange = { viewModel.middleName = it },
            label = { Text("Middle Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.lastName,
            onValueChange = { viewModel.lastName = it },
            label = { Text("Last Name *") },
            isError = viewModel.lastName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.fatherName,
            onValueChange = { viewModel.fatherName = it },
            label = { Text("Father's Name *") },
            isError = viewModel.fatherName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.motherName,
            onValueChange = { viewModel.motherName = it },
            label = { Text("Mother's Name *") },
            isError = viewModel.motherName.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            isError = !viewModel.isValidEmail(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = viewModel.countryCode,
                onValueChange = { viewModel.countryCode = it },
                label = { Text("Country Code") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = viewModel.phone,
                onValueChange = { viewModel.phone = it },
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(3f)
            )
        }

        if (!viewModel.isEmailOrPhoneProvided()) {
            Text("Either Email or Phone is required", color = MaterialTheme.colorScheme.error)
        }

        Text("Marital Status *")
        Row {
            listOf("Married", "UnMarried", "Divorced", "Widow").forEach { status ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    RadioButton(
                        selected = viewModel.maritalStatus == status,
                        onClick = { viewModel.maritalStatus = status }
                    )
                    Text(status)
                }
            }
        }

        if (viewModel.maritalStatus == "Married") {
            OutlinedTextField(
                value = viewModel.spouseName,
                onValueChange = { viewModel.spouseName = it },
                label = { Text("Spouse Name *") },
                isError = viewModel.spouseName.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.hasChildren,
                onCheckedChange = { viewModel.hasChildren = it }
            )
            Text("I have Children")
        }

        if (viewModel.hasChildren) {
            OutlinedTextField(
                value = viewModel.numberOfChildren,
                onValueChange = viewModel::onNumberOfChildrenChanged,
                label = { Text("Number of Children") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            viewModel.childNames.forEachIndexed { index, name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onChildNameChanged(index, it) },
                    label = { Text("Child ${index + 1} Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        OutlinedTextField(
            value = viewModel.gothram,
            onValueChange = { viewModel.gothram = it },
            label = { Text("Gothram *") },
            isError = viewModel.gothram.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.nakshatram,
            onValueChange = { viewModel.nakshatram = it },
            label = { Text("Nakshatram *") },
            isError = viewModel.nakshatram.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.birthDate,
            onValueChange = { viewModel.birthDate = it },
            label = { Text("Birth Date (DD-MM-YYYY) *") },
            isError = !viewModel.isValidDate(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.birthTime,
            onValueChange = { viewModel.birthTime = it },
            label = { Text("Birth Time *") },
            isError = viewModel.birthTime.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.birthPlace,
            onValueChange = { viewModel.birthPlace = it },
            label = { Text("Birth Place *") },
            isError = viewModel.birthPlace.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.addressLine1,
            onValueChange = { viewModel.addressLine1 = it },
            label = { Text("Address Line 1 *") },
            isError = viewModel.addressLine1.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.addressLine2,
            onValueChange = { viewModel.addressLine2 = it },
            label = { Text("Address Line 2") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.addressLine3,
            onValueChange = { viewModel.addressLine3 = it },
            label = { Text("Address Line 3") },
            modifier = Modifier.fillMaxWidth()
        )

        // Country dropdown
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
                    if (it.length >= 3) viewModel.fetchCountries()
                },
                label = { Text("Country *") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = viewModel.selectedCountry.isBlank(),
                readOnly = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCountryDropdownExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isCountryDropdownExpanded,
                onDismissRequest = { isCountryDropdownExpanded = false }
            ) {
                filteredCountries.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            viewModel.selectedCountry = it
                            countryQuery.value = it
                            stateQuery.value = ""
                            viewModel.fetchStates(it)
                            isCountryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // State dropdown
        ExposedDropdownMenuBox(
            expanded = isStateDropdownExpanded,
            onExpandedChange = {
                isStateDropdownExpanded = !isStateDropdownExpanded
                if (viewModel.selectedCountry.isNotBlank()) viewModel.fetchStates(viewModel.selectedCountry)
            }
        ) {
            OutlinedTextField(
                value = stateQuery.value,
                onValueChange = {
                    stateQuery.value = it
                    if (viewModel.selectedCountry.isNotBlank()) viewModel.fetchStates(viewModel.selectedCountry)
                },
                label = { Text("State *") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = viewModel.selectedState.isBlank(),
                readOnly = false,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStateDropdownExpanded) }
            )
            ExposedDropdownMenu(
                expanded = isStateDropdownExpanded,
                onDismissRequest = { isStateDropdownExpanded = false }
            ) {
                filteredStates.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            viewModel.selectedState = it
                            stateQuery.value = it
                            isStateDropdownExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = viewModel.city,
            onValueChange = { viewModel.city = it },
            label = { Text("City *") },
            isError = viewModel.city.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.pincode,
            onValueChange = { viewModel.pincode = it },
            label = { Text("Pincode *") },
            isError = !viewModel.isValidPincode(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.savePersonalInfo() },
            enabled = viewModel.isFormValid() && !viewModel.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (viewModel.isSaving)
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else
                Text("Submit")
        }
    }
}
