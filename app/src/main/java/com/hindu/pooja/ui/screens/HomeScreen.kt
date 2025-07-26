package com.hindu.pooja.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"

    val categories = listOf(
        "Daily Poojas" to "daily_index_te.json",
        "Vrathams / Nomulu" to "vrathams_index_te.json",
        "Festival Poojas" to "festival_index_te.json",
        "Sahasranamas" to "sahasranamas_index_te.json",
        "Ashtottaras" to "ashtottaras_index_te.json"
    )

    var selectedCategory by remember { mutableStateOf("Daily Poojas") }
    var isLoading by remember { mutableStateOf(true) }

    val categoryData = remember {
        categories.map { (title, fileName) ->
            title to PoojaLoader.loadPoojaIndex(context, fileName)
        }
    }

    // Simulate loading state for shimmer effect
    LaunchedEffect(Unit) {
        delay(1000)
        isLoading = false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.home_screen),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(this.maxHeight)
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
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        navController.navigate("find_it_game/hidden_objects_shiva_scene.json")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Text("🎯 Try Devotional Find-It Game")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            categoryData.forEach { (title, poojaList) ->
                item {
                    if (isLoading) {
                        ShimmerPlaceholderRow()
                    } else {
                        HomeSection(
                            sectionTitle = title,
                            items = poojaList,
                            isSelected = (title == selectedCategory),
                            onItemClick = { item ->
                                selectedCategory = title
                                navController.navigate("poojaDetail/${item.file}")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ShimmerPlaceholderRow() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(180.dp)
                    .height(230.dp)
                    .background(brush = brush, shape = MaterialTheme.shapes.medium)
            )
        }
    }
}
