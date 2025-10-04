package com.hindu.pooja.model.ramakoti

data class CellState(
    val index: Int,
    val filled: Boolean = false,
    val displayText: String = "", // what we show inside the cell (final, locked)
    val lang: String = "en",      // "en" | "hi" | "te"
    val ts: Long = 0L
)
