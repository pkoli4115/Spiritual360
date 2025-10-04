package com.hindu.pooja.feature.ramakoti.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RamakotiDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: RamakotiEntry)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<RamakotiEntry>)

    @Query("SELECT COUNT(*) FROM ramakoti_entries")
    fun countFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ramakoti_entries")
    suspend fun countOnce(): Int

    @Query("DELETE FROM ramakoti_entries")
    suspend fun clearAll()

    @Query("SELECT * FROM ramakoti_entries ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun listPaged(limit: Int, offset: Int): List<RamakotiEntry>

    @Query("SELECT createdAt FROM ramakoti_entries WHERE createdAt >= :sinceMs ORDER BY createdAt DESC")
    suspend fun getTimestampsSince(sinceMs: Long): List<Long>
}
