package com.hindu.pooja.model

data class PoojaIndexItem(
    val id: String,
    val name: String,
    val image: String,
    val file: String,
    val category: String? = null,           // <-- Now optional
    val isPremium: Boolean = false,
    val scrollable: Boolean = false
)
