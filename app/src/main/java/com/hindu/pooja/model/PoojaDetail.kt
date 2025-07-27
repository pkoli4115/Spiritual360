package com.hindu.pooja.model

data class PoojaDetail(
    val id: String,
    val name: String,
    val language: String,
    val category: String? = null,
    val addedDate: String? = null,
    val content: Map<String, String>? = null,
    val slokas: List<String>? = null,
    val verses: List<String>? = null,
    val kathalu: List<Katha>? = null
)

data class Katha(
    val order: Int,
    val title: String,
    val content: String
)
