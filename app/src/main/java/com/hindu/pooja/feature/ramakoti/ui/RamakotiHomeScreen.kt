package com.hindu.pooja.feature.ramakoti.ui
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.rememberCoroutineScope
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import com.hindu.pooja.feature.ramakoti.prefs.RamakotiPreferences
import com.hindu.pooja.feature.ramakoti.reminders.ReminderScheduler
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hindu.pooja.feature.ramakoti.RamakotiViewModel
import com.hindu.pooja.feature.ramakoti.prefs.LanguagePreferenceManager
import com.hindu.pooja.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RamakotiHomeScreen(
    navController: NavController,
    vm: RamakotiViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { RamakotiPreferences.getInstance(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val reminderEnabled by prefs.reminderEnabled.collectAsState(initial = false)
    val reminderHour by prefs.reminderHour.collectAsState(initial = 7)
    val reminderMinute by prefs.reminderMinute.collectAsState(initial = 0)
    fun formatReminderTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%d:%02d %s", hour12, minute, amPm)
    }
    fun openReminderPicker() {
        val dialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                scope.launch {
                    prefs.setReminderEnabled(true)
                    prefs.setReminderHour(selectedHour)
                    prefs.setReminderMinute(selectedMinute)
                    scheduler.scheduleDaily(selectedHour, selectedMinute)
                    android.widget.Toast.makeText(
                        context,
                        "Daily reminder set for ${formatReminderTime(selectedHour, selectedMinute)}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            reminderHour,
            reminderMinute,
            true
        )
        dialog.show()
    }
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val langMgr = remember { LanguagePreferenceManager.getInstance(context) }

    LaunchedEffect(Unit) {
        vm.refreshFromServer()
    }

    val firstName by produceState(initialValue = "") {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            value = ""
            return@produceState
        }

        val fallback = auth.currentUser?.displayName
            ?.trim()
            ?.split(" ")
            ?.firstOrNull()
            .orEmpty()

        value = fallback

        runCatching {
            val snap = db.collection("users").document(uid).get().await()
            val fromDoc = snap.getString("fullName")
                ?: snap.getString("displayName")
                ?: snap.getString("name")
                ?: fallback

            value = fromDoc.trim().split(" ").firstOrNull().orEmpty()
        }
    }

    val selectedLanguage by produceState(initialValue = "") {
        val uid = auth.currentUser?.uid
        value = if (uid == null) "" else langMgr.languageFlowFor(uid).first().orEmpty()
    }

    val latestCertificateUrl by produceState<String?>(initialValue = null) {
        val uid = auth.currentUser?.uid ?: return@produceState

        runCatching {
            val historySnap = db.collection("users")
                .document(uid)
                .collection("ramakotiHistory")
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val fromHistory = historySnap.documents.firstOrNull()?.getString("certificateUrl")
            if (!fromHistory.isNullOrBlank()) {
                value = fromHistory
                return@runCatching
            }

            val runsSnap = db.collection("users")
                .document(uid)
                .collection("ramakotiRuns")
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()

            value = runsSnap.documents
                .firstOrNull { !it.getString("certificateUrl").isNullOrBlank() }
                ?.getString("certificateUrl")
        }
    }
    val certificateCount by produceState(initialValue = 0) {
        val uid = auth.currentUser?.uid ?: return@produceState

        runCatching {
            val historySnap = db.collection("users")
                .document(uid)
                .collection("ramakotiHistory")
                .get()
                .await()

            value = historySnap.size()
        }
    }

    val latestCertificateTitle by produceState(initialValue = "") {
        val uid = auth.currentUser?.uid ?: return@produceState

        runCatching {
            val historySnap = db.collection("users")
                .document(uid)
                .collection("ramakotiHistory")
                .orderBy("completedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val latest = historySnap.documents.firstOrNull()
            val target = latest?.getLong("targetCount")?.toInt()

            value = when (target) {
                10 -> "10 Ram Naam"
                100 -> "100 Ram Naam"
                1000 -> "1,000 Ram Naam"
                100_000 -> "1 Lakh Ram Naam"
                1_000_000 -> "10 Lakh Ram Naam"
                10_000_000 -> "1 Crore Ram Naam"
                null -> ""
                else -> "$target Ram Naam"
            }
        }
    }
    val greetingName = firstName.ifBlank { "Devotee" }

    val targetProgress = if (ui.targetCount > 0) {
        (ui.runTotal.toFloat() / ui.targetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val batchProgress = (ui.currentBatchCount / 108f).coerceIn(0f, 1f)
    val remainingInBatch = max(0, 108 - ui.currentBatchCount)
    val progressPercent = (targetProgress * 100).toInt()

    val progressMessage = when {
        ui.targetReached -> "Your sacred target is complete."
        ui.currentBatchCount == 0 && ui.runTotal == 0 -> "Begin your first sacred batch of 108."
        remainingInBatch == 0 -> "Sacred batch completed."
        else -> "$remainingInBatch more to complete this sacred batch."
    }

    val languageLabel = when (selectedLanguage) {
        "te" -> "Telugu"
        "hi" -> "Hindi"
        "en" -> "English"
        else -> "Not selected"
    }

    val homeGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF3E3),
            Color(0xFFFFF8EE),
            Color(0xFFFFFBEF)
        )
    )

    val heroGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFE0B2),
            Color(0xFFFFF3E0)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFF3E3)
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Jai Shri Ram, $greetingName 🙏",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B4513)
                        )
                        Text(
                            text = "Let every nama bring peace, focus, and devotion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B4F2A)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(homeGradient)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroGradient)
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "Your Ramakoti Journey",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A3E00)
                        )

                        Spacer(Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HomeStatChip("Language: $languageLabel")
                            HomeStatChip("Current Batch: ${ui.currentBatchNumber}")
                            HomeStatChip("Current Crore: ${ui.currentCrore}")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = targetProgress,
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 10.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$progressPercent%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Target",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(Modifier.size(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Target Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "${ui.runTotal} / ${ui.targetCount}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = targetProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Surface(
                        color = Color(0xFFFFF4E8),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "Batch ${ui.currentBatchNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "${ui.currentBatchCount} / 108",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = batchProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = progressMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B4F2A)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Lifetime Count",
                    value = ui.lifetimeCount.toString()
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Current Run",
                    value = ui.runTotal.toString()
                )
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    if (selectedLanguage.isBlank()) {
                        navController.navigate(Screen.RamakotiLanguage.route)
                    } else {
                        navController.navigate(Screen.RamakotiWriter.route)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (ui.runTotal > 0) "Continue Writing" else "Start Writing"
                )
            }

            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openReminderPicker() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    1.dp,
                    if (reminderEnabled) Color(0xFFFFC46B) else Color(0xFFE0E0E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (reminderEnabled) Color(0xFFFFE8C7) else Color(0xFFF4F4F4)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔔",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reminder",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = if (reminderEnabled) {
                                "Daily at ${formatReminderTime(reminderHour, reminderMinute)}"
                            } else {
                                "Not set — tap to enable daily reminder"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B4F2A)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Maa Asayam",
                    subtitle = if (selectedLanguage.isBlank()) {
                        "Read in Telugu, English, or Hindi"
                    } else {
                        "Read in $languageLabel"
                    },
                    icon = Icons.Filled.MenuBook,
                    onClick = { navController.navigate(Screen.RamakotiIntro.route) }
                )
                CertificateCard(
                    modifier = Modifier.weight(1f),
                    certificateCount = certificateCount,
                    latestCertificateTitle = latestCertificateTitle,
                    onClick = {
                        latestCertificateUrl?.let { url ->
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(url)
                            )
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Journey Highlights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (ui.lifetimeCount >= 1) HomeMilestoneChip("First Nama")
                if (ui.lifetimeCount >= 108) HomeMilestoneChip("108 Completed")
                if (ui.lifetimeCount >= 1000) HomeMilestoneChip("1000 Nama")
                if (ui.lifetimeCount >= 100_000) HomeMilestoneChip("1 Lakh Journey")
                if (ui.targetReached) HomeMilestoneChip("Target Complete ✨")
            }

            Spacer(Modifier.height(18.dp))

            CertificateProgressSection(
                lifetimeCount = ui.lifetimeCount
            )
            Spacer(Modifier.height(18.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE2B5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF8B4513)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Daily Inspiration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "One nama at a time, one step closer to your sankalpam.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5A4632)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}
@Composable
private fun CertificateCard(
    modifier: Modifier = Modifier,
    certificateCount: Int,
    latestCertificateTitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = Color(0xFF8B4513)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Certificates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            if (certificateCount > 0) {
                Text(
                    text = "$certificateCount Sacred Milestones Earned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B4F2A),
                    fontWeight = FontWeight.Medium
                )

                if (latestCertificateTitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Latest: $latestCertificateTitle",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B4513)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Tap to view latest certificate",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Complete a target to unlock your first certificate",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B4F2A)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeStatChip(text: String) {
    FilterChip(
        selected = false,
        onClick = { },
        label = { Text(text) }
    )
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B4F2A)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF8B4513))
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B4F2A)
            )
        }
    }
}

@Composable
private fun HomeMilestoneChip(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFE8C7)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B4F2A)
        )
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CertificateProgressSection(lifetimeCount: Int) {
    val milestones = listOf(
        108 to "108 Nama",
        1_000 to "1,000 Nama",
        10_000 to "10,000 Nama",
        100_000 to "1 Lakh",
        1_000_000 to "10 Lakh",
        10_000_000 to "1 Crore"
    )

    Column {
        Text(
            text = "Sacred Milestones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            milestones.forEach { (count, label) ->
                val unlocked = lifetimeCount >= count
                SacredMilestoneCard(
                    label = label,
                    unlocked = unlocked
                )
            }
        }
    }
}

@Composable
private fun SacredMilestoneCard(
    label: String,
    unlocked: Boolean
) {
    val bgColor = if (unlocked) Color(0xFFFFE8C7) else Color(0xFFF2F2F2)
    val textColor = if (unlocked) Color(0xFF6B4F2A) else Color(0xFF888888)
    val borderColor = if (unlocked) Color(0xFFFFC46B) else Color(0xFFD8D8D8)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (unlocked) "🏅" else "🔒",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = if (unlocked) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}