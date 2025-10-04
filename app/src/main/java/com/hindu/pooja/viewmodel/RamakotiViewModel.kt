package com.hindu.pooja.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hindu.pooja.feature.ramakoti.data.RamakotiRepository
import com.hindu.pooja.model.ramakoti.CellState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RamakotiViewModel @Inject constructor(
    private val repo: RamakotiRepository
) : ViewModel() {

    private var currentBatchId: String? = null

    private val _cells = MutableStateFlow<List<CellState>>(emptyList())
    val cells: StateFlow<List<CellState>> = _cells.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _completed = MutableStateFlow(0)
    val completed: StateFlow<Int> = _completed.asStateFlow()

    private val _totalCommitted = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCommitted.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private val _audioMode = MutableStateFlow(false)
    val audioMode: StateFlow<Boolean> = _audioMode.asStateFlow()

    // ✅ Threshold temporarily lowered for testing; revert to 108 for production
    private val BATCH_SIZE = 108

    fun init(language: String) {
        viewModelScope.launch {
            try {
                _language.value = language
                Log.d("RamakotiVM", "🟢 init(language=$language)")

                // load or create a batch
                var pair = repo.getActiveBatchOrNull(language)
                if (pair.first == null) {
                    val created = repo.createNewInProgressBatch(language)
                    pair = created.first to created.second
                    Log.d("RamakotiVM", "🆕 Created new in-progress batch ${pair.first}")
                }

                currentBatchId = pair.first
                _cells.value = rawToCellState(pair.second, language)
                _completed.value = _cells.value.count { it.filled }

                // read committed-only total
                _totalCommitted.value = repo.readCommittedTotalCount() ?: 0
                _showCelebration.value = false

                // edge case: last batch already complete
                if (_completed.value >= BATCH_SIZE && currentBatchId != null) {
                    val finishedId = currentBatchId!!
                    Log.d("RamakotiVM", "⚙️ Auto-committing already complete batch $finishedId")
                    repo.commitBatchAndIncrementTotal(finishedId)
                    _totalCommitted.value = repo.readCommittedTotalCount() ?: 0
                    _showCelebration.value = true
                    Log.d("RamakotiVM", "🎉 Celebration triggered (auto-commit) for $finishedId")

                    val (newId, newCells) = repo.createNewInProgressBatch(_language.value)
                    currentBatchId = newId
                    _cells.value = rawToCellState(newCells, _language.value)
                    _completed.value = 0
                }

            } catch (e: Exception) {
                Log.e("RamakotiVM", "❌ init() failed", e)
            }
        }
    }

    fun switchLanguage(lang: String) { _language.value = lang }
    fun setAudioMode(enabled: Boolean) { _audioMode.value = enabled }
    fun dismissCelebration() { _showCelebration.value = false }
    fun undoLastFillNoop() { /* reserved for undo later */ }

    fun fillNextCellWithMantra() {
        viewModelScope.launch {
            try {
                val batchId = currentBatchId ?: return@launch

                val nextEmpty = _cells.value.firstOrNull { !it.filled } ?: return@launch
                val now = System.currentTimeMillis()
                val mantra = when (_language.value.lowercase()) {
                    "hi" -> "जय श्री राम"
                    "te" -> "జై శ్రీ రామ్"
                    else -> "Jai Shri Ram"
                }

                repo.fillCell(batchId, nextEmpty.index, mantra, _language.value, now)
                Log.d("RamakotiVM", "✍️ Filled cell ${nextEmpty.index} in batch $batchId")

                // refresh UI
                val refreshed = repo.getActiveBatchOrNull(_language.value)
                currentBatchId = refreshed.first
                _cells.value = rawToCellState(refreshed.second, _language.value)
                _completed.value = _cells.value.count { it.filled }
                Log.d("RamakotiVM", "📊 Progress updated = ${_completed.value}/$BATCH_SIZE")

                // check threshold
                if (_completed.value >= BATCH_SIZE) {
                    Log.d("RamakotiVM", "✅ Batch $batchId complete; committing...")
                    repo.commitBatchAndIncrementTotal(batchId)
                    _totalCommitted.value = repo.readCommittedTotalCount() ?: 0
                    _showCelebration.value = true
                    Log.d("RamakotiVM", "🎉 Celebration triggered (manual complete) for $batchId")

                    // create next batch
                    val (newId, newCells) = repo.createNewInProgressBatch(_language.value)
                    currentBatchId = newId
                    _cells.value = rawToCellState(newCells, _language.value)
                    _completed.value = 0
                }

            } catch (e: Exception) {
                Log.e("RamakotiVM", "❌ fillNextCellWithMantra failed", e)
            }
        }
    }

    /* ----------------------- helpers ----------------------- */

    private fun rawToCellState(raw: List<Any?>, langDefault: String): List<CellState> {
        return raw.mapIndexed { i, anyItem ->
            val m: Map<*, *> = (anyItem as? Map<*, *>) ?: emptyMap<String, Any?>()
            val filled: Boolean = (m["filled"] as? Boolean) == true
            val value: String = (m["value"] as? String).orEmpty()
            val lang: String = (m["lang"] as? String) ?: langDefault.uppercase()
            val ts: Long = (m["ts"] as? Number)?.toLong() ?: 0L

            CellState(
                index = i + 1,
                filled = filled,
                displayText = if (filled) value else "",
                lang = lang,
                ts = ts
            )
        }
    }
}
