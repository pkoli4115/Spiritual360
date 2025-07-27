package com.hindu.pooja.model

data class Katha(
    val order: Int = 0,
    val title: String = "",
    val content: String = ""
)

data class PoojaDetail(
    val id: String = "",
    val name: String = "",
    val language: String = "",
    val category: String = "",
    val addedDate: String = "",
    val content: String = "",                 // General intro or description (can be empty)
    val slokas: List<String>? = null,         // For stotrams/chalisas/etc.
    val kathalu: List<Katha>? = null,         // For vratham or stories, optional
    val verses: List<String>? = null          // For long stotrams or suktams, optional
    // Add more typed lists as you expand your app (e.g., steps, audio, imageList, etc.)
)
