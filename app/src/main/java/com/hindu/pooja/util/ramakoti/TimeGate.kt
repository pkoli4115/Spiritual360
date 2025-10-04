package com.hindu.pooja.util.ramakoti

object TimeGate {
    // minimum typing time (ms) to discourage pasting/autofill
    private const val MIN_MS = 700L
    private const val MIN_LEN = 6

    fun isHumanLike(firstCharAt: Long, now: Long, currentText: String): Boolean {
        return (now - firstCharAt) >= MIN_MS && currentText.length >= MIN_LEN
    }
}
