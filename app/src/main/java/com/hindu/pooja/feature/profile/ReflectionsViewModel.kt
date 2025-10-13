package com.hindu.pooja.feature.profile
import com.hindu.pooja.feature.profile.data.ReflectionsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hindu.pooja.feature.profile.data.JourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReflectionsViewModel : ViewModel() {

    private val repo = ReflectionsRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    data class UiState(
        val items: List<ReflectionsRepository.Reflection> = emptyList(),
        val loading: Boolean = true,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(loading = true, error = null)
                val list = repo.list()
                _ui.value = _ui.value.copy(items = list, loading = false)
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, error = t.message ?: t.toString())
            }
        }
    }

    fun addReflection(text: String, onDone: (() -> Unit)? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repo.add(text)
                refresh()
                onDone?.invoke()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
    }

    fun deleteReflection(id: String) {
        viewModelScope.launch {
            try {
                repo.delete(id)
                refresh()
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: t.toString())
            }
        }
    }
}
