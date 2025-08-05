package com.hindu.pooja.ui.personal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.hindu.pooja.R
import com.hindu.pooja.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Prefill from Firestore on entry
    var loaded by remember { mutableStateOf(false) }
    if (!loaded) {
        LaunchedEffect(Unit) {
            viewModel.loadProfile(
                onSuccess = { loaded = true },
                onFailure = { loaded = true }
            )
        }
    }

    // Collect all user fields as State
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
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()
    val profilePictureUrl by viewModel.profilePictureUrl.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val formValid by viewModel.formValid.collectAsState()

    // Snackbar for upload progress or errors
    val snackbarHostState = remember { SnackbarHostState() }

    // Image Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setProfilePictureUri(uri)
            viewModel.validateForm()
        }
    }

    // ---- Date Picker ----
    var showDateDialog by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val date = remember(birthDate) {
        try { LocalDate.parse(birthDate, dateFormatter) } catch (_: Exception) { LocalDate.now() }
    }
    if (showDateDialog) {
        LaunchedEffect(showDateDialog) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, date.year)
                set(Calendar.MONTH, date.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
            }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val formatted = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                    viewModel.birthDate.value = formatted
                    viewModel.validateForm()
                    showDateDialog = false
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnCancelListener { showDateDialog = false }
                show()
            }
        }
    }

    // ---- Time Picker ----
    var showTimeDialog by remember { mutableStateOf(false) }
    val (hour, minute, amPm) = remember(birthTime) {
        val regex = Regex("""(\d{2}):(\d{2})\s?(AM|PM)?""", RegexOption.IGNORE_CASE)
        val m = regex.find(birthTime)
        val h = m?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 12
        val min = m?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0
        val ap = m?.groupValues?.getOrNull(3)?.uppercase() ?: "AM"
        Triple(h, min, ap)
    }
    if (showTimeDialog) {
        LaunchedEffect(showTimeDialog) {
            TimePickerDialog(
                context,
                { _, hourOfDay, minuteOfHour ->
                    val isAm = hourOfDay < 12
                    val hour12 = if (hourOfDay == 0 || hourOfDay == 12) 12 else hourOfDay % 12
                    val formatted = String.format("%02d:%02d %s", hour12, minuteOfHour, if (isAm) "AM" else "PM")
                    viewModel.birthTime.value = formatted
                    viewModel.validateForm()
                    showTimeDialog = false
                },
                if (amPm == "AM" && hour == 12) 0 else if (amPm == "PM" && hour != 12) hour + 12 else hour,
                minute,
                false
            ).apply {
                setOnCancelListener { showTimeDialog = false }
                show()
            }
        }
    }

    if (saveSuccess == true) {
        viewModel.resetSaveState()
        onSaveSuccess()
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Edit Profile", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // --- Profile Image Picker with Pencil Icon ---
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                val imagePainter = when {
                    profilePictureUri != null -> rememberAsyncImagePainter(profilePictureUri)
                    profilePictureUrl.isNotBlank() -> rememberAsyncImagePainter(profilePictureUrl)
                    else -> painterResource(id = R.drawable.ic_profile_placeholder)
                }
                Image(
                    painter = imagePainter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                // Pencil Icon
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { galleryLauncher.launch("image/*") }
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- All profile fields (examples, add more as needed) ---
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
            // Date of Birth
            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                label = { Text("Birth Date (DD-MM-YYYY) *") },
                isError = !viewModel.isValidDate(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDateDialog = true },
                readOnly = true
            )
            // Birth Time
            OutlinedTextField(
                value = birthTime,
                onValueChange = {},
                label = { Text("Birth Time (hh:mm AM/PM)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimeDialog = true },
                readOnly = true
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.saveProfileWithPhoto(
                        onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Profile saved successfully!")
                            }
                        },
                        onFailure = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to save profile/photo")
                            }
                        }
                    )
                },
                enabled = formValid && !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Save Profile")
            }
        }

        // Snackbar for feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
