@file:OptIn(
    kotlinx.serialization.ExperimentalSerializationApi::class,
    kotlinx.serialization.InternalSerializationApi::class
)
package com.hindu.pooja.ui.kids.flashcards

import kotlinx.serialization.Serializable

@Serializable
data class FlashCard(
    val id: String,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String = "",
    val image: String? = null
)

@Serializable
data class FlashCardSet(
    val id: String,
    val title: String,
    val description: String? = null,
    val cards: List<FlashCard> = emptyList()
)
