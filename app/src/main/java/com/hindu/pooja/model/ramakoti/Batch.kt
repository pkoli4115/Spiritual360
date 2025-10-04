package com.hindu.pooja.model.ramakoti

data class RamakotiBatch(
    val batchNumber: Int = 1,
    val language: String = "en",
    val completedCells: Int = 0,
    val status: String = "in_progress", // "in_progress" | "committed"
    val startedAt: Long = System.currentTimeMillis(),
    val committedAt: Long? = null,
    val cells: List<Map<String, Any?>> = List(108) {
        mapOf(
            "type" to "text",
            "value" to "",
            "lang" to "en",
            "ts" to 0L,
            "filled" to false
        )
    }
)
