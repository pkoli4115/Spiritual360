package com.hindu.pooja.feature.ramakoti.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RamakotiEntry::class],
    version = 1,
    exportSchema = true
)
abstract class RamakotiDb : RoomDatabase() {
    abstract fun ramakotiDao(): RamakotiDao
}
