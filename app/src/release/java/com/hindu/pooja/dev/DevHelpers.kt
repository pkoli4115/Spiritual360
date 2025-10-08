package com.hindu.pooja.dev

import com.hindu.pooja.viewmodel.RamakotiViewModel

/**
 * No-op stub for release builds.
 * This ensures the dev-only helper is not shipped in production.
 */
object DevHelpers {
    const val DEV_VISIBLE: Boolean = false
    fun fillRemaining(viewModel: RamakotiViewModel) { /* no-op */ }
}
