package com.hindu.pooja.dev

import androidx.lifecycle.viewModelScope
import com.hindu.pooja.BuildConfig
import com.hindu.pooja.viewmodel.RamakotiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Dev-only helper for rapid testing of Ramakoti grid.
 * This code only exists in debug builds.
 */
object DevHelpers {
    /** UI visibility flag for dev gestures */
    const val DEV_VISIBLE: Boolean = true

    /**
     * Quickly fills all remaining cells by repeatedly calling
     * the same ViewModel action a user would normally trigger.
     */
    fun fillRemaining(viewModel: RamakotiViewModel) {
        check(BuildConfig.DEV_ONLY) { "Dev helper must never run in release builds." }

        viewModel.viewModelScope.launch {
            while (isActive && BuildConfig.DEV_ONLY) {
                val progress = viewModel.completed.value // 0..108
                if (progress >= 108) break
                viewModel.fillNextCellWithMantra()
                delay(20) // ~50 cells/sec, safe for Firestore
            }
        }
    }
}
