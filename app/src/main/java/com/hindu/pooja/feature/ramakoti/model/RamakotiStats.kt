package com.hindu.pooja.feature.ramakoti.model

import com.google.firebase.Timestamp

data class RamakotiStats(
    val totalCount: Long = 0L,
    val croreCount: Int = 0,
    val currentCroreNumber: Int = 1,
    val lastUpdated: Timestamp = Timestamp.now()
)
