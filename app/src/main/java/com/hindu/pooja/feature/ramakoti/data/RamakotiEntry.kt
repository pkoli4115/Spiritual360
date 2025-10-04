package com.hindu.pooja.feature.ramakoti.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ramakoti_entries")
data class RamakotiEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val text: String = "శ్రీరామ", // default
    val createdAt: Long = System.currentTimeMillis()
)
