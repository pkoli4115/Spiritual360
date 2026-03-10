package com.hindu.pooja.ui.kids.flashcards

object FlashCardRoutes {

    // Base pattern → matches: flashcards/{categoryId}
    const val ROUTE_PATTERN = "flashcards/{categoryId}"

    // Helper to build a full route string safely
    fun create(categoryId: String): String =
        "flashcards/$categoryId"

    // ---- Explicit category routes (SAFEST, matches your JSON filenames) ----

    // 1) Know the Gods
    fun knowGods(): String =
        create("know_gods")

    // 2) Sloka / Aarti meanings
    fun slokaMeanings(): String =
        create("sloka_meanings")

    // 3) Ramayana Story Cards
    fun ramayanaStories(): String =
        create("ramayana_stories")
}
