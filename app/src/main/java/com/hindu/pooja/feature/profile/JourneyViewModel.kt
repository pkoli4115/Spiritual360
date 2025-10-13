package com.hindu.pooja.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.hindu.pooja.feature.profile.data.JourneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JourneyViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val repo = JourneyRepository(auth, db)

    data class UiState(
        val totalCount: Long = 0L,
        val currentBatchCount: Int = 0,
        val currentCrore: Int = 1,
        val language: String = "en",
        val badges: List<String> = emptyList(),
        val history: List<JourneyRepository.CroreHistoryItem> = emptyList(),
        val loading: Boolean = true,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init {
        refresh()
        // Real-time listener for history newest-first
        listenHistory()
        // (Optional) You can also add a listener for the journey doc if you want true realtime
        // For now we just refresh at init; call refresh() after actions if needed.
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(loading = true, error = null)
                val j = repo.getJourney()
                _ui.value = _ui.value.copy(
                    totalCount = j.totalCount,
                    currentBatchCount = j.currentBatchCount,
                    currentCrore = j.currentCrore,
                    language = j.language,
                    badges = computeBadges(j.totalCount),
                    loading = false
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, error = t.message ?: t.toString())
            }
        }
    }

    private fun listenHistory() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("ramakotiHistory")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    _ui.value = _ui.value.copy(error = e.message ?: e.toString())
                    return@addSnapshotListener
                }
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.map { d ->
                    JourneyRepository.CroreHistoryItem(
                        id = d.id,
                        croreNumber = (d.getLong("croreNumber") ?: 0L).toInt(),
                        completedAtMs = d.getTimestamp("completedAt")?.toDate()?.time,
                        totalAtCompletion = d.getLong("totalAtCompletion") ?: 0L,
                        certificateId = d.getString("certificateId"),
                        certificateUrl = d.getString("certificateUrl")
                    )
                }
                _ui.value = _ui.value.copy(history = list, error = null)
            }
    }

    private fun computeBadges(totalCount: Long): List<String> {
        val out = mutableListOf<String>()
        if (totalCount >= 100_000L) out += "1 Lakh"
        if (totalCount >= 1_000_000L) out += "10 Lakhs"
        if (totalCount >= 10_000_000L) out += "1 Crore"
        return out
    }
}
