package com.hindu.pooja.ui.personal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hindu.pooja.viewmodel.PersonalInfoViewModel
import com.hindu.pooja.ui.components.DropdownMenuWithSearch
import kotlinx.coroutines.launch

@Composable
fun PersonalDetailsScreen(
    viewModel: PersonalInfoViewModel = viewModel(),
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchCountries()
        viewModel.loadExistingProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = viewModel.fullName,
            onValueChange = { viewModel.fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.fatherName,
            onValueChange = { viewModel.fatherName = it },
            label = { Text("Father's Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.motherName,
            onValueChange = { viewModel.motherName = it },
            label = { Text("Mother's Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = viewModel.phone,
            onValueChange = { viewModel.phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
        )

        Text("Marital Status")
        Row {
            listOf("Unmarried", "Married").forEach { status ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = viewModel.maritalStatus == status,
                        onClick = { viewModel.maritalStatus = status }
                    )
                    Text(status)
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        if (viewModel.maritalStatus == "Married") {
            OutlinedTextField(
                value = viewModel.spouseName,
                onValueChange = { viewModel.spouseName = it },
                label = { Text("Spouse Name") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.hasChildren,
                onCheckedChange = {
                    viewModel.hasChildren = it
                    if (!it) {
                        viewModel.numberOfChildren = "0"
                        viewModel.childNames = emptyList()
                    }
                }
            )
            Text("I have children")
        }

        if (viewModel.hasChildren) {
            OutlinedTextField(
                value = viewModel.numberOfChildren,
                onValueChange = { viewModel.onNumberOfChildrenChanged(it) },
                label = { Text("Number of Children") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
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
            label = { Text("Gothram") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.nakshatram,
            onValueChange = { viewModel.nakshatram = it },
            label = { Text("Nakshatram") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = viewModel.addressLine1,
            onValueChange = { viewModel.addressLine1 = it },
            label = { Text("Address Line 1") },
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

        Text("Country")
        if (viewModel.isCountriesLoading) {
            CircularProgressIndicator()
        } else {
            DropdownMenuWithSearch(
                options = viewModel.countries,
                selectedOption = viewModel.selectedCountry,
                onOptionSelected = {
                    viewModel.selectedCountry = it
                    viewModel.fetchStates(it)
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("State")
        if (viewModel.selectedCountry.isEmpty()) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Select a country first") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        } else if (viewModel.isStatesLoading) {
            CircularProgressIndicator()
        } else {
            DropdownMenuWithSearch(
                options = viewModel.states,
                selectedOption = viewModel.selectedState,
                onOptionSelected = { viewModel.selectedState = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.savePersonalInfo()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isSaving
        ) {
            Text("Submit")
        }

        if (viewModel.saveSuccess == true) {
            LaunchedEffect(Unit) { onSubmitSuccess() }
        } else if (viewModel.saveSuccess == false) {
            Text("Failed to save details. Try again.", color = MaterialTheme.colorScheme.error)
        }

        // ✅ Spacer to allow scroll past Submit
        Spacer(modifier = Modifier.height(80.dp))
    }
}
