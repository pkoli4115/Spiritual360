package com.hindu.pooja.feature.ramakoti.model

import com.google.firebase.Timestamp

data class RamakotiHistory(
    val id: String = "",
    val uid: String = "",
    val croreNumber: Int = 1,
    val totalInThisCrore: Long = 10_000_000L, // 1 Crore = 10,000,000
    val completedOn: Timestamp = Timestamp.now(),
    val certificateUrl: String = "",
    val reflection: String = "",
    val language: String = "EN"
)
