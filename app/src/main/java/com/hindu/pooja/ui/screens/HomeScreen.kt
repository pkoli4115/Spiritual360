package com.hindu.pooja.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.R
import com.hindu.pooja.data.PoojaLoader
import com.hindu.pooja.ui.components.HomeSection
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

    val categories = listOf(
        "Daily Poojas" to "daily_index_te.json",
        "Vrathams / Nomulu" to "vrathams_index_te.json",
        "Festival Poojas" to "festival_index_te.json",
        "Sahasranamas" to "sahasranamas_index_te.json",
        "Ashtottaras" to "ashtottaras_index_te.json"
    )
    val autoScrollSections = setOf("Daily Poojas", "Ashtottaras")

    var selectedCategory by remember { mutableStateOf("Daily Poojas") }
    var isLoading by remember { mutableStateOf(true) }

    val categoryData = remember {
        categories.map { (title, fileName) ->
            title to PoojaLoader.loadPoojaIndex(context, fileName)
        }
    }

    LaunchedEffect(Unit) {
        delay(1000)
        isLoading = false
    }

    var showDonateRow by rememberSaveable { mutableStateOf(true) }

    val upiId = DonationConfig.upiId
    val payeeName = DonationConfig.payeeName
    val note = DonationConfig.note

    @Suppress BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_screen),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Greeting
            item {
                Text(
                    text = "🙏 Jai Sri Ram $userName",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // --- Featured header + cards row ---
            item {
                FeaturedSectionHeader(
                    onViewAll = { navController.navigate(Screen.Featured.route) }
                )
                Spacer(Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FeaturedRamakotiCard(
                            onOpen = { navController.navigate(Screen.Ramakoti.route) }
                        )
                    }
                    item {
                        // Keep this opening the wiki/reader flow
                        FeaturedBalaKandaCard(
                            onOpen = { navController.navigate(Screen.BalaKandaWikiSimple.route) }
                        )
                    }
                    item {
                        // Ayodhyakanda — Reader entry
                        FeaturedAyodhyaCard(
                            onOpen = { navController.navigate("ramayana/ayodhya/wiki") }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Donate row
            if (showDonateRow) {
                item {
                    DonateActionRow(
                        onDonate = {
                            launchUpiChooserIntent(
                                context = context,
                                upiId = upiId,
                                payeeName = payeeName,
                                amount = null,
                                note = note
                            )
                        },
                        onDetails = { navController.navigate(Screen.Profile.route) },
                        onDismiss = { showDonateRow = false }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Find-It CTA
            item {
                Button(
                    onClick = { navController.navigate("find_it_game/hidden_objects_shiva_scene.json") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) { Text("🎯 Try Devotional Find-It Game") }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content sections
            categoryData.forEach { (title, poojaList) ->
                val filteredItems = if (title == "Daily Poojas") {
                    poojaList.filter { it.scrollable == true }
                } else poojaList

                val autoScroll = autoScrollSections.contains(title)

                if (!isLoading && filteredItems.isNotEmpty()) {
                    item {
                        HomeSection(
                            sectionTitle = title,
                            items = filteredItems,
                            isSelected = (title == selectedCategory),
                            autoScroll = autoScroll,
                            onItemClick = { item ->
                                selectedCategory = title
                                navController.navigate(
                                    Screen.PoojaDetail.createRoute(fileName = item.file)
                                )
                            },
                            onViewAllClick = {
                                val route = when (title) {
                                    "Daily Poojas" -> Screen.Poojas.createRoute("daily_index_te.json")
                                    "Vrathams / Nomulu" -> Screen.Vrathams.createRoute("vrathams_index_te.json")
                                    "Ashtottaras" -> Screen.Ashtottaras.createRoute("ashtottaras_index_te.json")
                                    else -> "unknown_list"
                                }
                                navController.navigate(route)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else if (isLoading) {
                    item { ShimmerPlaceholderRow() }
                }
            }
        }
    }
}

/* ---------------- Featured UI ---------------- */

private val FeaturedCardWidth = 300.dp
private val FeaturedCardHeight = 176.dp

@Composable
private fun FeaturedSectionHeader(onViewAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Featured",
            // 🔶 Match “Daily Poojas” saffron
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "View All  ➜",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { onViewAll() }
                .padding(4.dp)
        )
    }
}

@Composable
private fun FeaturedRamakotiCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .width(FeaturedCardWidth)
            .height(FeaturedCardHeight)
            .clickable { onOpen() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Ramakoti", style = MaterialTheme.typography.titleMedium)
            Text(
                "Write Jai Sri Ram (English / हिंदी / తెలుగు). Unlimited. Daily streaks, reminders & cloud sync.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f)) // keep buttons aligned at bottom
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Text("Open Ramakoti")
            }
        }
    }
}

@Composable
private fun FeaturedBalaKandaCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .width(FeaturedCardWidth)
            .height(FeaturedCardHeight)
            .clickable { onOpen() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Bala Kanda — Lessons", style = MaterialTheme.typography.titleMedium)
            Text(
                "Child-friendly Telugu lessons + quiz (simple reader view).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Text("Open Bala Kanda")
            }
        }
    }
}

@Composable
private fun FeaturedAyodhyaCard(onOpen: () -> Unit) {
    Card(
        modifier = Modifier
            .width(FeaturedCardWidth)
            .height(FeaturedCardHeight)
            .clickable { onOpen() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Ayodhya Kanda — Lessons", style = MaterialTheme.typography.titleMedium)
            Text(
                "Ayodhyakanda in Telugu (reader view) with TTS.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Text("Open Ayodhya Kanda")
            }
        }
    }
}

/* ---------------- Donate row & placeholders ---------------- */
// (remaining code unchanged)

@Composable
private fun DonateActionRow(
    onDonate: () -> Unit,
    onDetails: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Button(onClick = onDonate, modifier = Modifier.weight(1f)) { Text("Donate") }
        OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f)) { Text("Details") }
        TextButton(onClick = onDismiss) { Text("Dismiss") }
    }
}

@Composable
fun ShimmerPlaceholderRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(180.dp)
                    .height(230.dp)
                    .background(
                        Color.LightGray.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    )
            )
        }
    }
}

/* ---------------- UPI helpers ---------------- */

private fun buildUpiUri(
    upiId: String,
    payeeName: String,
    amount: String?,
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

private fun launchUpiChooserIntent(
    context: android.content.Context,
    upiId: String,
    payeeName: String,
    amount: String?,
    note: String
) {
    val uri = buildUpiUri(upiId, payeeName, amount, note)
    val base = Intent(Intent.ACTION_VIEW, uri)
    val chooser = Intent.createChooser(base, "Pay with UPI")
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No UPI app found. Please install Google Pay, PhonePe, or Paytm.",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open UPI. ${e.message}", Toast.LENGTH_LONG).show()
    }
}
