package com.hindu.pooja.data

import kotlinx.serialization.Serializable

@Serializable
data class Pooja(
    val id: String,
    val name: String,
    val language: String,
    val content: Map<String, String>,
    val kathalu: List<Katha>,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    // optional metadata
    val loginProvider: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class Katha(
    val order: Int,
    val title: String,
    val content: String,
    val image: String? = null
)