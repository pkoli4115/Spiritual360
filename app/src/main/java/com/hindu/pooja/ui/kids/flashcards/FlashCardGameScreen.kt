package com.hindu.pooja.ui.kids.flashcards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hindu.pooja.util.rememberSafePainter

@Composable
fun FlashCardGameScreen(
    navController: NavController,
    categoryId: String,          // "know_gods", "sloka_meanings", "ramayana_stories"
    viewModel: FlashCardViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(categoryId) {
        viewModel.loadCategory(context, categoryId)
    }

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.isError -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.errorMessage ?: "Error loading flash cards",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadCategory(context, categoryId) }) {
                        Text("Retry")
                    }
                }
            }
        }

        state.cards.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("No cards available.", style = MaterialTheme.typography.bodyLarge)
            }
        }

        state.isCompleted -> {
            FlashCardCompletedScreen(
                navController = navController,
                state = state,
                onRestart = { viewModel.restart() }
            )
        }

        else -> {
            FlashCardInProgressScreen(
                state = state,
                onOptionSelected = { index -> viewModel.onOptionSelected(index) },
                onNext = { viewModel.onNextCard() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun FlashCardInProgressScreen(
    state: FlashCardUiState,
    onOptionSelected: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val card = state.cards[state.currentIndex]
    val scrollState = rememberScrollState()

    // ---- Flip state + animation ----
    val density = LocalDensity.current
    val cameraDistancePx = with(density) { 16.dp.toPx() }

    // Flip state
    val (isFlipped, setIsFlipped) = remember { mutableStateOf(false) }

    // Reset flip whenever we move to a new question
    LaunchedEffect(state.currentIndex) {
        setIsFlipped(false)
    }

    // When user answers, flip to back side
    LaunchedEffect(state.hasAnswered) {
        if (state.hasAnswered) {
            setIsFlipped(true)
        }
    }

    val animatedRotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "flashCardFlip"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = state.setTitle.ifBlank { "Flash Cards" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Q ${state.currentIndex + 1} / ${state.cards.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // XP
        Text(
            text = "XP: ${state.currentXp}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ---- Flippable card ----
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    cameraDistance = cameraDistancePx
                    rotationY = animatedRotationY
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            if (animatedRotationY <= 90f) {
                // FRONT: Question + options
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Image (if any)
                    card.image?.let { imageName ->
                        Image(
                            painter = rememberSafePainter(imageName),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Question
                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Options
                    card.options.forEachIndexed { index, optionText ->
                        val isSelected = state.selectedOptionIndex == index

                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { onOptionSelected(index) },
                            enabled = !state.hasAnswered
                        ) {
                            Text(
                                text = optionText,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    if (!state.hasAnswered) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap an option to flip and see the explanation.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                // BACK: Explanation side
                // Apply inner 180° so text is not mirrored
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val resultText =
                        if (state.isCorrect == true) "✅ Correct!" else "❌ Not quite."

                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Answer: ${card.answer}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = card.explanation,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = {
                            // Move to next card and reset flip
                            setIsFlipped(false)
                            onNext()
                        }
                    ) {
                        Text("Next")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
private fun FlashCardCompletedScreen(
    navController: NavController,
    state: FlashCardUiState,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Well done!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You completed all ${state.cards.size} cards.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total XP: ${state.currentXp}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onRestart) {
                Text("Restart")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }
}
