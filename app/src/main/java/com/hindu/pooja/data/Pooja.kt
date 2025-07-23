package com.hindu.pooja.data

import kotlinx.serialization.Serializable

@Serializable
data class Pooja(
    val id: String,
    val name: String,
    val language: String,
    val content: Map<String, String>,
    val kathalu: List<Katha>
)

@Serializable
data class Katha(
    val order: Int,
    val title: String,
    val content: String
)
