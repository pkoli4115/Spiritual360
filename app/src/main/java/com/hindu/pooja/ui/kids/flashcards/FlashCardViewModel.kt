package com.hindu.pooja.ui.kids.flashcards

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FlashCardUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val categoryId: String = "",
    val setTitle: String = "",
    val cards: List<FlashCard> = emptyList(),
    val currentIndex: Int = 0,
    val currentXp: Int = 0,
    val hasAnswered: Boolean = false,
    val selectedOptionIndex: Int? = null,
    val isCorrect: Boolean? = null,
    val isCompleted: Boolean = false
)

class FlashCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FlashCardUiState())
    val uiState: StateFlow<FlashCardUiState> = _uiState.asStateFlow()

    fun loadCategory(context: Context, categoryId: String) {
        val current = _uiState.value
        // Avoid reloading if already loaded
        if (!current.isLoading && current.categoryId == categoryId && current.cards.isNotEmpty()) {
            return
        }

        _uiState.value = FlashCardUiState(
            isLoading = true,
            categoryId = categoryId
        )

        viewModelScope.launch {
            try {
                val set = FlashCardRepository.loadSetFromAssets(context, categoryId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = false,
                    errorMessage = null,
                    setTitle = set.title,
                    cards = set.cards,
                    currentIndex = 0,
                    currentXp = 0,
                    hasAnswered = false,
                    selectedOptionIndex = null,
                    isCorrect = null,
                    isCompleted = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "Failed to load flash cards"
                )
            }
        }
    }

    fun onOptionSelected(optionIndex: Int) {
        val state = _uiState.value
        if (state.hasAnswered || state.cards.isEmpty() || state.isCompleted) return

        val card = state.cards[state.currentIndex]
        val selected = card.options.getOrNull(optionIndex) ?: return
        val correct = selected == card.answer

        val xpGain = if (correct) 10 else 5

        _uiState.value = state.copy(
            hasAnswered = true,
            selectedOptionIndex = optionIndex,
            isCorrect = correct,
            currentXp = state.currentXp + xpGain
        )
        // 🔹 Do NOT mark isCompleted here – we still want to show the explanation + Next button
    }

    fun onNextCard() {
        val state = _uiState.value
        if (state.cards.isEmpty() || state.isCompleted) return

        if (state.currentIndex < state.cards.lastIndex) {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                hasAnswered = false,
                selectedOptionIndex = null,
                isCorrect = null
            )
        } else {
            // Last card → now mark completed
            _uiState.value = state.copy(isCompleted = true)
        }
    }

    fun restart() {
        val state = _uiState.value
        if (state.cards.isEmpty()) return

        _uiState.value = state.copy(
            currentIndex = 0,
            currentXp = 0,
            hasAnswered = false,
            selectedOptionIndex = null,
            isCorrect = null,
            isCompleted = false
        )
    }
}
