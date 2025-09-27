package com.hindu.pooja.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.hindu.pooja.R
import com.hindu.pooja.data.PoojaLoader
import com.hindu.pooja.ui.components.HomeSection
import com.hindu.pooja.ui.navigation.Screen
import com.hindu.pooja.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment

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

    // Dismissible donation banner
    var showDonateBanner by rememberSaveable { mutableStateOf(true) }
    val upiId = "qtilabs@okhdfcbank"
    val payeeName = "HinduPooja App"
    val note = "Donation to HinduPooja App"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "🙏 Jai Sri Ram $userName",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                AnimatedVisibility(visible = showDonateBanner, enter = fadeIn(), exit = fadeOut()) {
                    DonateMiniBanner(
                        onDonate = {
                            launchUpiIntent(
                                context = context,
                                upiId = upiId,
                                payeeName = payeeName,
                                amount = "", // let user enter inside UPI app
                                note = note
                            )
                        },
                        onDetails = { navController.navigate(Screen.Profile.route) },
                        onDismiss = { showDonateBanner = false }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Example extra CTA
            item {
                Button(
                    onClick = { navController.navigate("find_it_game/hidden_objects_shiva_scene.json") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) { Text("🎯 Try Devotional Find-It Game") }
                Spacer(modifier = Modifier.height(8.dp))
            }

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "🙏 Support this project",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "This is a voluntary donation to support development. All features are free for everyone.",
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
fun ShimmerPlaceholderRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(180.dp)
                    .height(230.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium)
            )
        }
    }
}

private fun launchUpiIntent(
    context: android.content.Context,
    upiId: String,
    payeeName: String,
    amount: String = "",
    note: String = ""
) {
    val encodedNote = Uri.encode(note)
    val base = "upi://pay?pa=$upiId&pn=${Uri.encode(payeeName)}&cu=INR&tn=$encodedNote"
    val uri = if (amount.isNotBlank()) Uri.parse("$base&am=${Uri.encode(amount)}") else Uri.parse(base)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No UPI app found. Please install GPay/PhonePe/Paytm.", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open UPI. ${e.message}", Toast.LENGTH_LONG).show()
    }
}
