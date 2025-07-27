package com.hindu.pooja.model

data class PoojaIndexItem(
    val id: String,
    val name: String,
    val image: String,
    val file: String,
    val category: String, // ✅ Add this line
    val scrollable: Boolean = false // ✅ Optional, used in HomeScreen.kt
)
