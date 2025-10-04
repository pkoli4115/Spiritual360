package com.hindu.pooja.viewmodel

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

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    private val _audioMode = MutableStateFlow(false)
    val audioMode: StateFlow<Boolean> = _audioMode.asStateFlow()

    fun init(language: String) {
        viewModelScope.launch {
            _language.value = language

            // Try to load an existing in-progress batch
            var pair: Pair<String?, List<Any?>> = repo.getActiveBatchOrNull(language)
            // If none, create one so the grid never appears empty
            if (pair.first == null) {
                val created = repo.createNewInProgressBatch(language)
                pair = created.first to created.second
            }

            currentBatchId = pair.first
            val raw: List<Any?> = pair.second
            _cells.value = rawToCellState(raw, language)
            _completed.value = _cells.value.count { it.filled }
            _totalCount.value = repo.readTotalCount() ?: 0
            _showCelebration.value = false
        }
    }

    fun switchLanguage(lang: String) { _language.value = lang }
    fun setAudioMode(enabled: Boolean) { _audioMode.value = enabled }
    fun dismissCelebration() { _showCelebration.value = false }
    fun undoLastFillNoop() { /* reserved for future */ }

    fun fillNextCellWithMantra() {
        viewModelScope.launch {
            val batchId = currentBatchId ?: return@launch

            val next = _cells.value.firstOrNull { !it.filled } ?: return@launch

            val now = System.currentTimeMillis()
            val text = when (_language.value.lowercase()) {
                "hi" -> "जय श्री राम"
                "te" -> "జై శ్రీ రామ్"
                else -> "Jai Shri Ram"
            }

            // Persist the next cell
            repo.fillCell(
                batchId = batchId,
                index = next.index,
                value = text,
                lang = _language.value,
                ts = now
            )

            // Refresh current batch state
            val refreshed = repo.getActiveBatchOrNull(_language.value)
            currentBatchId = refreshed.first
            _cells.value = rawToCellState(refreshed.second, _language.value)
            _completed.value = _cells.value.count { it.filled }

            // If completed -> commit & immediately start a brand-new batch
            if (_completed.value == 108) {
                repo.commitBatchAndIncrementTotal(batchId)

                // Update lifetime total from server (falls back to +108 if null)
                _totalCount.value = repo.readTotalCount() ?: (_totalCount.value + 108)
                _showCelebration.value = true

                // 🔁 Auto-create and load the next in-progress batch
                val (newId, newCells) = repo.createNewInProgressBatch(_language.value)
                currentBatchId = newId
                _cells.value = rawToCellState(newCells, _language.value)
                _completed.value = 0
            }
        }
    }

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
