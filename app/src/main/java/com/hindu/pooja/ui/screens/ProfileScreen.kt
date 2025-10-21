package com.hindu.pooja.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hindu.pooja.R
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.viewmodel.DonationRecord
import com.hindu.pooja.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* =====================  Donation constants  ===================== */
/** Change only these three if you ever switch UPI */
private const val DONATION_UPI_ID = "9121011887@ybl"
private const val DONATION_PAYEE = "Koli Prasanth"
private const val DONATION_NOTE  = "Donation to Spiritual360 App"
private const val TAG = "DonateFlow"

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onLogout: (() -> Unit)? = null
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val profileId by viewModel.profileId.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val photo by viewModel.profilePictureUrl.collectAsState()
    val provider by viewModel.loginProvider.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.lastError.collectAsState()
    val donations by viewModel.donations.collectAsState()

    // Show the donate banner until dismissed
    var showDonateBanner by rememberSaveable { mutableStateOf(true) }

    // UPI launcher that logs to Firestore and updates status
    val donate = rememberUpiDonationLauncher(
        upiId = DONATION_UPI_ID,
        payeeName = DONATION_PAYEE,
        note = DONATION_NOTE
    )

    val quickAmounts = listOf("₹51", "₹101", "₹501", "₹1001", "₹2001", "₹5001")

    /* ---------- Ramakoti Achievements (merged: runs + history) ---------- */
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var achievements by remember { mutableStateOf<List<RamakotiAchievement>>(emptyList()) }
    var achLoading by remember { mutableStateOf(true) }

    // Internal holders for the two sources
    var runsList by remember { mutableStateOf<List<RamakotiAchievement>>(emptyList()) }
    var historyList by remember { mutableStateOf<List<RamakotiAchievement>>(emptyList()) }

    fun recomputeMerged() {
        // Merge, dedupe, sort desc by completedAt
        val merged = (runsList + historyList)
            .distinctBy {
                when {
                    it.certificateUrl.isNotBlank() -> "cert:${it.certificateUrl}"
                    it.completedAt != null && it.targetCount != null ->
                        "pair:${it.targetCount}:${it.completedAt.time}"
                    else -> "fallback:${it.totalAtCompletion}:${it.targetCount ?: -1}"
                }
            }
            .sortedByDescending { it.completedAt?.time ?: 0L }
        achievements = merged
    }

    LaunchedEffect(uid) {
        if (uid == null) {
            achievements = emptyList()
            achLoading = false
            return@LaunchedEffect
        }
        val db = FirebaseFirestore.getInstance()

        // Runs (new flow): users/{uid}/ramakotiRuns where status == COMPLETED
        db.collection("users").document(uid)
            .collection("ramakotiRuns")
            .whereEqualTo("status", "COMPLETED")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    achLoading = false
                    return@addSnapshotListener
                }
                runsList = snap?.documents?.map { d ->
                    RamakotiAchievement(
                        totalAtCompletion = (d.getLong("targetCount") ?: 0L).toInt(), // display-only
                        targetCount       = (d.getLong("targetCount") ?: 0L).toInt().takeIf { it > 0 },
                        completedAt       = d.getTimestamp("completedAt")?.toDate(),
                        certificateUrl    = d.getString("certificateUrl") ?: "",
                        language          = d.getString("language") ?: "en"
                    )
                } ?: emptyList()
                achLoading = false
                recomputeMerged()
            }

        // History (older flow): users/{uid}/ramakotiHistory
        db.collection("users").document(uid)
            .collection("ramakotiHistory")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    achLoading = false
                    return@addSnapshotListener
                }
                historyList = snap?.documents?.map { d ->
                    RamakotiAchievement(
                        totalAtCompletion = (d.getLong("totalAtCompletion") ?: 0L).toInt(),
                        targetCount       = (d.getLong("targetCount") ?: 0L).toInt().takeIf { it > 0 },
                        completedAt       = d.getTimestamp("completedAt")?.toDate(),
                        certificateUrl    = d.getString("certificateUrl") ?: "",
                        language          = d.getString("language") ?: "en"
                    )
                } ?: emptyList()
                achLoading = false
                recomputeMerged()
            }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ---------- Title ---------- */
            item {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            /* ---------- Donation banner (dismissible) ---------- */
            if (showDonateBanner) {
                item {
                    DonateMiniBanner(
                        onDonate = { donate(null) },
                        onDetails = {
                            openUpiChooser(
                                context,
                                DONATION_UPI_ID,
                                DONATION_PAYEE,
                                null,
                                DONATION_NOTE
                            )
                        },
                        onDismiss = { showDonateBanner = false }
                    )
                }
            }

            /* ---------- Avatar ---------- */
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val painter = if (!photo.isNullOrBlank())
                        rememberAsyncImagePainter(model = photo)
                    else painterResource(R.drawable.ic_profile_placeholder)

                    Image(
                        painter = painter,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.2f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            /* ---------- Profile info ---------- */
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Profile ID: $profileId", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Name: $fullName", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: $email", style = MaterialTheme.typography.bodyLarge)
                    if (phone.isNotBlank()) Text("Mobile: $phone", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Logged in using $provider", style = MaterialTheme.typography.bodyMedium)
                }
            }

            /* ---------- Actions ---------- */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.EditProfile.route) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Edit Profile") }

                    OutlinedButton(
                        onClick = {
                            viewModel.logout { ok ->
                                if (ok) onLogout?.invoke() else {
                                    Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Logout") }
                }
            }

            /* ---------- Donation card (UPI link + QR + quick amounts) ---------- */
            item {
                DonationCard(
                    upiId = DONATION_UPI_ID,
                    payeeName = DONATION_PAYEE,
                    note = DONATION_NOTE,
                    amounts = quickAmounts,
                    onAmountClick = { label ->
                        val amount = label.filter { it.isDigit() }
                        donate(amount)
                    },
                    onUpiTap = {
                        openUpiChooser(context, DONATION_UPI_ID, DONATION_PAYEE, null, DONATION_NOTE)
                    }
                )
            }

            /* ---------- My Ramakoti Achievements ---------- */
            item {
                Text("My Ramakoti Achievements", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Completed targets with certificate links.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (achLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            } else if (achievements.isEmpty()) {
                item {
                    Text(
                        "No completed targets yet — keep going!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(achievements) { ach ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                titleFor(ach),
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            ach.completedAt?.let { date ->
                                val fmt = remember {
                                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                }
                                Text(
                                    "Completed on ${fmt.format(date)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (!ach.language.isNullOrBlank()) {
                                Text(
                                    "Language: ${ach.language}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (ach.certificateUrl.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(ach.certificateUrl)
                                            )
                                            context.startActivity(intent)
                                        }
                                    ) { Text("View Certificate") }
                                }
                                // Start a new journey → go to picker
                                TextButton(
                                    onClick = { navController.navigate("ramakoti/language") }
                                ) { Text("Set New Target") }
                            }
                        }
                    }
                }
            }

            /* ---------- Recent donations ---------- */
            if (donations.isNotEmpty()) {
                item { Text("Recent donations", style = MaterialTheme.typography.titleMedium) }
                items(donations) { d -> DonationRow(d) }
            }

            if (isLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            error?.let {
                item { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/* =====================  Achievements model + helpers  ===================== */

private data class RamakotiAchievement(
    val totalAtCompletion: Int,
    val targetCount: Int?,          // may be null in older entries
    val completedAt: Date?,
    val certificateUrl: String,
    val language: String?
)

private fun titleFor(a: RamakotiAchievement): String {
    val target = a.targetCount ?: inferTargetFromTotal(a.totalAtCompletion)
    return when (target) {
        100_000     -> "1 Lakh Sri Rama Namas — Completed"
        1_000_000   -> "10 Lakh Sri Rama Namas — Completed"
        10_000_000  -> "1 Crore Sri Rama Namas — Completed"
        else        -> "Target $target — Completed"
    }
}

private fun inferTargetFromTotal(total: Int): Int {
    return when {
        total >= 10_000_000 -> 10_000_000
        total >= 1_000_000  -> 1_000_000
        total >= 100_000    -> 100_000
        else -> total
    }
}

/* =====================  Donation UI & helpers  ===================== */

@Composable
private fun DonateMiniBanner(
    onDonate: () -> Unit,
    onDetails: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "🙏 Support this project",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Voluntary donation. All features are free for everyone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onDonate, modifier = Modifier.weight(1f)) { Text("Donate") }
                OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f)) { Text("Details") }
                TextButton(onClick = onDismiss) { Text("Dismiss") }
            }
        }
    }
}

@Composable
private fun DonationCard(
    upiId: String,
    payeeName: String,
    note: String,
    amounts: List<String>,
    onAmountClick: (String) -> Unit,
    onUpiTap: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Donate to $payeeName", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(
                upiId,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onUpiTap)
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(upiId))
                    Toast.makeText(context, "UPI ID copied", Toast.LENGTH_SHORT).show()
                }) { Text("Copy UPI ID") }
                TextButton(onClick = {
                    shareText(context, "UPI ID", upiId)
                }) { Text("Share UPI ID") }
            }

            Spacer(Modifier.height(12.dp))
            Image(
                painter = painterResource(R.drawable.donation_qr),
                contentDescription = "UPI QR",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp)
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(16.dp))
            Text("Choose an amount:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            QuickAmountPills(amounts, onAmountClick)
        }
    }
}

@Composable
private fun DonationRow(d: DonationRecord) {
    val statusColor = when (d.status.uppercase()) {
        "SUCCESS"   -> Color(0xFF1B5E20)
        "SUBMITTED" -> Color(0xFF1565C0)
        "FAILURE"   -> Color(0xFFB71C1C)
        "CANCELLED" -> Color(0xFF6D6D6D)
        else        -> Color(0xFF8E24AA)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("₹${d.amount.ifBlank { "--" }} • ${d.payeeName}", style = MaterialTheme.typography.bodyLarge)
            Text("UPI: ${d.upiId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            d.txnId?.let { Text("TxnId: $it", style = MaterialTheme.typography.bodySmall) }
            d.approvalRefNo?.let { Text("Ref: $it", style = MaterialTheme.typography.bodySmall) }
        }
        AssistChip(
            onClick = {},
            label = { Text(d.status) },
            colors = AssistChipDefaults.assistChipColors(
                labelColor = Color.White,
                containerColor = statusColor
            )
        )
    }
}

/* =====================  UPI logging + chooser  ===================== */

@Composable
private fun rememberUpiDonationLauncher(
    upiId: String,
    payeeName: String,
    note: String
): (String?) -> Unit {

    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    var pendingTr by remember { mutableStateOf<String?>(null) }

    fun buildUri(amount: String?, tr: String): Uri {
        val sb = StringBuilder()
            .append("upi://pay")
            .append("?pa=").append(upiId)
            .append("&pn=").append(Uri.encode(payeeName))
            .append("&tn=").append(Uri.encode(note))
            .append("&tr=").append(Uri.encode(tr))
            .append("&cu=INR")
        if (!amount.isNullOrBlank()) sb.append("&am=").append(Uri.encode(amount))
        val uri = Uri.parse(sb.toString())
        Log.d(TAG, "Built UPI URI: $uri")
        return uri
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uid = auth.currentUser?.uid ?: run {
            Log.w(TAG, "onActivityResult: No user logged in; aborting update")
            return@rememberLauncherForActivityResult
        }
        val tr = pendingTr ?: run {
            Log.w(TAG, "onActivityResult: No pendingTr; nothing to update")
            return@rememberLauncherForActivityResult
        }

        val responseText = result.data?.getStringExtra("response") ?: result.data?.dataString
        Log.d(TAG, "onActivityResult: resultCode=${result.resultCode}, response=$responseText")
        val parsed = parseUpiResponse(responseText)
        Log.d(TAG, "onActivityResult: parsed map=$parsed")

        var status = mapUpiStatus(parsed["status"])
        if (status == "UNKNOWN" && result.resultCode == Activity.RESULT_CANCELED) {
            status = "CANCELLED"
        }
        Log.d(TAG, "onActivityResult: derived status=$status")

        val update = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAtMs" to System.currentTimeMillis()
        )
        parsed["txnid"]?.let { update["txnId"] = it }
        (parsed["approvalrefno"] ?: parsed["approvalref"] ?: parsed["refno"])?.let {
            update["approvalRefNo"] = it
        }
        Log.d(TAG, "onActivityResult: updating Firestore donation/$tr with $update")

        db.collection("userProfiles").document(uid)
            .collection("donations").document(tr)
            .update(update as Map<String, Any>)
            .addOnSuccessListener { Log.d(TAG, "onActivityResult: Firestore update SUCCESS for $tr") }
            .addOnFailureListener { e ->
                Log.e(TAG, "onActivityResult: Firestore update FAILED for $tr", e)
                Toast.makeText(context, "Failed to update donation status: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        pendingTr = null
    }

    return remember {
        { amount: String? ->
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Log.w(TAG, "startDonation: user not logged in")
                Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
            } else {
                val tr = "DON-${System.currentTimeMillis()}"
                pendingTr = tr
                val now = System.currentTimeMillis()
                val init = mutableMapOf<String, Any>(
                    "id" to tr,
                    "upiId" to upiId,
                    "payeeName" to payeeName,
                    "amount" to (amount ?: ""),
                    "note" to DONATION_NOTE,
                    "status" to "INITIATED",
                    "createdAtMs" to now,
                    "updatedAtMs" to now
                )

                Log.d(TAG, "startDonation: creating donation doc $tr with $init")
                db.collection("userProfiles").document(uid)
                    .collection("donations").document(tr)
                    .set(init)
                    .addOnSuccessListener {
                        Log.d(TAG, "startDonation: Firestore .set SUCCESS for $tr → launching chooser")
                        val intent = Intent(Intent.ACTION_VIEW, buildUri(amount, tr))
                        val chooser = Intent.createChooser(intent, "Pay with UPI")
                        try {
                            Log.d(TAG, "startDonation: startActivity(chooser)")
                            context.startActivity(chooser)
                        } catch (_: ActivityNotFoundException) {
                            Log.e(TAG, "startDonation: no UPI app found")
                            Toast.makeText(
                                context,
                                "No UPI app found. Install Google Pay / PhonePe / Paytm.",
                                Toast.LENGTH_LONG
                            ).show()
                            db.collection("userProfiles").document(uid)
                                .collection("donations").document(tr)
                                .update(
                                    mapOf(
                                        "status" to "CANCELLED",
                                        "updatedAtMs" to System.currentTimeMillis()
                                    )
                                )
                                .addOnSuccessListener { Log.d(TAG, "fallback: marked $tr as CANCELLED") }
                                .addOnFailureListener { e -> Log.e(TAG, "fallback: failed to mark CANCELLED", e) }
                        } catch (e: Exception) {
                            Log.e(TAG, "startDonation: could not start UPI chooser", e)
                            Toast.makeText(context, "Could not start UPI: ${e.message}", Toast.LENGTH_LONG).show()
                            db.collection("userProfiles").document(uid)
                                .collection("donations").document(tr)
                                .update(
                                    mapOf(
                                        "status" to "FAILURE",
                                        "updatedAtMs" to System.currentTimeMillis()
                                    )
                                )
                                .addOnSuccessListener { Log.d(TAG, "fallback: marked $tr as FAILURE") }
                                .addOnFailureListener { ex -> Log.e(TAG, "fallback: failed to mark FAILURE", ex) }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "startDonation: Firestore .set FAILED for $tr", e)
                        Toast.makeText(context, "Failed to log donation: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

/* =====================  Helpers  ===================== */

private fun openUpiChooser(
    context: android.content.Context,
    upiId: String,
    payeeName: String,
    amount: String?,
    note: String
) {
    val uri = buildUpiUri(upiId, payeeName, amount, note)
    val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), "Pay with UPI")
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No UPI app found. Install Google Pay / PhonePe / Paytm.",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun buildUpiUri(
    upiId: String,
    payeeName: String,
    amount: String?, // null => no &am
    note: String,
    tr: String = "HP-" + System.currentTimeMillis()
): Uri {
    val sb = StringBuilder()
        .append("upi://pay")
        .append("?pa=").append(upiId)
        .append("&pn=").append(Uri.encode(payeeName))
    if (!amount.isNullOrBlank()) sb.append("&am=").append(Uri.encode(amount))
    sb.append("&tn=").append(Uri.encode(note))
        .append("&tr=").append(Uri.encode(tr))
        .append("&cu=INR")
    return Uri.parse(sb.toString())
}

private fun shareText(context: android.content.Context, title: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    context.startActivity(Intent.createChooser(intent, title))
}

private fun parseUpiResponse(response: String?): Map<String, String> {
    if (response.isNullOrBlank()) return emptyMap()
    return response.split("&").mapNotNull { pair ->
        val idx = pair.indexOf('=')
        if (idx <= 0) null
        else pair.substring(0, idx).trim().lowercase() to pair.substring(idx + 1).trim()
    }.toMap()
}

private fun mapUpiStatus(raw: String?): String {
    return when (raw?.uppercase()) {
        "SUCCESS"   -> "SUCCESS"
        "FAILURE"   -> "FAILURE"
        "SUBMITTED" -> "SUBMITTED"
        "PENDING"   -> "SUBMITTED"
        "CANCELLED" -> "CANCELLED"
        else        -> "UNKNOWN"
    }
}

/* ---------- Quick amount pills (unique name to avoid conflicts) ---------- */
@Composable
private fun QuickAmountPills(
    labels: List<String>,
    onClick: (String) -> Unit
) {
    Column {
        val maxPerRow = 3
        labels.chunked(maxPerRow).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { text ->
                    AssistChip(onClick = { onClick(text) }, label = { Text(text) })
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
