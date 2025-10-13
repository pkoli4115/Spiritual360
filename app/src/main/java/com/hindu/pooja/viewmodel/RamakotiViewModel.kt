package com.hindu.pooja.feature.ramakoti.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hindu.pooja.feature.ramakoti.data.RamakotiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel exposes:
 * - grid (108 cells) state for current active batch
 * - totalCount / croreCount stats
 * - language preference
 * - actions: tap cell, autofill remaining, switch language
 *
 * No Compose imports here; pure state flows so UI can observe safely.
 */
class RamakotiViewModel(
    private val repo: RamakotiRepository = RamakotiRepository()
) : ViewModel() {

    // ---------------- UI State Models ----------------
    data class GridState(
        val batchId: String? = null,
        val index: Long = 0L,
        val cells: List<Boolean> = List(108) { false },
        val filled: Int = 0,
        val committed: Boolean = false
    ) {
        val isComplete: Boolean get() = filled >= 108
    }

    data class StatsState(
        val totalCount: Long = 0L,
        val croreCount: Int = 0,
        val currentCroreNumber: Int = 1,
        val language: String = "EN"
    )

    data class UiState(
        val loading: Boolean = true,
        val error: String? = null,
        val grid: GridState = GridState(),
        val stats: StatsState = StatsState()
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var currentUid: String? = null

    // ---------------- Public API ----------------

    fun init(uid: String) {
        if (uid == currentUid && !_ui.value.loading) return
        currentUid = uid
        _ui.update { it.copy(loading = true, error = null) }

        // Load stats + ensure an active batch
        viewModelScope.launch {
            try {
                // Observe stats live
                observeStats(uid)

                // Ensure batch
                val batch = repo.ensureActiveBatch(uid)
                _ui.update {
                    it.copy(
                        loading = false,
                        grid = it.grid.copy(
                            batchId = batch.id,
                            index = batch.index,
                            cells = batch.cells,
                            filled = batch.filled,
                            committed = batch.committed
                        )
                    )
                }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = e.message ?: "Failed to initialize") }
            }
        }
    }

    fun setLanguage(language: String) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            try {
                repo.setLanguage(uid, language)
                _ui.update { it.copy(stats = it.stats.copy(language = language)) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun tapCell(cellIndex: Int, value: Boolean) {
        val uid = currentUid ?: return
        val batchId = _ui.value.grid.batchId ?: return
        if (cellIndex !in 0..107) return

        viewModelScope.launch {
            try {
                val updated = repo.toggleCell(uid, batchId, cellIndex, value)
                _ui.update {
                    it.copy(
                        grid = it.grid.copy(
                            batchId = updated.id,
                            index = updated.index,
                            cells = updated.cells,
                            filled = updated.filled,
                            committed = updated.committed
                        ),
                        // If the batch got committed, we may now have a new active batch; preload it
                        loading = false
                    )
                }

                if (updated.committed) {
                    // Preload the next active batch so UI remains smooth
                    val next = repo.ensureActiveBatch(uid)
                    _ui.update {
                        it.copy(
                            grid = it.grid.copy(
                                batchId = next.id,
                                index = next.index,
                                cells = next.cells,
                                filled = next.filled,
                                committed = next.committed
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    /** Long-press helper to auto-fill remaining cells to 108. */
    fun autofillRemaining() {
        val uid = currentUid ?: return
        viewModelScope.launch {
            try {
                val updated = repo.fillRemaining(uid)
                _ui.update {
                    it.copy(
                        grid = it.grid.copy(
                            batchId = updated.id,
                            index = updated.index,
                            cells = updated.cells,
                            filled = updated.filled,
                            committed = updated.committed
                        )
                    )
                }
                if (updated.committed) {
                    val next = repo.ensureActiveBatch(uid)
                    _ui.update {
                        it.copy(
                            grid = it.grid.copy(
                                batchId = next.id,
                                index = next.index,
                                cells = next.cells,
                                filled = next.filled,
                                committed = next.committed
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    // ---------------- Internals ----------------
    private fun observeStats(uid: String) {
        viewModelScope.launch {
            repo.observeStats(uid).collect { s ->
                _ui.update {
                    it.copy(
                        stats = it.stats.copy(
                            totalCount = s.totalCount,
                            croreCount = s.croreCount,
                            currentCroreNumber = s.currentCroreNumber,
                            language = s.language
                        )
                    )
                }
            }
        }
        // Also do a one-time fetch to ensure doc exists and fill initial values fast
        viewModelScope.launch {
            try {
                val s = repo.getStats(uid)
                _ui.update {
                    it.copy(
                        stats = it.stats.copy(
                            totalCount = s.totalCount,
                            croreCount = s.croreCount,
                            currentCroreNumber = s.currentCroreNumber,
                            language = s.language
                        )
                    )
                }
            } catch (_: Exception) { /* ignore */ }
        }
    }
}
