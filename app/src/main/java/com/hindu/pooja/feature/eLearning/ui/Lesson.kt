package com.hindu.pooja.feature.elearning.ui

/**
 * Minimal lesson model reused by all reader screens.
 * Keep it tiny so mapping from any JSON/source is trivial.
 */
data class Lesson(
    val id: String,
    val title: String,
    val content: String
)
