package com.hindu.pooja.app.util

object AccessibilityUtils {
    val MIN_FONT_SCALE = 1.0f
    val MAX_FONT_SCALE = 2.0f
    val FONT_STEP = 0.1f

    fun increaseFontScale(current: Float): Float {
        return (current + FONT_STEP).coerceAtMost(MAX_FONT_SCALE)
    }

    fun decreaseFontScale(current: Float): Float {
        return (current - FONT_STEP).coerceAtLeast(MIN_FONT_SCALE)
    }
}
